package com.arjuna.wsc11.tests.arq;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wsc11.tests.WarDeployment;
import com.arjuna.wsc11.tests.arq.TestActivationCoordinatorProcessor.CreateCoordinationContextDetails;


@RunWith(Arquillian.class)
public class ActivationTest {
	@Inject
	Activation test;

	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				Activation.class,
				TestActivationCoordinatorProcessor.class,
				CreateCoordinationContextDetails.class);
	}

	@Before
	public void setUp() throws Exception {
		test.setUp();
	}

	@Test
	public void testRequestWithoutExpiresWithoutCurrentContext() 
		throws Exception {
		test.testRequestWithoutExpiresWithoutCurrentContext();
	}
	
	@Test
	public void testRequestWithExpiresWithoutCurrentContext()
    throws Exception {
		test.testRequestWithExpiresWithoutCurrentContext();
	}
	
	@Test
	public void testRequestWithoutExpiresWithCurrentContextWithoutExpires()
    throws Exception {
		test.testRequestWithoutExpiresWithCurrentContextWithoutExpires();
	}
	
	@Test
	public void testRequestWithoutExpiresWithCurrentContextWithExpires()
    throws Exception {
		test.testRequestWithoutExpiresWithCurrentContextWithExpires();
	}
	
	@Test
	public void testRequestWithExpiresWithCurrentContextWithoutExpires()
    throws Exception {
		test.testRequestWithExpiresWithCurrentContextWithoutExpires();
	}
	
	@Test
	public void testRequestWithExpiresWithCurrentContextWithExpires()
    throws Exception {
		test.testRequestWithExpiresWithCurrentContextWithExpires();
	}
	
	@After
	public void tearDown() throws Exception {
		test.tearDown();
	}
}
