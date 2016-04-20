package usgaard.jacob.rest;

import static org.junit.Assert.fail;

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
		RestRequest restRequest = null;

		try {
			restRequest = restService.convert("age=10&name=Jacob Usgaard&fields=age+,name-", MockObject.class);
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

	@Test
	public void testConvertServletRequestClassOfT() {
		ServletRequest servletRequest = new MockServletRequest();
		RestService restService = new RestService();
		RestRequest restRequest = null;

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
