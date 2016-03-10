package usgaard.jacob.rest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QueryParameterMapper implements ParameterMapper {

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

}
