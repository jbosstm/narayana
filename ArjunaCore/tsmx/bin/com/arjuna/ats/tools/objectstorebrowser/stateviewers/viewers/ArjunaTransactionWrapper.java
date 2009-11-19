/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers;

import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import java.util.Collection;
import java.util.Collections;

public class ArjunaTransactionWrapper extends BasicAction implements BasicActionInfo
{
    private String type;
    private UidInfo uidInfo;
    private ObjectStore os;

    public ArjunaTransactionWrapper(Uid objUid, String type)
    {
        this(null, type, objUid);
    }

    public ArjunaTransactionWrapper(ObjectStore os, String type, Uid objUid)
    {
        super(objUid, ObjectType.ANDPERSISTENT);
        this.type = type;
        this.os = os;
        uidInfo = new UidInfo(get_uid(), getClass().getName() + "@" + Integer.toHexString(hashCode()));
        
        try
        {
            uidInfo.setCommitted(os.read_committed(objUid, type));
//            uidInfo.setUncommitted(os.read_uncommitted(objUid, type));
        }
        catch (ObjectStoreException e)
        {
            System.out.println("Error reading tx log record state: " + e.getMessage());
        }
    }

    public ObjectStore getStore()
    {
        return os;
    }

    /**
     * Overloads StateManager.type()
     */
    public String type()
    {
        return type; //ArjunaTransactionImple.typeName();
    }

    public RecordList getFailedList()
    {
        return failedList;
    }

    public RecordList getHeuristicList()
    {
        return heuristicList;
    }

    public RecordList getPendingList()
    {
        return pendingList;
    }

    public RecordList getPreparedList()
    {
        return preparedList;
    }

    public RecordList getReadOnlyList()
    {
        return readonlyList;
    }

    public UidInfo getUidInfo()
    {
        return uidInfo;
    }

    public int getTxTimeout()
    {
        return -1;
    }

    /**
     * Return the Arjuna concept of the transaction status
     * (as opposed to javax transaction Status 
     * @see javax.transaction.Status
     * @return
     */
    public int getStatus()
    {
        return super.status();
    }

    public Collection<SynchronizationInfo> getSynchronizationInfo()
    {
        return Collections.EMPTY_LIST;
    }

    public Collection<XAResourceInfo> getResources()
    {
        return Collections.EMPTY_LIST;
    }

    public void remove() throws ObjectStoreException
    {
        if (!getStore().remove_committed(getSavingUid(), type()))
            throw new ObjectStoreException();
    }

    public boolean isLive()
    {
        return false;
    }
}
