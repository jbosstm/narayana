package com.arjuna.wstx.tests.arq.ba;

import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.wstx.tests.arq.WarDeployment;
import com.arjuna.wstx.tests.common.DemoBusinessParticipant;
import com.arjuna.wstx.tests.common.FailureBusinessParticipant;

@RunWith(Arquillian.class)
public class MultiCloseTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoBusinessParticipant.class,
                FailureBusinessParticipant.class,
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
    public void testMultiClose()
            throws Exception
            {

        ParticipantCompletionCoordinatorRules.setParticipantCount(2);

        UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();

        BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
        com.arjuna.wst11.BAParticipantManager bpm1 = null;
        com.arjuna.wst11.BAParticipantManager bpm2 = null;
        DemoBusinessParticipant p = new DemoBusinessParticipant(DemoBusinessParticipant.CLOSE, "1240");
        FailureBusinessParticipant fp = new FailureBusinessParticipant(FailureBusinessParticipant.FAIL_IN_CLOSE, "5679");

        try {
            uba.begin();

            bpm1 = bam.enlistForBusinessAgreementWithParticipantCompletion(p, "1240");
            bpm2 = bam.enlistForBusinessAgreementWithParticipantCompletion(fp, "5679");
            bpm1.completed();
            bpm2.completed();
        } catch (Exception eouter) {
            try {
                uba.cancel();
            } catch(Exception einner) {
            }
            throw eouter;
        }
        uba.close();
        assertTrue(p.passed());
            }
}
