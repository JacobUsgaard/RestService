package usgaard.jacob.rest;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usgaard.jacob.rest.FieldMapper.Sort;
import usgaard.jacob.rest.exception.ConversionException;
import usgaard.jacob.rest.exception.ParameterException;
import usgaard.jacob.rest.hibernate.CriteriaGenerator;
import usgaard.jacob.rest.request.FieldMapping;
import usgaard.jacob.rest.request.OperatorMapping;
import usgaard.jacob.rest.request.RestRequest;
import usgaard.jacob.rest.request.SearchCriterion;

/**
 * A service to be used to create mappings between a provided query and the
 * request class. Common conversions are already supported (e.g. REST queries in
 * form of {@link String} or {@link ServletRequest} but custom mapping is also
 * available.
 * 
 * Features:
 * 
 * <ol>
 * <li><a href="#start">Start</a></li>
 * <li><a href="#limit">Limit</a></li>
 * <li><a href="#fields">Fields</a></li>
 * </ol>
 * 
 * <h3 id="start">Start</h3>
 * <h3 id="limit">Limit</h3>
 * <h3 id="fields">Fields</h3>
 * 
 * 
 * @author Jacob
 * @see RestRequest
 */

public class RestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestService.class);

	private static int defaultStart = 0;
	private static int defaultLimit = 10;
	private Integer start;
	private Integer limit;
	private static String defaultFieldsParameterName = "fields";
	private static String defaultStartParameterName = "start";
	private static String defaultLimitParameterName = "limit";
	private String fieldsParameterName = defaultFieldsParameterName;
	private String startParameterName = defaultStartParameterName;
	private String limitParameterName = defaultLimitParameterName;
	private static List<OperatorMapping> operatorMappings = new ArrayList<OperatorMapping>() {
		private static final long serialVersionUID = -8895872087917806097L;

		{
			add(new OperatorMapping(Pattern.compile("\\="), Operator.EQUAL));
			add(new OperatorMapping(Pattern.compile("\\>"), Operator.GREATER_THAN));
			add(new OperatorMapping(Pattern.compile("\\<"), Operator.LESS_THAN));
			add(new OperatorMapping(Pattern.compile("\\>\\="), Operator.GREATER_THAN_OR_EQUAL));
			add(new OperatorMapping(Pattern.compile("\\<\\="), Operator.LESS_THAN_OR_EQUAL));
		}
	};

	public enum Operator {
		EQUAL, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL;
	}

	private static final FieldMapper<PropertyDescriptor> fieldMapper = new FieldMapper<PropertyDescriptor>() {

		@Override
		public <Id, Op, Val> List<FieldMapping<PropertyDescriptor>> generateFieldMappings(Class<?> clazz,
				List<ParameterMapping<Id, Op, Val>> parameterMappings, TypeGenerator typeGenerator,
				Id fieldParameterIdentifier) throws ConversionException, IntrospectionException {
			List<FieldMapping<PropertyDescriptor>> fieldMappings = new LinkedList<FieldMapping<PropertyDescriptor>>();
			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();

			Val fieldsValue = null;

			for (ParameterMapping<Id, Op, Val> parameterMapping : parameterMappings) {
				if (parameterMapping.getIdentifier().equals(fieldParameterIdentifier)) {
					fieldsValue = parameterMapping.getValue();
					break;
				}
			}

			LOGGER.debug("fields parameter: {}, value: {}", fieldParameterIdentifier, fieldsValue);

			if (fieldsValue == null) {
				for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
					if ("class".equals(propertyDescriptor.getName())) {
						continue;
					}
					fieldMappings.add(new FieldMapping<PropertyDescriptor>(propertyDescriptor, null));
					LOGGER.debug("Field found: {}, sort: {}", propertyDescriptor.getName(), null);
				}

				return fieldMappings;
			}

			String fieldsParameterValue = typeGenerator.generateType(String.class, fieldsValue);
			String[] fields = fieldsParameterValue.split(",");

			for (String field : fields) {
				for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
					String propertyName = field.replaceAll("(\\+|\\-)", "");

					if (propertyName.equalsIgnoreCase(propertyDescriptor.getName())) {
						Sort sort = null;
						if (field.endsWith("+")) {
							sort = Sort.ASCENDING;
						} else if (field.endsWith("-")) {
							sort = Sort.DESCENDING;
						}

						fieldMappings.add(new FieldMapping<PropertyDescriptor>(propertyDescriptor, sort));
						LOGGER.debug("Field found: {}, sort: {}", propertyDescriptor.getName(), sort);
					}
				}
			}

			return fieldMappings;
		}
	};

	private static final SearchCriteriaGenerator<PropertyDescriptor, Operator, Object> searchCriteriaGenerator = new SearchCriteriaGenerator<PropertyDescriptor, Operator, Object>() {

		@Override
		public <Id, Op, Val> List<SearchCriterion<PropertyDescriptor, Operator, Object>> generateSearchCriteria(
				Class<?> clazz, List<ParameterMapping<Id, Op, Val>> parameterMappings, TypeGenerator typeGenerator)
				throws ConversionException, IntrospectionException {
			List<SearchCriterion<PropertyDescriptor, Operator, Object>> searchCriteria = new LinkedList<SearchCriterion<PropertyDescriptor, Operator, Object>>();
			for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
				Class<?> parameterType = propertyDescriptor.getPropertyType();
				String propertyName = propertyDescriptor.getName();

				if ("class".equalsIgnoreCase(propertyName)) {
					continue;
				}

				SearchCriterion<PropertyDescriptor, Operator, Object> searchCriterion;
				Id id = null;
				Operator operator = null;
				for (ParameterMapping<Id, Op, Val> parameterMapping : parameterMappings) {
					id = parameterMapping.getIdentifier();

					if (id instanceof String && id.equals(propertyName)) {

						for (OperatorMapping operatorMapping : operatorMappings) {
							if (operatorMapping.getOperator().equals(parameterMapping.getOperator())) {
								operator = operatorMapping.getOperator();
							}
						}

						searchCriterion = new SearchCriterion<PropertyDescriptor, RestService.Operator, Object>(
								propertyDescriptor, operator,
								typeGenerator.generateType(parameterType, parameterMapping.getValue()));
						searchCriteria.add(searchCriterion);
						LOGGER.debug("SearchValue found: {}, operator: {}, value: {}",
								searchCriterion.getIdentifier().getName(), searchCriterion.getOperator(),
								searchCriterion.getValue());
						break;
					}
				}
			}
			return searchCriteria;
		}
	};

	private static final ParameterMapper<String, Operator, Object> queryParameterMapper = new ParameterMapper<String, Operator, Object>() {

		@Override
		public List<ParameterMapping<String, Operator, Object>> generateParameterMappings(Object source)
				throws ParameterException {
			if (source == null) {
				return null;
			}

			String query = (String) source;
			String[] nameValuePairs = query.split("\\&");
			List<ParameterMapping<String, Operator, Object>> parameterMappings = new LinkedList<ParameterMapping<String, Operator, Object>>();

			if (nameValuePairs == null || nameValuePairs.length == 0) {
				return parameterMappings;
			}

			String name;
			Object value;
			ParameterMapping<String, Operator, Object> parameterMapping;

			Matcher matcher;
			for (String nameValuePair : nameValuePairs) {
				parameterMapping = new ParameterMapping<String, Operator, Object>();
				int maxLength = -1, currentLength, start = -1, end = -1;
				for (OperatorMapping operatorMapping : operatorMappings) {
					matcher = operatorMapping.getPattern().matcher(nameValuePair);

					if (!matcher.find()) {
						LOGGER.trace("operator {} does not match input string: {}", operatorMapping.getOperator(),
								nameValuePair);
						continue;
					}

					LOGGER.trace("operator {} matches input string: {}", operatorMapping.getOperator(), nameValuePair);
					start = matcher.start();
					end = matcher.end();
					currentLength = end - start;
					if (maxLength < currentLength) {
						maxLength = currentLength;
						parameterMapping.setOperator(operatorMapping.getOperator());
					}
				}

				if (parameterMapping.getOperator() == null) {
					throw new ParameterException("Unable to find operator for parameter: " + nameValuePair);
				}

				name = nameValuePair.substring(0, start);
				value = nameValuePair.substring(end);

				if (name == null) {
					continue;
				}

				LOGGER.debug("parameter mapped: {}, value: {}, operator: {}", name, value,
						parameterMapping.getOperator());

				parameterMapping.setIdentifier(name);
				parameterMapping.setValue(value);

				parameterMappings.add(parameterMapping);
			}

			return parameterMappings;
		}
	};

	private static final ParameterMapper<String, Operator, Object> servletRequestParameterMapper = new ParameterMapper<String, Operator, Object>() {

		@Override
		public List<ParameterMapping<String, Operator, Object>> generateParameterMappings(Object source)
				throws ParameterException {
			if (source == null) {
				return null;
			}

			return queryParameterMapper.generateParameterMappings(((HttpServletRequest) source).getQueryString());
		}
	};

	private static TypeGenerator defaultTypeGenerator = new TypeGenerator() {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T generateType(Class<T> clazz, Object object) throws ConversionException {
			if (clazz == null || object == null) {
				return null;
			}

			if (String.class.equals(clazz)) {
				return (T) object.toString();
			}

			if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
				return (T) new Integer(object.toString());
			}

			if (Long.class.equals(clazz) || long.class.equals(clazz)) {
				return (T) new Long(object.toString());
			}

			if (Double.class.equals(clazz) || double.class.equals(clazz)) {
				return (T) new Double(object.toString());
			}

			if (Float.class.equals(clazz) || float.class.equals(clazz)) {
				return (T) new Float(object.toString());
			}

			if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
				return (T) new Boolean(object.toString());
			}

			if (clazz.equals(object.getClass())) {
				return (T) object;
			}

			throw new ConversionException("Could not convert from: " + object.getClass() + " to: " + clazz);
		}
	};

	private static CriteriaGenerator<PropertyDescriptor, Operator, Object> criteriaGenerator = new CriteriaGenerator<PropertyDescriptor, RestService.Operator, Object>() {

		@Override
		public Criteria generateCriteria(Session session,
				RestRequest<PropertyDescriptor, Operator, Object> restRequest) {
			if (session == null || !session.isOpen()) {
				return null;
			}

			Criteria criteria = session.createCriteria(restRequest.getRootClass());

			String propertyName;
			Object value;
			for (SearchCriterion<PropertyDescriptor, Operator, Object> searchCriterion : restRequest
					.getSearchCriteria()) {
				propertyName = searchCriterion.getIdentifier().getName();
				value = searchCriterion.getValue();

				switch (searchCriterion.getOperator()) {
				case EQUAL:
					criteria.add(Restrictions.eq(propertyName, value));
					break;
				case GREATER_THAN:
					criteria.add(Restrictions.gt(propertyName, value));
					break;
				case GREATER_THAN_OR_EQUAL:
					criteria.add(Restrictions.ge(propertyName, value));
					break;
				case LESS_THAN:
					criteria.add(Restrictions.lt(propertyName, value));
					break;
				case LESS_THAN_OR_EQUAL:
					criteria.add(Restrictions.le(propertyName, value));
					break;
				}
			}

			Sort sort;
			ProjectionList projectionList = Projections.projectionList();
			for (FieldMapping<PropertyDescriptor> fieldMapping : restRequest.getFieldMappings()) {
				propertyName = fieldMapping.getIdentifier().getName();
				sort = fieldMapping.getSort();

				projectionList.add(Projections.property(propertyName));
				LOGGER.debug("adding projection: {}", propertyName);

				switch (sort) {
				case ASCENDING:
					criteria.addOrder(Order.asc(propertyName));
					break;
				case DESCENDING:
					criteria.addOrder(Order.desc(propertyName));
					break;
				}

				LOGGER.debug("adding order: {}", sort);
			}

			LOGGER.debug("projections: {}", projectionList.getLength());
			criteria.setProjection(projectionList);
			criteria.setFirstResult(restRequest.getStart());
			criteria.setMaxResults(restRequest.getLimit());

			return criteria;
		}

	};

	private void setParameterMapperDefaults(ParameterMapper<String, Operator, Object> parameterMapper) {
		parameterMapper.setFieldsParameterIdentifier(this.getUsableFieldsParameterName());
		parameterMapper.setStartParameterIdentifier(this.getUsableStartParameterName());
		parameterMapper.setLimitParameterIdentifier(this.getUsableLimitParameterName());
	}

	public RestRequest<PropertyDescriptor, Operator, Object> convert(String query, Class<?> clazz)
			throws IntrospectionException, ParameterException, ConversionException {
		this.setParameterMapperDefaults(queryParameterMapper);

		return this.convert(query, clazz, queryParameterMapper, defaultTypeGenerator, searchCriteriaGenerator,
				fieldMapper);
	}

	public RestRequest<PropertyDescriptor, Operator, Object> convert(ServletRequest servletRequest, Class<?> clazz)
			throws IntrospectionException, ParameterException, ConversionException {
		this.setParameterMapperDefaults(servletRequestParameterMapper);

		return this.convert(servletRequest, clazz, servletRequestParameterMapper, defaultTypeGenerator,
				searchCriteriaGenerator, fieldMapper);
	}

	public <ParameterMapperIdentifier, ParameterMapperOperator, ParameterMapperValue> RestRequest<PropertyDescriptor, Operator, Object> convert(
			Object object, Class<?> clazz,
			ParameterMapper<ParameterMapperIdentifier, ParameterMapperOperator, ParameterMapperValue> parameterMapper)
			throws IntrospectionException, ParameterException, ConversionException {
		return this.convert(object, clazz, parameterMapper, defaultTypeGenerator, searchCriteriaGenerator, fieldMapper);
	}

	public <SearchCriteriaId, SearchCriteriaOperator, SearchCriteriaValue, ParameterMapperIdentifier, ParameterMapperOperator, ParameterMapperValue> RestRequest<SearchCriteriaId, SearchCriteriaOperator, SearchCriteriaValue> convert(
			Object object, Class<?> clazz,
			ParameterMapper<ParameterMapperIdentifier, ParameterMapperOperator, ParameterMapperValue> parameterMapper,
			TypeGenerator typeGenerator,
			SearchCriteriaGenerator<SearchCriteriaId, SearchCriteriaOperator, SearchCriteriaValue> searchCriteriaGenerator,
			FieldMapper<SearchCriteriaId> fieldMapper)
			throws IntrospectionException, ParameterException, ConversionException {

		if (parameterMapper == null || typeGenerator == null || searchCriteriaGenerator == null
				|| fieldMapper == null) {
			return null;
		}

		RestRequest<SearchCriteriaId, SearchCriteriaOperator, SearchCriteriaValue> restRequest = new RestRequest<SearchCriteriaId, SearchCriteriaOperator, SearchCriteriaValue>();

		restRequest.setRootClass(clazz);

		List<ParameterMapping<ParameterMapperIdentifier, ParameterMapperOperator, ParameterMapperValue>> parameterMappings = parameterMapper
				.generateParameterMappings(object);
		LOGGER.debug("fieldsParameterIdentifier: {}", parameterMapper.getFieldsParameterIdentifier());
		restRequest.setSearchCriteria(
				searchCriteriaGenerator.generateSearchCriteria(clazz, parameterMappings, typeGenerator));
		restRequest.setFieldMappings(fieldMapper.generateFieldMappings(clazz, parameterMappings, typeGenerator,
				parameterMapper.getFieldsParameterIdentifier()));

		restRequest.setStart(getUsableStart());
		restRequest.setLimit(getUsableLimit());

		LOGGER.debug("startParameterIdentifier: {}", parameterMapper.getStartParameterIdentifier());
		LOGGER.debug("limitParameterIdentifier: {}", parameterMapper.getLimitParameterIdentifier());

		for (ParameterMapping<ParameterMapperIdentifier, ParameterMapperOperator, ParameterMapperValue> parameterMapping : parameterMappings) {
			if (parameterMapping.getIdentifier().equals(parameterMapper.getStartParameterIdentifier())) {
				restRequest.setStart(typeGenerator.generateType(int.class, parameterMapping.getValue()));
				break;
			}

			if (parameterMapping.getIdentifier().equals(parameterMapper.getLimitParameterIdentifier())) {
				restRequest.setLimit(typeGenerator.generateType(int.class, parameterMapping.getValue()));
			}
		}

		return restRequest;
	}

	public <T> List<T> convert(String query, Class<T> clazz, Session session)
			throws IntrospectionException, ParameterException, ConversionException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		RestRequest<PropertyDescriptor, Operator, Object> restRequest = this.convert(query, clazz);
		Criteria criteria = criteriaGenerator.generateCriteria(session, restRequest);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = criteria.list();

		List<FieldMapping<PropertyDescriptor>> columns = restRequest.getFieldMappings();
		List<T> list = new LinkedList<T>();
		T object = null;
		int count = 0;
		for (Object[] row : rows) {
			LOGGER.debug("entity mapping: {}", ++count);
			object = clazz.newInstance();
			for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
				FieldMapping<PropertyDescriptor> column = columns.get(columnIndex);
				column.getIdentifier().getWriteMethod().invoke(object, row[columnIndex]);
				LOGGER.debug("placed column {} with value {}", column.getIdentifier().getName(), row[columnIndex]);
			}

			list.add(object);
		}

		return list;
	}

	private int getUsableStart() {
		return this.start == null ? defaultStart : this.start;
	}

	private int getUsableLimit() {
		return this.limit == null ? defaultLimit : this.limit;
	}

	private String getUsableFieldsParameterName() {
		return (this.fieldsParameterName == null || this.fieldsParameterName.isEmpty()) ? defaultFieldsParameterName
				: this.fieldsParameterName;
	}

	private String getUsableStartParameterName() {
		return (this.startParameterName == null || this.startParameterName.isEmpty()) ? defaultStartParameterName
				: this.startParameterName;
	}

	private String getUsableLimitParameterName() {
		return (this.limitParameterName == null || this.limitParameterName.isEmpty()) ? defaultLimitParameterName
				: this.limitParameterName;
	}

	public void setDefaultTypeGenerator(TypeGenerator defaultTypeGenerator) {
		RestService.defaultTypeGenerator = defaultTypeGenerator;
	}

	public static int getDefaultStart() {
		return defaultStart;
	}

	public static void setDefaultStart(int defaultStart) {
		RestService.defaultStart = defaultStart;
	}

	public static int getDefaultLimit() {
		return defaultLimit;
	}

	public static void setDefaultLimit(int defaultLimit) {
		RestService.defaultLimit = defaultLimit;
	}

	public String getFieldsParameterName() {
		return fieldsParameterName;
	}

	public void setFieldsParameterName(String fieldsParameterName) {
		this.fieldsParameterName = fieldsParameterName;
	}

	public String getStartParameterName() {
		return startParameterName;
	}

	public void setStartParameterName(String startParameterName) {
		this.startParameterName = startParameterName;
	}

	public String getLimitParameterName() {
		return limitParameterName;
	}

	public void setLimitParameterName(String limitParameterName) {
		this.limitParameterName = limitParameterName;
	}

	public FieldMapper<PropertyDescriptor> getFieldMapper() {
		return fieldMapper;
	}

	public SearchCriteriaGenerator<PropertyDescriptor, Operator, Object> getSearchCriteriaGenerator() {
		return searchCriteriaGenerator;
	}

	public ParameterMapper<String, Operator, Object> getQueryParameterMapper() {
		return queryParameterMapper;
	}

	public ParameterMapper<String, Operator, Object> getServletRequestParameterMapper() {
		return servletRequestParameterMapper;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

}