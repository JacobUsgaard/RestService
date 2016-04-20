package usgaard.jacob.rest;

import java.util.List;

import usgaard.jacob.rest.RestService.ParameterMapping;

/**
 * @author Jacob
 *
 */
public interface ParameterMapper {
	public List<ParameterMapping> generateParameterMappings(Object source);
}
