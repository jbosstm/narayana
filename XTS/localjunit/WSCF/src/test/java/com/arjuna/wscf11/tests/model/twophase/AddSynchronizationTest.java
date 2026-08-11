package com.arjuna.wscf11.tests.model.twophase;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wscf11.tests.WarDeployment;

@RunWith(Arquillian.class)
public class AddSynchronizationTest {
	@Inject
	AddSynchronization test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(AddSynchronization.class);
	}
	
	@Test
	public void testAddSynchronization() throws Exception {
		test.testAddSynchronization();
	}
}
