package usgaard.jacob.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

public class ServletRequestParameterMapper implements ParameterMapper {

	@Override
	public Map<String, List<Object>> generateParameterMap(Object object) {
		ServletRequest servletRequest = (ServletRequest) object;
		Map<String, List<Object>> parameterMap = new HashMap<>();

		Enumeration<String> parameterNames = servletRequest.getParameterNames();
		if (parameterNames == null) {
			return parameterMap;
		}

		for (String parameterName : Collections.list(servletRequest.getParameterNames())) {
			parameterMap.put(parameterName, Arrays.asList((Object[]) servletRequest.getParameterValues(parameterName)));
		}

		return parameterMap;
	}

}
