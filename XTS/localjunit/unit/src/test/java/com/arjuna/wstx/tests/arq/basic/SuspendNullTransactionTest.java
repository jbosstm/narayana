package com.arjuna.wstx.tests.arq.basic;

import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.wstx.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class SuspendNullTransactionTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testSuspendNullTransaction()
            throws Exception
            {
        TransactionManager ut = TransactionManager.getTransactionManager();

        TxContext ctx = ut.suspend();

        System.out.println("Suspended: "+ctx);

        assertTrue(ctx == null);
            }
}
