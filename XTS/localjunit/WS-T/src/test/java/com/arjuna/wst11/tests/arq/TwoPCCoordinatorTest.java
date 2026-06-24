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
import com.arjuna.wst11.tests.arq.TestParticipantProcessor.ParticipantDetails;

@RunWith(Arquillian.class)
public class TwoPCCoordinatorTest {
	@Inject
	TwoPCCoordinatorTestCase test;

	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				ParticipantDetails.class,
				TestParticipantProcessor.class,
				TwoPCCoordinatorTestCase.class);
	}

	@Before
	public void setUp() throws Exception{
		test.setUp();
	}

	@Test
	public void testSendPrepare() throws Exception {
		test.testSendPrepare();
	}
	
	@Test
	public void testSendCommit() throws Exception {
		test.testSendCommit();
	}
	
	@Test
	public void testSendRollback() throws Exception {
		test.testSendRollback();
	}
	
	@Test
	public void testSendError() throws Exception {
		test.testSendError();
	}

	@After
	public void tearDown() throws Exception {
		test.tearDown();
	}
}
