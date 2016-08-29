package usgaard.jacob.rest;

import java.beans.IntrospectionException;
import java.util.List;

import usgaard.jacob.rest.exception.ConversionException;
import usgaard.jacob.rest.request.OrderMapping;
import usgaard.jacob.rest.request.ParameterMapping;

/**
 * A simple mapper that creates a list of mappings between identifiers and
 * orders.
 * 
 * @author Jacob
 *
 * @param <Identifier>
 */
public interface OrderMapper<Identifier> {
	public enum Sort {
		ASCENDING, DESCENDING;
	}

	/**
	 * 
	 * @param clazz
	 * @param parameterMappings
	 * @param typeGenerator
	 * @param fieldParameterIdentifier
	 * @return
	 * @throws IntrospectionException
	 * @throws ConversionException
	 */
	public <Id, Op, Val> List<OrderMapping<Identifier>> generateOrderMappings(Class<?> clazz,
			List<ParameterMapping<Id, Op, Val>> parameterMappings, TypeGenerator typeGenerator,
			Id fieldParameterIdentifier) throws IntrospectionException, ConversionException;
}
