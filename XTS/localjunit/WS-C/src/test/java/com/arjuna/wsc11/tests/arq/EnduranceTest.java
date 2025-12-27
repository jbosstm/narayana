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
import com.arjuna.wsc11.tests.arq.TestRegistrationCoordinatorProcessor.RegisterDetails;

@RunWith(Arquillian.class)
public class EnduranceTest {
	@Inject
	Endurance test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				Endurance.class,
				TestActivationCoordinatorProcessor.class,
				TestRegistrationCoordinatorProcessor.class,
				CreateCoordinationContextDetails.class,
				RegisterDetails.class);
	}
	
	@Before
	 public void setUp() throws Exception {
		test.setUp();
	}
	
	@Test
	public void testCreateCoordinationContextRequest() throws Exception {
		test.testCreateCoordinationContextRequest();
	}
	
	@Test
	 public void testCreateCoordinationContextError() throws Exception {
		test.testCreateCoordinationContextError();
	}
	
	@Test
	public void testRegisterRequest() throws Exception {
		test.testRegisterRequest();
	}
	
	@Test
	public void testRegisterError() throws Exception {
		test.testRegisterError();
	}
	
	@Test
	 public void testEachInTurn() throws Exception {
		test.testEachInTurn();
	}
	
	@After
	public void tearDown() throws Exception {
		test.tearDown();
	}
}