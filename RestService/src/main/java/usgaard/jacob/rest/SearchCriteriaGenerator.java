package usgaard.jacob.rest;

import java.beans.IntrospectionException;
import java.util.List;

import usgaard.jacob.rest.exception.ConversionException;
import usgaard.jacob.rest.request.ParameterMapping;
import usgaard.jacob.rest.request.SearchCriterion;

public interface SearchCriteriaGenerator<Identifier, Operator, Value> {
	public <Id, Op, Val> List<SearchCriterion<Identifier, Operator, Value>> generateSearchCriteria(Class<?> clazz,
			List<ParameterMapping<Id, Op, Val>> parameterMappings, TypeGenerator typeGenerator)
			throws ConversionException, IntrospectionException;
}
