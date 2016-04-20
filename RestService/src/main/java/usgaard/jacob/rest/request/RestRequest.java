package usgaard.jacob.rest.request;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jacob
 *
 */
public class RestRequest {
	public enum Sort {
		ASCENDING, DESCENDING;
	}

	public enum Operator {
		EQUAL, GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL;
	}

	private Sort sort = Sort.ASCENDING;
	private int start = 0;
	private int limit = 10;
	private Map<PropertyDescriptor, Sort> fields = new HashMap<PropertyDescriptor, Sort>();
	private List<SearchCriterion> searchCriteria = new LinkedList<SearchCriterion>();

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

	public Map<PropertyDescriptor, Sort> getFields() {
		return fields;
	}

	public void setFields(Map<PropertyDescriptor, Sort> fields) {
		this.fields = fields;
	}

	public List<SearchCriterion> getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(List<SearchCriterion> searchCriteria) {
		this.searchCriteria = searchCriteria;
	}

}
