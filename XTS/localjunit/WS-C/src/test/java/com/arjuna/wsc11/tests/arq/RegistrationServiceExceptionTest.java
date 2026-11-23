package com.arjuna.wsc11.tests.arq;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wsc11.tests.WarDeployment;

@RunWith(Arquillian.class)
public class RegistrationServiceExceptionTest {
	@Inject
	RegistrationServiceException test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(RegistrationServiceException.class);
	}
	
	@Test
	public void testAlreadyRegisteredProtocolIdentifierException() throws Exception {
		test.testAlreadyRegisteredProtocolIdentifierException();
	}
	
	@Test
	public void testInvalidProtocolProtocolIdentifierException() throws Exception {
		test.testInvalidProtocolProtocolIdentifierException();
	}
	
	@Test
	public void testInvalidStateProtocolIdentifierException() throws Exception {
		test.testInvalidStateProtocolIdentifierException();
	}
	
	@Test
	public void testNoActivityProtocolIdentifierException() throws Exception {
		test.testNoActivityProtocolIdentifierException();
	}
}
