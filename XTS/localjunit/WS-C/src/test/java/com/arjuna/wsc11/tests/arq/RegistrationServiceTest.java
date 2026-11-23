package com.arjuna.wsc11.tests.arq;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wsc11.tests.WarDeployment;

@RunWith(Arquillian.class)
public class RegistrationServiceTest {
	@Inject
	RegistrationService test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(RegistrationService.class);
	}
	
	@Test
	public void testKnownCoordinationType() throws Exception {
		test.testKnownCoordinationType();
	}
	
	@Test
	public void testUnknownCoordinationType() throws Exception {
		test.testUnknownCoordinationType();
	}
}
