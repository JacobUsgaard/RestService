package usgaard.jacob.rest.request;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestRequest {
	public enum Sort {
		ASCENDING, DESCENDING;
	}

	private Class<?> mappedObject;
	private Sort sort = Sort.ASCENDING;
	private int start = 0;
	private int limit = 10;
	private Map<PropertyDescriptor, List<Object>> searchValues = new HashMap<>();
	private Map<PropertyDescriptor, Sort> fields = new HashMap<>();

	public Class<?> getMappedObject() {
		return mappedObject;
	}

	public void setMappedObject(Class<?> mappedObject) {
		this.mappedObject = mappedObject;
	}

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

	public Map<PropertyDescriptor, List<Object>> getSearchValues() {
		return searchValues;
	}

	public void setSearchValues(Map<PropertyDescriptor, List<Object>> searchValues) {
		this.searchValues = searchValues;
	}

	public Map<PropertyDescriptor, Sort> getFields() {
		return fields;
	}

	public void setFields(Map<PropertyDescriptor, Sort> fields) {
		this.fields = fields;
	}
}
