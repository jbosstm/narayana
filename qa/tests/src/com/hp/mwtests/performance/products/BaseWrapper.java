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
 * (C) 2008
 * @author JBoss Inc.
 */
package com.hp.mwtests.performance.products;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;

import javax.transaction.*;
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
