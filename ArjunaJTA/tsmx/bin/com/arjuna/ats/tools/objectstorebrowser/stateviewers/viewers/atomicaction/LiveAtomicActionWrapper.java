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
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jta.transaction.Transaction;
import com.arjuna.ats.internal.jta.xa.TxInfo;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.UidInfo;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.SynchronizationInfo;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.XAResourceInfo;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.ArjunaTransactionWrapper;

import javax.transaction.xa.XAResource;
import javax.transaction.SystemException;

import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

public class LiveAtomicActionWrapper extends ArjunaTransactionWrapper //AtomicActionWrapper
{
    private UidInfo uidInfo;
    private Transaction delegate;
    private Collection<SynchronizationInfo> synchronizations;
    private Collection<XAResourceInfo> resources;

    public LiveAtomicActionWrapper(Transaction delegate, Uid objUid, String type)
    {
        super(objUid, type);
        uidInfo = new UidInfo(objUid, getClass().getName() + "@" + Integer.toHexString(hashCode()));
        this.delegate = delegate;
    }

    public UidInfo getUidInfo()
    {
        return uidInfo;
    }

//    public Object getDelegate()
//    {
//        return delegate;
//    }

    public int getTxTimeout()
    {
        return delegate == null ? super.getTxTimeout() : delegate.getTimeout();
    }

    public int getStatus()
    {
        try
        {
            return delegate == null ? super.getStatus() : delegate.getStatus();
        }
        catch (SystemException e)
        {
            System.out.println(e.getMessage());
            return javax.transaction.Status.STATUS_UNKNOWN;
        }
    }

    public Collection<SynchronizationInfo> getSynchronizationInfo()
    {
        if (delegate == null)
            return super.getSynchronizationInfo();

        if (synchronizations == null)
        {
            synchronizations = new ArrayList<SynchronizationInfo> ();

            for (Map.Entry<Uid, String> me : delegate.getSynchronizations().entrySet())
                synchronizations.add(new SynchronizationInfo(me.getKey(), me.getValue()));
        }

        return synchronizations;
    }

    public Collection<XAResourceInfo> getResources()
    {
        if (delegate == null)
            return super.getResources();

        if (resources == null)
        {
            resources = new ArrayList<XAResourceInfo>();

            for (Map.Entry<XAResource, TxInfo> me : delegate.getResources().entrySet())
                resources.add(new XAResourceInfo(me.getKey(), me.getValue().xid(), txState(me.getValue())));
        }

        return resources;
    }

    private String txState(TxInfo info)
    {
        switch (info.getState())
        {
            case TxInfo.ASSOCIATED: return "TxInfo.ASSOCIATED";
            case TxInfo.NOT_ASSOCIATED: return "TxInfo.NOT_ASSOCIATED";
            case TxInfo.ASSOCIATION_SUSPENDED: return "TxInfo.ASSOCIATION_SUSPENDED";
            case TxInfo.FAILED: return "TxInfo.FAILED";
            case TxInfo.OPTIMIZED_ROLLBACK: return "TxInfo.OPTIMIZED_ROLLBACK";
            default:
                // FALLTHRU
            case TxInfo.UNKNOWN: return "TxInfo.UNKNOWN";
        }
    }

    public boolean isLive()
    {
        return true;
    }
}
