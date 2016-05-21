package usgaard.jacob.rest.request;

public class FieldMapping<Identifier> {
	private Identifier identifier;

	public FieldMapping() {
		super();
	}

	public FieldMapping(Identifier identifier) {
		super();
		this.identifier = identifier;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

}
