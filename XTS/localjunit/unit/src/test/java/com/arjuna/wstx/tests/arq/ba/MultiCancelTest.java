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
import com.arjuna.wst.SystemException;
import com.arjuna.wstx.tests.arq.WarDeployment;
import com.arjuna.wstx.tests.common.DemoBusinessParticipant;
import com.arjuna.wstx.tests.common.FailureBusinessParticipant;

@RunWith(Arquillian.class)
public class MultiCancelTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoBusinessParticipant.class,
                FailureBusinessParticipant.class);
    }

    @Test
    public void testMultiCancel()
            throws Exception
            {
        UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();
        BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
        DemoBusinessParticipant p = new DemoBusinessParticipant(DemoBusinessParticipant.CANCEL, "1239");
        FailureBusinessParticipant fp = new FailureBusinessParticipant(FailureBusinessParticipant.FAIL_IN_CANCEL, "5678");
        try {
            uba.begin();

            bam.enlistForBusinessAgreementWithParticipantCompletion(p, "1239");
            bam.enlistForBusinessAgreementWithParticipantCompletion(fp, "5678");
        } catch (Exception eouter) {
            try {
                uba.cancel();
            } catch(Exception einner) {
            }
            throw eouter;
        }
        try {
            uba.cancel();
        } catch (SystemException ex) {
            // failure will result in heuristic hazard warning message but wil not throw an exception
            throw ex;
        } catch (Exception eouter) {
            try {
                uba.cancel();
            } catch(Exception einner) {
            }
            throw eouter;
        }
        assertTrue(p.passed());
            }
}
