package org.jboss.jbossts.qa.astests.crash;

import org.jboss.jbossts.qa.astests.recovery.ASFailureSpec;
import org.jboss.jbossts.qa.astests.recovery.TestASRecovery;

import javax.ejb.SessionContext;
import javax.ejb.SessionBean;
import javax.transaction.Transaction;

public class CrashCMTBean implements SessionBean {
    private SessionContext context;

    public void setSessionContext(SessionContext context) { this.context = context; }
    public void ejbCreate() { }
    public void ejbActivate() { }
    public void ejbPassivate() { }
    public void ejbRemove() { }

    public String testXA(String ... args)
    {
        return "Passed";
    }

    public String testXA(ASFailureSpec... specs)
    {
        System.out.println("CMT testXA called with " + specs.length + " specs");

        TestASRecovery xatest = new TestASRecovery();
        Transaction tx;

        try
        {
            tx = com.arjuna.ats.jta.TransactionManager.transactionManager().getTransaction();
        }
        catch (javax.transaction.SystemException e)
        {
            tx = null;
        }

        if (tx == null)
        {
            System.out.println("CMT testXA called without a transaction");

            return "Failed";
        }
        else
        {
            for (ASFailureSpec spec : specs)
                xatest.addResource(spec);

            xatest.startTest(tx);

            return "Passed";
        }
    }
}
