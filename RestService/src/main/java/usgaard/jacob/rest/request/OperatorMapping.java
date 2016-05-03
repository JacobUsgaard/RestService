package usgaard.jacob.rest.request;

import java.util.regex.Pattern;

import usgaard.jacob.rest.RestService.Operator;

public class OperatorMapping {
	private Pattern pattern;
	private Operator operator;

	public OperatorMapping() {
		super();
	}

	public OperatorMapping(Pattern pattern, Operator operator) {
		super();
		this.pattern = pattern;
		this.operator = operator;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

}
