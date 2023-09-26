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
public class SuspendResumeParticipantsTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoDurableParticipant.class);
    }

    @Test
    public void testSuspendResumeParticipants()
            throws Exception
            {
        UserTransaction ut = UserTransaction.getUserTransaction();
        TransactionManager tm = TransactionManager.getTransactionManager();
        DemoDurableParticipant p1 = new DemoDurableParticipant();
        DemoDurableParticipant p2 = new DemoDurableParticipant();
        DemoDurableParticipant p3 = new DemoDurableParticipant();
        DemoDurableParticipant p4 = new DemoDurableParticipant();

        System.out.println("Starting first transaction.\n");

        ut.begin();
        try {
            tm.enlistForDurableTwoPhase(p1, p1.identifier());
            tm.enlistForDurableTwoPhase(p2, p2.identifier());
            tm.enlistForDurableTwoPhase(p3, p3.identifier());
            tm.enlistForDurableTwoPhase(p4, p4.identifier());

            TxContext ctx = tm.suspend();

            System.out.println("Suspended: "+ctx);

            ut.begin();

            System.out.println("\nStarted second transaction.");

            tm.resume(ctx);

            System.out.println("\nCommitting first transaction.\n");
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
