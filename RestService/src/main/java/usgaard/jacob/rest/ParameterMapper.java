package usgaard.jacob.rest;

import java.util.Map;

public interface ParameterMapper {
	public Map<String, String[]> generateParameterMap(Object object);
}
