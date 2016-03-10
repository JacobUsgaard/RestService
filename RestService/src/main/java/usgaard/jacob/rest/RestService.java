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

import usgaard.jacob.rest.request.RestRequest;
import usgaard.jacob.rest.request.RestRequest.Sort;

public class RestService {
	public static final String DEFAULT_PARAMETER_NAME_START = "start";

	public static final String DEFAULT_PARAMETER_NAME_LIMIT = "limit";

	public static final String DEFAULT_PARAMETER_NAME_FIELDS = "fields";

	protected String startParameterName = DEFAULT_PARAMETER_NAME_START;

	protected String limitParameterName = DEFAULT_PARAMETER_NAME_LIMIT;

	protected String fieldsParameterName = DEFAULT_PARAMETER_NAME_FIELDS;

	private static ParameterMapper queryParameterMapper = new ParameterMapper() {

		@Override
		public Map<String, List<Object>> generateParameterMap(Object object) {
			String queryString = (String) object;
			String[] parameterPairs = queryString.split("\\&");
			Map<String, List<Object>> parameterMap = new HashMap<>();
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
					parameterValues = new LinkedList<>();
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
			Map<String, List<Object>> parameterMap = new HashMap<>();

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

	private static TypeGenerator defaultTypeGenerator = new TypeGenerator() {

		@Override
		public Object generateType(Class<?> clazz, Object object) {
			if (clazz == null || object == null) {
				return null;
			}
			Class<?> type = clazz.getClass();

			if (type.equals(String.class)) {
				return object.toString();
			}

			if (type.equals(Integer.class) || type.equals(int.class)) {
				return new Integer(object.toString());
			}

			if (type.equals(Long.class) || type.equals(long.class)) {
				return new Long(object.toString());
			}

			if (type.equals(Double.class) || type.equals(double.class)) {
				return new Double(object.toString());
			}

			if (type.equals(Float.class) || type.equals(float.class)) {
				return new Float(object.toString());
			}

			if (type.equals(object.getClass())) {
				return object;
			}

			return null;
		}

	};

	public RestRequest convert(String query, Class<?> clazz) throws IntrospectionException {
		Map<String, List<Object>> parameterMap = queryParameterMapper.generateParameterMap(query);
		return this.createRestRequest(clazz, parameterMap, defaultTypeGenerator);
	}

	public RestRequest convert(ServletRequest servletRequest, Class<?> clazz) throws IntrospectionException {
		Map<String, List<Object>> parameterMap = servletRequestParameterMapper.generateParameterMap(servletRequest);
		return this.createRestRequest(clazz, parameterMap, defaultTypeGenerator);
	}

	public RestRequest convert(Map<String, List<Object>> parameterMap, Class<?> clazz) throws IntrospectionException {
		return this.createRestRequest(clazz, parameterMap, defaultTypeGenerator);
	}

	public RestRequest convert(Object object, Class<?> clazz, ParameterMapper parameterMapper)
			throws IntrospectionException {
		Map<String, List<Object>> parameterMap = parameterMapper.generateParameterMap(object);
		return this.createRestRequest(clazz, parameterMap, defaultTypeGenerator);
	}

	public RestRequest convert(Object object, Class<?> clazz, ParameterMapper parameterMapper,
			TypeGenerator typeGenerator) throws IntrospectionException {
		Map<String, List<Object>> parameterMap = parameterMapper.generateParameterMap(object);
		if (typeGenerator == null) {
			return this.createRestRequest(clazz, parameterMap, defaultTypeGenerator);
		}

		return this.createRestRequest(clazz, parameterMap, typeGenerator);
	}

	protected RestRequest createRestRequest(Class<?> clazz, Map<String, List<Object>> parameterMap,
			TypeGenerator typeGenerator) throws IntrospectionException {
		RestRequest restRequest = new RestRequest();

		List<Object> limitValues = parameterMap.get(limitParameterName);
		if (limitValues != null && limitValues.size() == 1) {
			restRequest.setLimit(new Integer(limitValues.get(0).toString()));
		} else if (limitValues != null && limitValues.size() > 1) {
			// TODO throw error
		}

		List<Object> startValues = parameterMap.get(startParameterName);
		if (startValues != null && startValues.size() > 1) {
			restRequest.setStart(new Integer(limitValues.get(0).toString()));
		} else if (limitValues != null && limitValues.size() > 1) {
			// TODO throw error
		}

		List<Object> fieldsValues = parameterMap.get(fieldsParameterName);

		List<PropertyDescriptor> propertyDescriptors = Arrays
				.asList(Introspector.getBeanInfo(clazz).getPropertyDescriptors());
		if (fieldsValues == null || (fieldsValues.size() == 1 && fieldsValues.get(0) == null)) {
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				restRequest.getFields().put(propertyDescriptor, null);
			}
		} else if (fieldsValues.size() == 1) {
			String fieldsParameterValue = fieldsValues.get(0).toString();
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

						restRequest.getFields().put(propertyDescriptor, sort);
					}
				}
			}
		} else {
			// throw error
		}

		Map<PropertyDescriptor, List<Object>> searchValues = restRequest.getSearchValues();
		List<Object> values = null;
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			System.out.println("propertyDescriptor: " + propertyDescriptor.getName());
			Class<?> parameterType = propertyDescriptor.getPropertyType();
			Method writeMethod = propertyDescriptor.getWriteMethod();
			String parameterName = propertyDescriptor.getName();

			if (writeMethod == null || "class".equalsIgnoreCase(parameterName)) {
				continue;
			}

			List<Object> parameterValues = parameterMap.get(parameterName);
			if (parameterValues == null) {
				continue;
			}

			values = new ArrayList<>(parameterValues.size());
			for (Object parameterValue : parameterValues) {
				values.add(typeGenerator.generateType(parameterType, parameterValue));
			}

			searchValues.put(propertyDescriptor, values);
		}
		restRequest.setSearchValues(searchValues);

		return restRequest;
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

	public interface ParameterMapper {
		public Map<String, List<Object>> generateParameterMap(Object object);
	}

	public interface TypeGenerator {
		public Object generateType(Class<?> clazz, Object object);
	}
}