package usgaard.jacob.rest;

import java.beans.IntrospectionException;
import java.util.List;

import usgaard.jacob.rest.exception.ConversionException;
import usgaard.jacob.rest.request.FieldMapping;
import usgaard.jacob.rest.request.ParameterMapping;

public interface FieldMapper<Identifier> {

	public <Id, Op, Val> List<FieldMapping<Identifier>> generateFieldMappings(Class<?> clazz,
			List<ParameterMapping<Id, Op, Val>> parameterMappings, TypeGenerator typeGenerator,
			Id fieldParameterIdentifier) throws ConversionException, IntrospectionException;

}
