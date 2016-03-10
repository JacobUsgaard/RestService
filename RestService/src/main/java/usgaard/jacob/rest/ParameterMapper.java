package usgaard.jacob.rest;

import java.util.List;
import java.util.Map;

public interface ParameterMapper {
	public Map<String, List<Object>> generateParameterMap(Object object);
}
