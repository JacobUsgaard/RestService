package usgaard.jacob.rest.request;

public class SearchCriterion<Identifier, Operator, Value> {
	private Identifier identifier;
	private Operator operator;
	private Value value;

	public SearchCriterion() {
		super();
	}

	public SearchCriterion(Identifier identifier, Operator operator, Value value) {
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
