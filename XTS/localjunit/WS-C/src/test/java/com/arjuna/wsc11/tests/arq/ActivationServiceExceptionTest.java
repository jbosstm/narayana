package com.arjuna.wsc11.tests.arq;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wsc11.tests.WarDeployment;

@RunWith(Arquillian.class)
public class ActivationServiceExceptionTest {
	@Inject
	ActivationServiceException test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(ActivationServiceException.class);
	}
	
	@Test
	public void testInvalidCreateParametersException() throws Exception {
		test.testInvalidCreateParametersException();
	}
}
