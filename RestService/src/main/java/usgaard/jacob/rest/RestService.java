package usgaard.jacob.rest;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usgaard.jacob.rest.exception.ConversionException;
import usgaard.jacob.rest.exception.ParameterException;
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

	public enum Operator {
		EQUAL, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL;
	}

	private final FieldMapper<PropertyDescriptor> fieldMapper = new FieldMapper<PropertyDescriptor>() {

		@Override
		public <Id, Op, Val> Map<PropertyDescriptor, FieldMapper.Sort> generateFields(Class<?> clazz,
				List<ParameterMapping<Id, Op, Val>> parameterMappings, TypeGenerator typeGenerator,
				Id fieldParameterIdentifier) throws ConversionException, IntrospectionException {
			Map<PropertyDescriptor, Sort> fieldsMap = new HashMap<PropertyDescriptor, Sort>();
			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();

			Val fieldsValue = null;

			for (ParameterMapping<Id, Op, Val> parameterMapping : parameterMappings) {
				if (parameterMapping.getIdentifier().equals(fieldParameterIdentifier)) {
					fieldsValue = parameterMapping.getValue();
					break;
				}
			}

			if (fieldsValue == null) {
				for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
					fieldsMap.put(propertyDescriptor, null);
				}

				return fieldsMap;
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

						fieldsMap.put(propertyDescriptor, sort);
						LOGGER.debug("Field found: {}, sort: {}", propertyDescriptor.getName(), sort);
					}
				}
			}

			return fieldsMap;
		}

	};

	private final SearchCriteriaGenerator<PropertyDescriptor, Operator, Object> searchCriteriaGenerator = new SearchCriteriaGenerator<PropertyDescriptor, Operator, Object>() {

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
				for (ParameterMapping<Id, Op, Val> parameterMapping : parameterMappings) {
					id = parameterMapping.getIdentifier();

					if (id instanceof String && id.equals(propertyName)) {
						searchCriterion = new SearchCriterion<PropertyDescriptor, RestService.Operator, Object>(
								propertyDescriptor, Operator.EQUAL,
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

	private final ParameterMapper<String, Operator, Object> queryParameterMapper = new ParameterMapper<String, Operator, Object>() {

		@Override
		public List<ParameterMapping<String, Operator, Object>> generateParameterMappings(Object source) {
			String query = (String) source;
			String[] nameValuePairs = query.split("\\&");
			List<ParameterMapping<String, Operator, Object>> parameterMappings = new LinkedList<ParameterMapping<String, Operator, Object>>();

			if (nameValuePairs == null || nameValuePairs.length == 0) {
				return parameterMappings;
			}

			String[] nameValueArray;
			String name;
			Object value;
			ParameterMapping<String, Operator, Object> parameterMapping;

			Pattern pattern = Pattern.compile(".*=.*");

			for (String nameValuePair : nameValuePairs) {
				nameValueArray = nameValuePair.split("\\=", 2);

				name = nameValueArray[0];

				if (name == null) {
					continue;
				}

				value = nameValueArray.length == 1 ? null : nameValueArray[1];

				parameterMapping = new ParameterMapping<String, Operator, Object>();

				if (pattern.matcher(nameValuePair).matches()) {
					parameterMapping.setOperator(Operator.EQUAL);

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

	private final ParameterMapper<String, Operator, Object> servletRequestParameterMapper = new ParameterMapper<String, Operator, Object>() {

		@Override
		public List<ParameterMapping<String, Operator, Object>> generateParameterMappings(Object source) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) source;

			return queryParameterMapper.generateParameterMappings(httpServletRequest.getQueryString());
		}
	};

	private static TypeGenerator defaultTypeGenerator = new TypeGenerator() {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T generateType(Class<T> clazz, Object object) throws ConversionException {
			if (clazz == null || object == null) {
				return null;
			}

			if (clazz.equals(String.class)) {
				return (T) object.toString();
			}

			if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
				return (T) new Integer(object.toString());
			}

			if (clazz.equals(Long.class) || clazz.equals(long.class)) {
				return (T) new Long(object.toString());
			}

			if (clazz.equals(Double.class) || clazz.equals(double.class)) {
				return (T) new Double(object.toString());
			}

			if (clazz.equals(Float.class) || clazz.equals(float.class)) {
				return (T) new Float(object.toString());
			}

			if (clazz.equals(object.getClass())) {
				return (T) object;
			}

			throw new ConversionException("Could not convert from: " + object.getClass() + " to: " + clazz);
		}
	};

	public RestRequest<PropertyDescriptor, Operator, Object> convert(String query, Class<?> clazz)
			throws IntrospectionException, ParameterException, ConversionException {

		return this.convert(query, clazz, queryParameterMapper, defaultTypeGenerator, searchCriteriaGenerator,
				fieldMapper);
	}

	public RestRequest<PropertyDescriptor, Operator, Object> convert(ServletRequest servletRequest, Class<?> clazz)
			throws IntrospectionException, ParameterException, ConversionException {

		return this.convert(servletRequest, clazz, servletRequestParameterMapper, defaultTypeGenerator,
				searchCriteriaGenerator, fieldMapper);
	}

	public <Id, Op, Val> RestRequest<PropertyDescriptor, Operator, Object> convert(Object object, Class<?> clazz,
			ParameterMapper<Id, Op, Val> parameterMapper)
			throws IntrospectionException, ParameterException, ConversionException {
		return this.convert(object, clazz, parameterMapper, defaultTypeGenerator, searchCriteriaGenerator, fieldMapper);
	}

	public <SCId, SCOp, SCVal, PMId, PMOp, PMVal> RestRequest<SCId, SCOp, SCVal> convert(Object object, Class<?> clazz,
			ParameterMapper<PMId, PMOp, PMVal> parameterMapper, TypeGenerator typeGenerator,
			SearchCriteriaGenerator<SCId, SCOp, SCVal> searchCriteriaGenerator, FieldMapper<SCId> fieldMapper)
			throws IntrospectionException, ParameterException, ConversionException {

		RestRequest<SCId, SCOp, SCVal> restRequest = new RestRequest<SCId, SCOp, SCVal>();

		List<ParameterMapping<PMId, PMOp, PMVal>> parameterMappings = parameterMapper.generateParameterMappings(object);
		restRequest.setSearchCriteria(
				searchCriteriaGenerator.generateSearchCriteria(clazz, parameterMappings, typeGenerator));
		restRequest.setFields(fieldMapper.generateFields(clazz, parameterMappings, typeGenerator,
				parameterMapper.getFieldsParameterIdentifier()));

		for (ParameterMapping<PMId, PMOp, PMVal> parameterMapping : parameterMappings) {
			if (parameterMapping.getIdentifier().equals(parameterMapper.getStartParameterIdentifier())) {
				restRequest.setStart(this.getUsableStart());
				break;
			}

			if (parameterMapping.getIdentifier().equals(parameterMapper.getStartParameterIdentifier())) {
				restRequest.setLimit(this.getUsableLimit());
			}
		}

		return restRequest;
	}

	private int getUsableStart() {
		return this.start == null ? defaultStart : this.start;
	}

	private int getUsableLimit() {
		return this.limit == null ? defaultLimit : this.limit;
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

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}