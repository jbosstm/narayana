package com.arjuna.wstx11.tests.arq.ba;

import javax.inject.Inject;

import com.arjuna.wstx11.tests.arq.ba.MultiCancel;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wstx.tests.common.DemoBusinessParticipant;
import com.arjuna.wstx.tests.common.FailureBusinessParticipant;
import com.arjuna.wstx11.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class MultiCancelTest {
	@Inject
    MultiCancel test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				MultiCancel.class,
				DemoBusinessParticipant.class,
				FailureBusinessParticipant.class);
	}
	
	@Test
	public void test() throws Exception {
		test.testMultiCancel();
	}
}
