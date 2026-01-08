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
import com.arjuna.wst11.tests.arq.TestCompletionCoordinatorProcessor.CompletionCoordinatorDetails;

@RunWith(Arquillian.class)
public class CompletionParticipantTest {
	@Inject
	CompletionParticipantTestCase test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				CompletionParticipantTestCase.class,
				TestCompletionCoordinatorProcessor.class,
				CompletionCoordinatorDetails.class);
	}
	
	@Before
	public void setUp() throws Exception{
		test.setUp();
	}
	
	@Test
	public void testSendCommit() throws Exception {
		test.testSendCommit();
		
	}
	
	@Test
	public void testSendRollback() throws Exception {
		test.testSendRollback();
	}
	
	@After
	public void tearDown() throws Exception {
		test.tearDown();
	}
}
