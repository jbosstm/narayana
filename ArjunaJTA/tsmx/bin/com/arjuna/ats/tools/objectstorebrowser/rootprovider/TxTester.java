/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.rootprovider;

import javax.transaction.*;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import java.io.Serializable;

public class TxTester
{
    static DummyTx[] ta = new DummyTx[2];

    public void createTransactions()
    {
        boolean endTx = false;

        for (int i = 0; i < ta.length; i++)
        {
            try
            {
                if (ta[i] == null || ta[i].terminateTxIfNotActive())
                    ta[i] = newTx(endTx);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (endTx)
                    ta[i] = null;
            }
        }
    }

    private DummyTx newTx(boolean commit)
    {
        DummyTx t = new DummyTx();

        t.commit = commit;
        t.start();
        return t;
    }

    private class DummyTx extends Thread
    {
        UserTransaction ut;
        Transaction tx;
        boolean commit;

        private Synchronization getSynchronization() throws SystemException, RollbackException
        {
            return new Synchronization() {
                public void beforeCompletion()
                {
                    System.out.println("Before completion");
                }

                public void afterCompletion(int i)
                {
                    System.out.println("After completion");
                }
            };
        }

        public int getStatus()
        {
            try
            {
                return ut.getStatus();
            }
            catch (SystemException e)
            {
                System.out.println(e.getMessage());
                return Status.STATUS_UNKNOWN;
            }
        }

        public boolean terminateTxIfNotActive()
        {
            if (getStatus() != Status.STATUS_ACTIVE && getStatus() != Status.STATUS_NO_TRANSACTION)
            {
                try
                {
                    ut.rollback();
                }
                catch (Throwable e)
                {
                    System.out.println(e.getMessage());
                }

                return true;
            }

            return false;
        }

        public void run()
        {
//            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            
            ut = com.arjuna.ats.jta.UserTransaction.userTransaction();

            try{
                ut.setTransactionTimeout(3000);
                ut.begin();
                tx = com.arjuna.ats.jta.TransactionManager.transactionManager().getTransaction();
                tx.registerSynchronization(getSynchronization());
                tx.registerSynchronization(getSynchronization());
                tx.enlistResource(new DummyXAResource(false));
                tx.enlistResource(new DummyXAResource(true));
                tx.enlistResource(new DummyXAResource(true));

                if (commit)
                    ut.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
