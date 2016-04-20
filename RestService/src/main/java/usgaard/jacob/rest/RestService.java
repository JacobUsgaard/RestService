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

import usgaard.jacob.rest.exception.ParameterException;
import usgaard.jacob.rest.request.RestRequest;
import usgaard.jacob.rest.request.RestRequest.Operator;
import usgaard.jacob.rest.request.RestRequest.Sort;
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

	private static final String DEFAULT_PARAMETER_NAME_START = "start";

	private static final String DEFAULT_PARAMETER_NAME_LIMIT = "limit";

	private static final String DEFAULT_PARAMETER_NAME_FIELDS = "fields";

	private static final int DEFAULT_PARAMETER_START = 0;

	private static final int DEFAULT_PARAMETER_LIMIT = 10;

	protected String startParameterName = DEFAULT_PARAMETER_NAME_START;

	protected String limitParameterName = DEFAULT_PARAMETER_NAME_LIMIT;

	protected String fieldsParameterName = DEFAULT_PARAMETER_NAME_FIELDS;

	protected TypeGenerator typeGenerator;

	private final ParameterMapper queryParameterMapper = new ParameterMapper() {

		@Override
		public List<ParameterMapping> generateParameterMappings(Object source) {
			String query = (String) source;
			String[] nameValuePairs = query.split("\\&");
			List<RestService.ParameterMapping> parameterMappings = new LinkedList<RestService.ParameterMapping>();

			if (nameValuePairs == null || nameValuePairs.length == 0) {
				return parameterMappings;
			}

			String[] nameValueArray;
			String name;
			Object value;
			ParameterMapping parameterMapping;

			Pattern pattern = Pattern.compile(".*=.*");

			for (String nameValuePair : nameValuePairs) {
				nameValueArray = nameValuePair.split("\\=", 2);

				name = nameValueArray[0];

				if (name == null) {
					continue;
				}

				value = nameValueArray.length == 1 ? null : nameValueArray[1];

				parameterMapping = new ParameterMapping();

				if (pattern.matcher(nameValuePair).matches()) {
					parameterMapping.operator = Operator.EQUAL;

				}

				LOGGER.debug("parameter mapped: {}, value: {}, operator: {}", name, value, parameterMapping.operator);

				parameterMapping.name = name;
				parameterMapping.value = value;

				parameterMappings.add(parameterMapping);
			}

			return parameterMappings;
		}
	};

	private final ParameterMapper servletRequestParameterMapper = new ParameterMapper() {

		@Override
		public List<ParameterMapping> generateParameterMappings(Object source) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) source;

			return queryParameterMapper.generateParameterMappings(httpServletRequest.getQueryString());
		}
	};

	private static TypeGenerator defaultTypeGenerator = new TypeGenerator() {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T generateType(Class<T> clazz, Object object) {
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

			LOGGER.debug("No conversion method found for {} -> {}", object.getClass(), clazz);
			return null;
		}
	};

	/**
	 * Converts the query into a {@link RestRequest} using following structure.
	 * 
	 * @param query
	 * @param clazz
	 * @return
	 * @throws IntrospectionException
	 * @throws ParameterException
	 */
	public RestRequest convert(String query, Class<?> clazz) throws IntrospectionException, ParameterException {
		List<ParameterMapping> parameterMappings = queryParameterMapper.generateParameterMappings(query);

		return this.createRestRequest(clazz, parameterMappings, this.getUsableTypeGenerator());
	}

	/**
	 * 
	 * @param servletRequest
	 * @param clazz
	 * @return
	 * @throws IntrospectionException
	 * @throws ParameterException
	 */
	public RestRequest convert(ServletRequest servletRequest, Class<?> clazz)
			throws IntrospectionException, ParameterException {
		List<ParameterMapping> parameterMappings = servletRequestParameterMapper
				.generateParameterMappings(servletRequest);

		return this.createRestRequest(clazz, parameterMappings, this.getUsableTypeGenerator());
	}

	/**
	 * @param parameterMappings
	 * @param clazz
	 * @return
	 * @throws IntrospectionException
	 * @throws ParameterException
	 */
	public RestRequest convert(List<ParameterMapping> parameterMappings, Class<?> clazz)
			throws IntrospectionException, ParameterException {
		return this.createRestRequest(clazz, parameterMappings, this.getUsableTypeGenerator());
	}

	/**
	 * @param object
	 * @param clazz
	 * @param parameterMapper
	 * @return
	 * @throws IntrospectionException
	 * @throws ParameterException
	 */
	public RestRequest convert(Object object, Class<?> clazz, ParameterMapper parameterMapper)
			throws IntrospectionException, ParameterException {
		List<ParameterMapping> parameterMappings = parameterMapper.generateParameterMappings(object);
		return this.createRestRequest(clazz, parameterMappings, this.getUsableTypeGenerator());
	}

	/**
	 * @param object
	 * @param clazz
	 * @param parameterMapper
	 * @param typeGenerator
	 * @return
	 * @throws IntrospectionException
	 * @throws ParameterException
	 */
	public RestRequest convert(Object object, Class<?> clazz, ParameterMapper parameterMapper,
			TypeGenerator typeGenerator) throws IntrospectionException, ParameterException {
		List<ParameterMapping> parameterMappings = parameterMapper.generateParameterMappings(object);
		if (typeGenerator == null) {
			return this.convert(object, clazz, parameterMapper);
		}

		return this.createRestRequest(clazz, parameterMappings, typeGenerator);
	}

	/**
	 * @param clazz
	 * @param parameterMappings
	 * @param typeGenerator
	 * @return
	 * @throws IntrospectionException
	 * @throws ParameterException
	 */
	protected RestRequest createRestRequest(Class<?> clazz, List<ParameterMapping> parameterMappings,
			TypeGenerator typeGenerator) throws IntrospectionException, ParameterException {
		RestRequest restRequest = new RestRequest();

		restRequest.setLimit(this.getLimitParameter(parameterMappings));
		LOGGER.debug("RestRequest limit: {}", restRequest.getLimit());

		restRequest.setStart(this.getStartParameter(parameterMappings));
		LOGGER.debug("RestRequest start: {}", restRequest.getStart());

		PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();

		restRequest.setFields(this.getFieldsParameter(propertyDescriptors, parameterMappings));
		LOGGER.debug("RestRequest fields: {}", restRequest.getFields().size());

		restRequest.setSearchCriteria(this.getSearchCriteria(propertyDescriptors, parameterMappings));
		LOGGER.debug("Search values found: {}", restRequest.getSearchCriteria().size());

		return restRequest;
	}

	/**
	 * @param propertyDescriptors
	 * @param parameterMappings
	 * @return
	 * @throws ParameterException
	 */
	protected Map<PropertyDescriptor, Sort> getFieldsParameter(PropertyDescriptor[] propertyDescriptors,
			List<ParameterMapping> parameterMappings) {
		Map<PropertyDescriptor, Sort> fieldsMap = new HashMap<PropertyDescriptor, Sort>();

		Object fieldsValue = null;

		for (ParameterMapping parameterMapping : parameterMappings) {
			if (parameterMapping.getName().equals(this.fieldsParameterName)) {
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

		String fieldsParameterValue = this.getUsableTypeGenerator().generateType(String.class, fieldsValue);
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

	/**
	 * @param propertyDescriptors
	 * @param parameterMappings
	 * @return
	 * @throws ParameterException
	 */
	protected List<SearchCriterion> getSearchCriteria(PropertyDescriptor[] propertyDescriptors,
			List<ParameterMapping> parameterMappings) throws ParameterException {
		List<SearchCriterion> searchCriteria = new LinkedList<SearchCriterion>();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			Class<?> parameterType = propertyDescriptor.getPropertyType();
			String propertyName = propertyDescriptor.getName();

			if ("class".equalsIgnoreCase(propertyName)) {
				continue;
			}

			SearchCriterion searchCriterion;
			String parameterName;
			for (ParameterMapping parameterMapping : parameterMappings) {
				parameterName = parameterMapping.getName();

				if (parameterName.equals(this.fieldsParameterName)) {
					throw new ParameterException("Conflicting parameter name with special 'fields' parameter name: "
							+ this.fieldsParameterName);
				}

				if (parameterName.equals(this.startParameterName)) {
					throw new ParameterException("Conflicting parameter name with special 'start' parameter name: "
							+ this.startParameterName);
				}

				if (parameterName.equals(this.limitParameterName)) {
					throw new ParameterException("Conflicting parameter name with special 'limit' parameter name: "
							+ this.limitParameterName);
				}

				if (parameterName.equals(propertyName)) {

					searchCriterion = new SearchCriterion(
							this.getUsableTypeGenerator().generateType(parameterType, parameterMapping.getValue()),
							parameterMapping.getOperator(), propertyDescriptor);
					searchCriteria.add(searchCriterion);
					LOGGER.debug("SearchValue found: {}, value: {}, operator: {}",
							searchCriterion.getPropertyDescriptor().getName(), searchCriterion.getValue(),
							searchCriterion.getOperator());
					break;
				}
			}
		}
		return searchCriteria;
	}

	/**
	 * @param parameterMappings
	 * @return
	 * @throws ParameterException
	 */
	protected Integer getLimitParameter(List<ParameterMapping> parameterMappings) {
		for (ParameterMapping parameterMapping : parameterMappings) {
			if (parameterMapping.getName().equals(this.getLimitParameterName())) {
				return this.getUsableTypeGenerator().generateType(Integer.class, parameterMapping.getValue());
			}
		}

		return DEFAULT_PARAMETER_LIMIT;
	}

	/**
	 * @param parameterMappings
	 * @return
	 * @throws ParameterException
	 */
	protected Integer getStartParameter(List<ParameterMapping> parameterMappings) {
		for (ParameterMapping parameterMapping : parameterMappings) {
			if (parameterMapping.getName().equals(this.getStartParameterName())) {
				return this.getUsableTypeGenerator().generateType(Integer.class, parameterMapping.getValue());
			}
		}

		return DEFAULT_PARAMETER_START;
	}

	private TypeGenerator getUsableTypeGenerator() {
		return this.typeGenerator == null ? RestService.defaultTypeGenerator : this.typeGenerator;
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

	public String getFieldsParameterName() {
		return fieldsParameterName;
	}

	public void setFieldsParameterName(String fieldsParameterName) {
		this.fieldsParameterName = fieldsParameterName;
	}

	public TypeGenerator getDefaultTypeGenerator() {
		return defaultTypeGenerator;
	}

	public void setDefaultTypeGenerator(TypeGenerator defaultTypeGenerator) {
		RestService.defaultTypeGenerator = defaultTypeGenerator;
	}

	public TypeGenerator getTypeGenerator() {
		return typeGenerator;
	}

	public void setTypeGenerator(TypeGenerator typeGenerator) {
		this.typeGenerator = typeGenerator;
	}

	protected static class ParameterMapping {
		private String name;
		private Operator operator;
		private Object value;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Operator getOperator() {
			return operator;
		}

		public void setOperator(Operator operator) {
			this.operator = operator;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}
}