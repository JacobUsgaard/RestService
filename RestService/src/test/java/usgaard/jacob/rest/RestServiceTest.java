package usgaard.jacob.rest;

import static org.junit.Assert.fail;

import java.beans.PropertyDescriptor;
import java.util.List;

import javax.servlet.ServletRequest;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usgaard.jacob.rest.domain.MockObject;
import usgaard.jacob.rest.domain.MockServletRequest;
import usgaard.jacob.rest.request.RestRequest;

public class RestServiceTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestServiceTest.class);

	@Test
	public void testConvertStringClassOfT() {
		RestService restService = new RestService();
		RestRequest<PropertyDescriptor, RestService.Operator, Object> restRequest = null;

		try {
			restRequest = restService.convert("age>=10&name=Jacob Usgaard&fields=age+,name-&pi=3.14&limit=999",
					MockObject.class);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		Assert.assertNotNull(restRequest);
		Assert.assertNotNull(restRequest.getSearchCriteria());
		Assert.assertEquals(3, restRequest.getSearchCriteria().size());
		Assert.assertNotNull(restRequest.getFieldMappings());
		Assert.assertEquals(2, restRequest.getFieldMappings().size());
		Assert.assertEquals(999, restRequest.getLimit());
	}

	@Test
	public void testConvertServletRequestClassOfT() {
		ServletRequest servletRequest = new MockServletRequest();
		RestService restService = new RestService();
		RestRequest<PropertyDescriptor, RestService.Operator, Object> restRequest = null;

		try {
			restRequest = restService.convert(servletRequest, MockObject.class);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		Assert.assertNotNull(restRequest);
		Assert.assertNotNull(restRequest.getSearchCriteria());
		Assert.assertEquals(2, restRequest.getSearchCriteria().size());
		Assert.assertNotNull(restRequest.getFieldMappings());
		Assert.assertEquals(2, restRequest.getFieldMappings().size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDoAll() {
		RestService restService = new RestService();
		SessionFactory sessionFactory = this.buildSessionFactory();
		List<MockObject> mockObjects = null;
		Session session = sessionFactory.openSession();

		try {
			session.beginTransaction();
			session.saveOrUpdate(new MockObject("Jacob Usgaard", 25, 3.14f));
			session.getTransaction().commit();

			session.beginTransaction();
			mockObjects = session.createQuery("from MockObject").list();

			LOGGER.debug("objects found: {}", mockObjects.size());

			for (Object mockObject : mockObjects) {
				LOGGER.debug(mockObject.toString());
			}

			session.getTransaction().commit();
			session.beginTransaction();
			mockObjects = restService.convert("age>=10&fields=age+,name-,pi+&limit=999", MockObject.class, session);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();

			fail();
		} finally {
			session.close();
			sessionFactory.close();
		}

		LOGGER.debug("objects found: {}", mockObjects.size());

		for (Object mockObject : mockObjects) {
			LOGGER.debug(mockObject.toString());
		}
	}

	private SessionFactory buildSessionFactory() {
		Configuration configuration = new Configuration().configure();
		configuration.addAnnotatedClass(MockObject.class);
		StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
		serviceRegistryBuilder.applySettings(configuration.getProperties());
		ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();
		return configuration.buildSessionFactory(serviceRegistry);
	}

}
