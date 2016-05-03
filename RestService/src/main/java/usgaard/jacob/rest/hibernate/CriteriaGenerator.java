package usgaard.jacob.rest.hibernate;

import org.hibernate.Criteria;
import org.hibernate.Session;

import usgaard.jacob.rest.request.RestRequest;

public interface CriteriaGenerator<Identifier, Operator, Value> {
	public Criteria generateCriteria(Session session, RestRequest<Identifier, Operator, Value> restRequest);
}
