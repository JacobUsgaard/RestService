package usgaard.jacob.rest;

import usgaard.jacob.rest.exception.ConversionException;

/**
 * @author Jacob
 *
 */
public interface TypeGenerator {
	/**
	 * @param clazz
	 * @param object
	 * @return
	 * @throws ConversionException
	 */
	public <T> T generateType(Class<T> clazz, Object object) throws ConversionException;

}
