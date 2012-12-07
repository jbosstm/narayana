package com.arjuna.wst11.tests.arq.ba;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wst.tests.common.DemoBusinessParticipant;
import com.arjuna.wst11.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class CloseTest {
	@Inject
	Close test;
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				Close.class,
				DemoBusinessParticipant.class,
                ParticipantCompletionCoordinatorRules.class);
	}

    @BeforeClass()
    public static void submitBytemanScript() throws Exception {
        BMScript.submit(ParticipantCompletionCoordinatorRules.RESOURCE_PATH);
    }

    @AfterClass()
    public static void removeBytemanScript() {
        BMScript.remove(ParticipantCompletionCoordinatorRules.RESOURCE_PATH);
    }
	
	@Test
	public void test() throws Exception {
        ParticipantCompletionCoordinatorRules.setParticipantCount(1);
		test.testClose();
	}
}
