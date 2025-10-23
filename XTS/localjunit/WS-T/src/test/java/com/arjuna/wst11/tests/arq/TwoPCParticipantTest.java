package com.arjuna.wst11.tests.arq;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wst11.tests.WarDeployment;
import com.arjuna.wst11.tests.arq.TestCoordinatorProcessor.CoordinatorDetails;

@RunWith(Arquillian.class)
public class TwoPCParticipantTest {
	@Inject
	TwoPCParticipantTestCase test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				TestCoordinatorProcessor.class,
				CoordinatorDetails.class,
				TwoPCParticipantTestCase.class);
	}
	
	@Before
	public void setUp() throws Exception{
		test.setUp();
	}
	
	@Test
	public void testSendCommitted() throws Exception {
		test.testSendCommitted();
	}
	
	@Test
	public void testSendAborted() throws Exception {
		test.testSendAborted();
	}
	
	@Test
	public void testSendError() throws Exception {
		test.testSendError();
	}
	
	@Test
	public void testSendPrepared() throws Exception {
		test.testSendPrepared();
	}
	
	@Test
	public void testSendReadOnly() throws Exception {
		test.testSendReadOnly();
	}
	
	@After
	public void tearDown() throws Exception {
		test.tearDown();
	}
}
