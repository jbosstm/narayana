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
import com.arjuna.wsc11.tests.arq.TestRegistrationCoordinatorProcessor.RegisterDetails;

@RunWith(Arquillian.class)
public class RegistrationTest {
	@Inject
	Registration test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				Registration.class,
				TestRegistrationCoordinatorProcessor.class,
				RegisterDetails.class);
	}
	
	@Before
	 public void setUp() throws Exception {
		test.setUp();
	}
	
	@Test
	public void testRequestWithoutInstanceIdentifier() throws Exception {
		test.testRequestWithoutInstanceIdentifier();
	}
	
	@Test
	public void testRequestWithInstanceIdentifier() throws Exception {
		test.testRequestWithInstanceIdentifier();
	}
	
	@After
	public void tearDown() throws Exception {
		test.tearDown();
	}
}
