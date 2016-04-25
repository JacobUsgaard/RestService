package usgaard.jacob.rest;

import java.beans.IntrospectionException;
import java.util.List;
import java.util.Map;

import usgaard.jacob.rest.exception.ConversionException;

public interface FieldMapper<Identifier> {
	public enum Sort {
		ASCENDING, DESCENDING;
	}

	public <Id, Op, Val> Map<Identifier, Sort> generateFields(Class<?> clazz,
			List<ParameterMapping<Id, Op, Val>> parameterMappings, TypeGenerator typeGenerator,
			Id fieldParameterIdentifier) throws ConversionException, IntrospectionException;

}
