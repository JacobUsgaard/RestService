package usgaard.jacob.rest.request;

/**
 * A simple mapping between an identifier, an operator, and a value. There are
 * currently two types of parameter mappings: search criteria and fields.
 * 
 * @author Jacob
 *
 * @param <Identifier>
 * @param <Operator>
 * @param <Value>
 * 
 * @see SearchCriterion
 * @see FieldMapping
 */
public class ParameterMapping<Identifier, Operator, Value> {
	private Identifier identifier;
	private Operator operator;
	private Value value;

	public ParameterMapping() {
		super();
	}

	public ParameterMapping(Identifier identifier, Operator operator, Value value) {
		super();
		this.identifier = identifier;
		this.operator = operator;
		this.value = value;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

}
