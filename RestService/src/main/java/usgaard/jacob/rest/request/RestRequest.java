package usgaard.jacob.rest.request;

import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper class for information contained in the original request.
 * 
 * @author Jacob
 *
 */
public class RestRequest<Identifier, Operator, Value> {

	private Class<?> rootClass;
	private int start = 0;
	private int limit = 10;
	private List<FieldMapping<Identifier>> fieldMappings = new LinkedList<FieldMapping<Identifier>>();
	private List<SearchCriterion<Identifier, Operator, Value>> searchCriteria = new LinkedList<SearchCriterion<Identifier, Operator, Value>>();
	private List<OrderMapping<Identifier>> orderMappings = new LinkedList<OrderMapping<Identifier>>();

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

	public List<FieldMapping<Identifier>> getFieldMappings() {
		return fieldMappings;
	}

	public void setFieldMappings(List<FieldMapping<Identifier>> fieldMappings) {
		this.fieldMappings = fieldMappings;
	}

	public List<SearchCriterion<Identifier, Operator, Value>> getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(List<SearchCriterion<Identifier, Operator, Value>> searchCriteria) {
		this.searchCriteria = searchCriteria;
	}

	public Class<?> getRootClass() {
		return rootClass;
	}

	public void setRootClass(Class<?> rootClass) {
		this.rootClass = rootClass;
	}

	public List<OrderMapping<Identifier>> getOrderMappings() {
		return orderMappings;
	}

	public void setOrderMappings(List<OrderMapping<Identifier>> orderMappings) {
		this.orderMappings = orderMappings;
	}
}