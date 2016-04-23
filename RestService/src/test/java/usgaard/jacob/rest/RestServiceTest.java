package usgaard.jacob.rest;

import static org.junit.Assert.fail;

import java.beans.PropertyDescriptor;

import javax.servlet.ServletRequest;

import org.junit.Assert;
import org.junit.Test;

import usgaard.jacob.rest.domain.MockObject;
import usgaard.jacob.rest.domain.MockServletRequest;
import usgaard.jacob.rest.request.RestRequest;

public class RestServiceTest {

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
		Assert.assertNotNull(restRequest.getFields());
		Assert.assertEquals(2, restRequest.getFields().size());
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
		Assert.assertNotNull(restRequest.getFields());
		Assert.assertEquals(2, restRequest.getFields().size());
	}

}
