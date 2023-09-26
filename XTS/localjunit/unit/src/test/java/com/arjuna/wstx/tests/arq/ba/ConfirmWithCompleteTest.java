/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wstx.tests.arq.ba;

import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.wstx.tests.arq.WarDeployment;
import com.arjuna.wstx.tests.common.DemoBusinessParticipant;
import com.arjuna.wstx.tests.common.DemoBusinessParticipantWithComplete;

@RunWith(Arquillian.class)
public class ConfirmWithCompleteTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoBusinessParticipant.class,
                DemoBusinessParticipantWithComplete.class);
    }


    @Test
    public void testConfirmWithComplete()
            throws Exception
            {
        UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();
        BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
        DemoBusinessParticipantWithComplete p = new DemoBusinessParticipantWithComplete(DemoBusinessParticipantWithComplete.COMPLETE, "1234");
        try {
            uba.begin();

            bam.enlistForBusinessAgreementWithCoordinatorCompletion(p, "1237");

            uba.complete();
        } catch (Exception eouter) {
            try {
                uba.cancel();
            } catch(Exception einner) {
            }
            throw eouter;
        }

        uba.cancel();

        assertTrue(p.passed());
            }
}
