package com.arjuna.wstx11.tests.arq.basic;

import javax.inject.Inject;

import com.arjuna.wstx11.tests.arq.basic.SuspendNullTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wstx11.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class SuspendNullTransactionTest {
	@Inject
    SuspendNullTransaction test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(SuspendNullTransaction.class);
	}
	
	@Test
	public void test() throws Exception {
		test.testSuspendNullTransaction();
	}
}
