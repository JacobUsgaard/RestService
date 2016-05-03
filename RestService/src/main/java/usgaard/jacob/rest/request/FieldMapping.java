package usgaard.jacob.rest.request;

import usgaard.jacob.rest.FieldMapper.Sort;

public class FieldMapping<Identifier> {
	private Identifier identifier;
	private Sort sort;

	public FieldMapping() {
		super();
	}

	public FieldMapping(Identifier identifier, Sort sort) {
		super();
		this.identifier = identifier;
		this.sort = sort;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	public Sort getSort() {
		return sort;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}
}
