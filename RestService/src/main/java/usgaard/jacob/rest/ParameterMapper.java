package usgaard.jacob.rest;

import java.util.List;

/**
 * @author Jacob
 *
 */
public abstract class ParameterMapper<Identifier, Operator, Value> {
	private Identifier startParameterIdentifier;
	private Identifier limitParameterIdentifier;
	private Identifier fieldsParameterIdentifier;

	public abstract List<ParameterMapping<Identifier, Operator, Value>> generateParameterMappings(Object source);

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

}
