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

@RunWith(Arquillian.class)
public class CancelTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoBusinessParticipant.class);
    }

    @Test
    public void testCancel()
            throws Exception
            {
        UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();
        BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
        String participantId = "1234";
        DemoBusinessParticipant p = new DemoBusinessParticipant(DemoBusinessParticipant.CANCEL, participantId);
        try {

            uba.begin();

            bam.enlistForBusinessAgreementWithParticipantCompletion(p, participantId);
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
