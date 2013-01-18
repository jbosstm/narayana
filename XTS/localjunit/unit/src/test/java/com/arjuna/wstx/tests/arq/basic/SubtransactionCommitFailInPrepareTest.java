package com.arjuna.wstx.tests.arq.basic;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wstx.tests.arq.WarDeployment;
import com.arjuna.wstx.tests.common.DemoDurableParticipant;
import com.arjuna.wstx.tests.common.DemoVolatileParticipant;
import com.arjuna.wstx.tests.common.FailureParticipant;

@RunWith(Arquillian.class)
public class SubtransactionCommitFailInPrepareTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoDurableParticipant.class,
                DemoVolatileParticipant.class,
                FailureParticipant.class);
    }

    @Test
    public void testSubTransactionCommitFailInPrepare()
            throws Exception
            {
        final UserTransaction ut = UserTransactionFactory.userTransaction();
        final UserTransaction ust = UserTransactionFactory.userSubordinateTransaction();
        final TransactionManager tm = TransactionManager.getTransactionManager();

        final DemoDurableParticipant p1 = new DemoDurableParticipant();
        final DemoVolatileParticipant p2 = new DemoVolatileParticipant();
        final FailureParticipant p3 = new FailureParticipant(FailureParticipant.FAIL_IN_PREPARE, FailureParticipant.WRONG_STATE);
        final DemoVolatileParticipant p4 = new DemoVolatileParticipant();

        ut.begin();
        final TxContext tx = tm.suspend();
        tm.resume(tx);
        tm.enlistForDurableTwoPhase(p1, p1.identifier());
        tm.enlistForVolatileTwoPhase(p2, p2.identifier());
        ust.begin();
        final TxContext stx = tm.suspend();
        tm.resume(stx);
        tm.enlistForDurableTwoPhase(p3, "failure in prepare");
        tm.enlistForVolatileTwoPhase(p4, p4.identifier());

        tm.resume(tx);
        try {
            ut.commit();
            fail("expecting TransactionRolledBackException");
        } catch (TransactionRolledBackException wse) {
            // expect this
        }
        assertTrue(p1.prepared() && p1.resolved() && !p1.passed());
        assertTrue(p2.prepared() && p2.resolved() && !p2.passed());
        assertTrue(!p3.passed());
        assertTrue(p4.prepared() && p4.resolved() && !p4.passed());
            }
}
