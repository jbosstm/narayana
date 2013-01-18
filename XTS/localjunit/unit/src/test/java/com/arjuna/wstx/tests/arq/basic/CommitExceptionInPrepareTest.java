package com.arjuna.wstx.tests.arq.basic;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.wstx.tests.arq.WarDeployment;
import com.arjuna.wstx.tests.common.DemoDurableParticipant;
import com.arjuna.wstx.tests.common.FailureParticipant;

@RunWith(Arquillian.class)
public class CommitExceptionInPrepareTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                DemoDurableParticipant.class,
                FailureParticipant.class);
    }

    @Test
    public void testCommitExceptionInPrepare()
            throws Exception
            {
        UserTransaction ut = UserTransaction.getUserTransaction();
        try
        {
            TransactionManager tm = TransactionManager.getTransactionManager();
            FailureParticipant p1 = new FailureParticipant(FailureParticipant.FAIL_IN_PREPARE, FailureParticipant.WRONG_STATE);
            DemoDurableParticipant p2 = new DemoDurableParticipant();

            ut.begin();

            tm.enlistForDurableTwoPhase(p1, "failure");
            tm.enlistForDurableTwoPhase(p2, p2.identifier());
        } catch (Exception eouter) {
            try {
                ut.rollback();
            } catch(Exception einner) {
            }
            throw eouter;
        }
        try {
            ut.commit();

            fail("expected SystemException");
        }
        catch (com.arjuna.wst.SystemException ex)
        {
            // we should arrive here
        }
        catch (com.arjuna.wst.TransactionRolledBackException ex)
        {
            // or if not we should arrive here
        }
            }
}
