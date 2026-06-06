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
import com.arjuna.wst11.tests.arq.TestCoordinatorCompletionParticipantProcessor.CoordinatorCompletionParticipantDetails;

@RunWith(Arquillian.class)
public class BusinessAgreementWithCoordinatorCompletionCoordinatorTest {
	@Inject
	BusinessAgreementWithCoordinatorCompletionCoordinatorTestCase test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				CoordinatorCompletionParticipantDetails.class,
				TestCoordinatorCompletionParticipantProcessor.class,
				BusinessAgreementWithCoordinatorCompletionCoordinatorTestCase.class);
	}
	
	@Before
	public void setUp() throws Exception{
		test.setUp();
	}
	
	@Test
	public void testSendCancel() throws Exception {
		test.testSendCancel();
	}
	
	@Test
	public void testSendClose() throws Exception {
		test.testSendClose();
	}
	
	@Test
	public void testSendCompensate() throws Exception {
		test.testSendCompensate();
	}
	
	@Test
	public void testSendComplete() throws Exception {
		test.testSendComplete();
	}
	
	@Test
	public void testSendError() throws Exception {
		test.testSendError();
	}
	
	@Test
	public void testSendExited() throws Exception {
		test.testSendExited();
	}
	
	@Test
	public void testSendFaulted() throws Exception {
		test.testSendFaulted();
	}
	
	@Test
	public void testSendGetStatus() throws Exception {
		test.testSendGetStatus();
	}
	
	@Test
	public void testSendNotCompleted() throws Exception {
		test.testSendNotCompleted();
	}
	
	@Test
	public void testSendStatus() throws Exception {
		test.testSendStatus();
	}
	
	@After
	public void tearDown() throws Exception {
		test.tearDown();
	}
}
