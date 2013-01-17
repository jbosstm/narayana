package com.arjuna.wstx11.tests.arq.basic;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.wstx.tests.common.DemoDurableParticipant;
import com.arjuna.wstx11.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class MultiParticipantsTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoDurableParticipant.class);
    }

    @Test
    public void testMultiParticipants()
            throws Exception
            {
        UserTransaction ut = UserTransaction.getUserTransaction();
        TransactionManager tm = TransactionManager.getTransactionManager();
        DemoDurableParticipant p1 = new DemoDurableParticipant();
        DemoDurableParticipant p2 = new DemoDurableParticipant();
        DemoDurableParticipant p3 = new DemoDurableParticipant();
        DemoDurableParticipant p4 = new DemoDurableParticipant();

        ut.begin();
        try {
            tm.enlistForDurableTwoPhase(p1, p1.identifier());
            tm.enlistForDurableTwoPhase(p2, p2.identifier());
            tm.enlistForDurableTwoPhase(p3, p3.identifier());
            tm.enlistForDurableTwoPhase(p4, p4.identifier());
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
