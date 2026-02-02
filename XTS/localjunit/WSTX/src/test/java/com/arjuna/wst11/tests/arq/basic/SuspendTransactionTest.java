package com.arjuna.wst11.tests.arq.basic;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wst11.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class SuspendTransactionTest {
	@Inject
	SuspendTransaction test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(SuspendTransaction.class);
	}
	
	@Test
	public void test() throws Exception {
		test.testSuspendTransaction();
	}
}
