package com.arjuna.wstx11.tests.arq.basic;

import javax.inject.Inject;

import com.arjuna.wstx11.tests.arq.WarDeployment;
import com.arjuna.wstx11.tests.arq.basic.SubtransactionCommit;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wstx.tests.common.DemoDurableParticipant;
import com.arjuna.wstx.tests.common.DemoVolatileParticipant;
import com.arjuna.wstx11.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class SubtransactionCommitTest {
	@Inject
    SubtransactionCommit test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
                SubtransactionCommit.class,
                DemoDurableParticipant.class,
                DemoVolatileParticipant.class);
	}
	
	@Test
	public void test() throws Exception {
		test.testSubTransactionCommit();
	}
}
