package usgaard.jacob.rest.request;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import usgaard.jacob.rest.FieldMapper.Sort;

/**
 * @author Jacob
 *
 */
public class RestRequest<Identifier, Operator, Value> {

	private Sort sort = Sort.ASCENDING;
	private int start = 0;
	private int limit = 10;
	private Map<Identifier, Sort> fields = new HashMap<Identifier, Sort>();
	private List<SearchCriterion<Identifier, Operator, Value>> searchCriteria = new LinkedList<SearchCriterion<Identifier, Operator, Value>>();

	public Sort getSort() {
		return sort;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public Map<Identifier, Sort> getFields() {
		return fields;
	}

	public void setFields(Map<Identifier, Sort> fields) {
		this.fields = fields;
	}

	public List<SearchCriterion<Identifier, Operator, Value>> getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(List<SearchCriterion<Identifier, Operator, Value>> searchCriteria) {
		this.searchCriteria = searchCriteria;
	}
}