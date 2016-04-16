package usgaard.jacob.rest;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import usgaard.jacob.rest.exception.ParameterException;
import usgaard.jacob.rest.request.RestRequest;
import usgaard.jacob.rest.request.RestRequest.Sort;

public class RestService {
	private static final String DEFAULT_PARAMETER_NAME_START = "start";

	private static final String DEFAULT_PARAMETER_NAME_LIMIT = "limit";

	private static final String DEFAULT_PARAMETER_NAME_FIELDS = "fields";

	private static final int DEFAULT_PARAMETER_START = 0;

	private static final int DEFAULT_PARAMETER_LIMIT = 10;

	protected String startParameterName = DEFAULT_PARAMETER_NAME_START;

	protected String limitParameterName = DEFAULT_PARAMETER_NAME_LIMIT;

	protected String fieldsParameterName = DEFAULT_PARAMETER_NAME_FIELDS;

	protected TypeGenerator defaultTypeGenerator = typeGenerator;

	private static ParameterMapper queryParameterMapper = new ParameterMapper() {

		@Override
		public Map<String, List<Object>> generateParameterMap(Object object) {
			String queryString = (String) object;
			String[] parameterPairs = queryString.split("\\&");
			Map<String, List<Object>> parameterMap = new HashMap<String, List<Object>>();
			if (parameterPairs == null) {
				return parameterMap;
			}

			for (String parameterPair : parameterPairs) {
				String[] nameValueArray = parameterPair.split("\\=", 1);
				if (nameValueArray.length != 2) {
					continue;
				}

				String parameterName = nameValueArray[0];
				String parameterValue = nameValueArray[1];

				List<Object> parameterValues = parameterMap.get(parameterName);
				if (parameterValues == null) {
					parameterValues = new LinkedList<Object>();
				}

				parameterValues.add(parameterValue);
				parameterMap.put(parameterName, parameterValues);
			}

			return parameterMap;
		}
	};

	private ParameterMapper servletRequestParameterMapper = new ParameterMapper() {

		@Override
		public Map<String, List<Object>> generateParameterMap(Object object) {
			ServletRequest servletRequest = (ServletRequest) object;
			Map<String, List<Object>> parameterMap = new HashMap<String, List<Object>>();

			Enumeration<String> parameterNames = servletRequest.getParameterNames();
			if (parameterNames == null) {
				return parameterMap;
			}

			for (String parameterName : Collections.list(servletRequest.getParameterNames())) {
				parameterMap.put(parameterName,
						Arrays.asList((Object[]) servletRequest.getParameterValues(parameterName)));
			}

			return parameterMap;
		}
	};

	private static TypeGenerator typeGenerator = new TypeGenerator() {

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

			return null;
		}
	};

	public RestRequest convert(String query, Class<?> clazz) throws IntrospectionException, ParameterException {
		Map<String, List<Object>> parameterMap = queryParameterMapper.generateParameterMap(query);
		return this.createRestRequest(clazz, parameterMap, defaultTypeGenerator);
	}

	public RestRequest convert(ServletRequest servletRequest, Class<?> clazz)
			throws IntrospectionException, ParameterException {
		Map<String, List<Object>> parameterMap = servletRequestParameterMapper.generateParameterMap(servletRequest);
		return this.createRestRequest(clazz, parameterMap, defaultTypeGenerator);
	}

	public RestRequest convert(Map<String, List<Object>> parameterMap, Class<?> clazz)
			throws IntrospectionException, ParameterException {
		return this.createRestRequest(clazz, parameterMap, defaultTypeGenerator);
	}

	public RestRequest convert(Object object, Class<?> clazz, ParameterMapper parameterMapper)
			throws IntrospectionException, ParameterException {
		Map<String, List<Object>> parameterMap = parameterMapper.generateParameterMap(object);
		return this.createRestRequest(clazz, parameterMap, defaultTypeGenerator);
	}

	public RestRequest convert(Object object, Class<?> clazz, ParameterMapper parameterMapper,
			TypeGenerator typeGenerator) throws IntrospectionException, ParameterException {
		Map<String, List<Object>> parameterMap = parameterMapper.generateParameterMap(object);
		if (typeGenerator == null) {
			return this.createRestRequest(clazz, parameterMap, defaultTypeGenerator);
		}

		return this.createRestRequest(clazz, parameterMap, typeGenerator);
	}

	protected RestRequest createRestRequest(Class<?> clazz, Map<String, List<Object>> parameterMap,
			TypeGenerator typeGenerator) throws IntrospectionException, ParameterException {
		RestRequest restRequest = new RestRequest();

		restRequest.setLimit(this.getLimitParameter(parameterMap));

		restRequest.setStart(this.getStartParameter(parameterMap));

		PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();

		restRequest.setFields(this.getFieldsParameter(propertyDescriptors, parameterMap));

		restRequest.setSearchValues(this.getSearchValues(propertyDescriptors, parameterMap));

		return restRequest;
	}

	protected Map<PropertyDescriptor, Sort> getFieldsParameter(PropertyDescriptor[] propertyDescriptors,
			Map<String, List<Object>> parameterMap) throws ParameterException {
		List<Object> fieldsValues = parameterMap.get(fieldsParameterName);
		Map<PropertyDescriptor, Sort> fieldsMap = new HashMap<PropertyDescriptor, Sort>();

		if (fieldsValues == null) {
			return fieldsMap;
		} else if (fieldsValues.isEmpty()) {
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				fieldsMap.put(propertyDescriptor, null);
			}

			return fieldsMap;
		} else if (fieldsValues.size() > 1) {
			throw new ParameterException("Only one value allowed for special parameter: " + this.fieldsParameterName);
		}

		String fieldsParameterValue = typeGenerator.generateType(String.class, fieldsValues.get(0));
		String[] fields = fieldsParameterValue.split(",");

		for (String field : fields) {
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				String propertyName = field.replaceAll("(\\+|\\-)", "");

				if (propertyName.equalsIgnoreCase(propertyDescriptor.getName())) {
					Sort sort = null;
					if (field.contains("+")) {
						sort = Sort.ASCENDING;
					} else if (field.contains("-")) {
						sort = Sort.DESCENDING;
					}

					fieldsMap.put(propertyDescriptor, sort);
				}
			}
		}

		return fieldsMap;
	}

	protected Map<PropertyDescriptor, List<Object>> getSearchValues(PropertyDescriptor[] propertyDescriptors,
			Map<String, List<Object>> parameterMap) throws IntrospectionException {
		Map<PropertyDescriptor, List<Object>> searchValues = new HashMap<PropertyDescriptor, List<Object>>();
		List<Object> values = null;
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			Class<?> parameterType = propertyDescriptor.getPropertyType();
			Method writeMethod = propertyDescriptor.getWriteMethod();
			String parameterName = propertyDescriptor.getName();

			if (writeMethod == null || "class".equalsIgnoreCase(parameterName)) {
				continue;
			}

			List<Object> parameterValues = parameterMap.get(parameterName);
			if (parameterValues == null || parameterValues.isEmpty()) {
				continue;
			}

			values = new ArrayList<Object>(parameterValues.size());
			for (Object parameterValue : parameterValues) {
				values.add(typeGenerator.generateType(parameterType, parameterValue));
			}

			searchValues.put(propertyDescriptor, values);
		}

		return searchValues;
	}

	protected Integer getLimitParameter(Map<String, List<Object>> parameterMap) throws ParameterException {
		List<Object> limitValues = parameterMap.get(limitParameterName);

		if (limitValues == null || limitValues.isEmpty()) {
			return DEFAULT_PARAMETER_LIMIT;
		} else if (limitValues.size() > 1) {
			throw new ParameterException("Only one value allowed for special parameter: " + this.limitParameterName);
		}

		return typeGenerator.generateType(Integer.class, limitValues.get(0));
	}

	protected Integer getStartParameter(Map<String, List<Object>> parameterMap) throws ParameterException {
		List<Object> startValues = parameterMap.get(limitParameterName);

		if (startValues == null || startValues.isEmpty()) {
			return DEFAULT_PARAMETER_START;
		} else if (startValues.size() > 1) {
			throw new ParameterException("Only one value allowed for special parameter: " + this.startParameterName);
		}

		return typeGenerator.generateType(Integer.class, startValues.get(0));
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
		this.defaultTypeGenerator = defaultTypeGenerator;
	}

	public interface ParameterMapper {
		public Map<String, List<Object>> generateParameterMap(Object object);
	}

	public interface TypeGenerator {
		public <T> T generateType(Class<T> clazz, Object object);

	}
}