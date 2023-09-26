/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wstx.tests.arq.basic;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.wstx.tests.arq.WarDeployment;
import com.arjuna.wstx.tests.common.DemoDurableParticipant;

@RunWith(Arquillian.class)
public class SuspendResumeSingleParticipantTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoDurableParticipant.class);
    }

    @Test
    public void testSuspendResumeSingleParticipant()
            throws Exception
            {
        UserTransaction ut = UserTransaction.getUserTransaction();
        TransactionManager tm = TransactionManager.getTransactionManager();
        DemoDurableParticipant p = new DemoDurableParticipant();
        try {
            ut.begin();

            tm.enlistForDurableTwoPhase(p, p.identifier());

            TxContext ctx = tm.suspend();

            System.out.println("Suspended: "+ctx);

            tm.resume(ctx);

            System.out.println("\nResumed\n");
        }  catch (Exception eouter) {
            try {
                ut.rollback();
            } catch(Exception einner) {
            }
            throw eouter;
        }
        ut.commit();
            }
}
