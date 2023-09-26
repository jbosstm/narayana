/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.products;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;

import jakarta.transaction.*;
import javax.transaction.xa.XAResource;

/**
 * TODO
 */
abstract public class BaseWrapper
{
    private UserTransaction tx;

    protected void setUserTransaction(UserTransaction tx)
    {
        this.tx = tx;
    }

    abstract public Transaction getTransaction() throws SystemException;

    public int begin()
    {
        try
        {
            tx.begin();
            return ActionStatus.RUNNING;
        }
        catch (SystemException e)
        {
            return ActionStatus.INVALID;
        }
        catch (NotSupportedException e)
        {
            return ActionStatus.INVALID;
        }
    }

    public int commit()
    {
        if (tx == null)
            return ActionStatus.INVALID;

        try
        {
            tx.commit();
            return ActionStatus.COMMITTED;
        }
        catch (HeuristicMixedException e)
        {
            return ActionStatus.H_MIXED;
        }
        catch (HeuristicRollbackException e)
        {
            return ActionStatus.H_ROLLBACK;
        }
        catch (RollbackException e)
        {
            return ActionStatus.ABORTED;
        }
        catch (SystemException e)
        {
            return ActionStatus.INVALID;
        }
    }

    public int abort()
    {
        if (tx == null)
            return ActionStatus.INVALID;

        try
        {
            tx.rollback();
            return ActionStatus.ABORTED;
        }
        catch (SystemException e)
        {
            return ActionStatus.INVALID;
        }
    }

    public int add(XAResource xares)
    {
        try
        {
            getTransaction().enlistResource(xares);
        }
        catch (Exception e)  //RollbackException, IllegalStateException, SystemException
        {
            return AddOutcome.AR_REJECTED;
        }

        return AddOutcome.AR_ADDED;
    }

    protected XAResource toXAResource(AbstractRecord record, boolean verbose)
    {
//        return new com.hp.mwtests.ts.jta.common.DummyXA(verbose);
        return new org.jboss.jbossts.qa.CrashRecovery13Impls.RecoveryXAResource();
    }

    public int add(AbstractRecord record)
    {
        return add(toXAResource(record, false));
    }

}