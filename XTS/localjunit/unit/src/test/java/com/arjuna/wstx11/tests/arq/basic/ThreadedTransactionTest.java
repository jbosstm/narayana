package com.arjuna.wstx11.tests.arq.basic;

import javax.inject.Inject;

import com.arjuna.wstx11.tests.arq.basic.ThreadedObject;
import com.arjuna.wstx11.tests.arq.basic.ThreadedTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wstx11.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class ThreadedTransactionTest {
	@Inject
    ThreadedTransaction test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				ThreadedTransaction.class,
				ThreadedObject.class);
	}
	
	@Test
	public void test() throws Exception {
		test.testThreadedTransaction();
	}
}
