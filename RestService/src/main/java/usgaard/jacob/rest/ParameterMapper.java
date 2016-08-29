package usgaard.jacob.rest;

import java.util.List;

import usgaard.jacob.rest.exception.ParameterException;
import usgaard.jacob.rest.request.ParameterMapping;

/**
 * @author Jacob
 *
 */
public abstract class ParameterMapper<Identifier, Operator, Value> {
	protected Identifier startParameterIdentifier;
	protected Identifier limitParameterIdentifier;
	protected Identifier fieldsParameterIdentifier;
	protected Identifier orderParameterIdentifier;

	public abstract List<ParameterMapping<Identifier, Operator, Value>> generateParameterMappings(Object source)
			throws ParameterException;

	public Identifier getStartParameterIdentifier() {
		return startParameterIdentifier;
	}

	public void setStartParameterIdentifier(Identifier startParameterIdentifier) {
		this.startParameterIdentifier = startParameterIdentifier;
	}

	public Identifier getLimitParameterIdentifier() {
		return limitParameterIdentifier;
	}

	public void setLimitParameterIdentifier(Identifier limitParameterIdentifier) {
		this.limitParameterIdentifier = limitParameterIdentifier;
	}

	public Identifier getFieldsParameterIdentifier() {
		return fieldsParameterIdentifier;
	}

	public void setFieldsParameterIdentifier(Identifier fieldsParameterIdentifier) {
		this.fieldsParameterIdentifier = fieldsParameterIdentifier;
	}

	public Identifier getOrderParameterIdentifier() {
		return orderParameterIdentifier;
	}

	public void setOrderParameterIdentifier(Identifier orderParameterIdentifier) {
		this.orderParameterIdentifier = orderParameterIdentifier;
	}

}
