package com.arjuna.wst11.tests.arq.basic;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wst.tests.common.DemoDurableParticipant;
import com.arjuna.wst.tests.common.FailureParticipant;
import com.arjuna.wst11.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class RollbackExceptionInRollbackTest {
	@Inject
	RollbackExceptionInRollback test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				RollbackExceptionInRollback.class,
				DemoDurableParticipant.class,
				FailureParticipant.class);
	}
	
	@Test
	public void test() throws Exception {
		test.testRollbackExceptionInRollback();
	}
}
