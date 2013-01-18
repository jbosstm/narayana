package com.arjuna.wstx.tests.arq.basic;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.wstx.tests.arq.WarDeployment;

@RunWith(Arquillian.class)
public class ResumeNullTransactionTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testResumeNullTransaction()
            throws Exception
            {
        TransactionManager ut = TransactionManager.getTransactionManager();

        ut.resume(null);
            }
}
