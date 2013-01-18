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

@RunWith(Arquillian.class)
public class SuspendResumeCommitTransactionTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testSuspendResumeCommitTransaction()
            throws Exception
            {
        UserTransaction ut = UserTransaction.getUserTransaction();
        TransactionManager tm = TransactionManager.getTransactionManager();

        ut.begin();

        TxContext ctx = tm.suspend();

        System.out.println("Suspended: "+ctx);

        tm.resume(ctx);

        System.out.println("\nResumed");

        ut.commit();
            }
}
