package usgaard.jacob.rest;

import java.beans.IntrospectionException;
import java.util.List;

import usgaard.jacob.rest.exception.ConversionException;
import usgaard.jacob.rest.request.OrderMapping;
import usgaard.jacob.rest.request.ParameterMapping;

public interface OrderMapper<Identifier> {
	public enum Sort {
		ASCENDING, DESCENDING;
	}

	public <Id, Op, Val> List<OrderMapping<Identifier>> generateOrderMappings(Class<?> clazz,
			List<ParameterMapping<Id, Op, Val>> parameterMappings, TypeGenerator typeGenerator,
			Id fieldParameterIdentifier) throws IntrospectionException, ConversionException;
}
