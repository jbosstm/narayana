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
public class ExitTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoBusinessParticipant.class);
    }

    @Test
    public void testExit()
            throws Exception
            {
        UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();
        BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
        com.arjuna.wst11.BAParticipantManager bpm = null;
        DemoBusinessParticipant p = new DemoBusinessParticipant(DemoBusinessParticipant.EXIT, "1238");
        try {
            uba.begin();

            bpm = bam.enlistForBusinessAgreementWithParticipantCompletion(p, "1238");

            bpm.exit();
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
