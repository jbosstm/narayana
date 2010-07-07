/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction;

import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.XAResourceInfo;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.UidInfo;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.SynchronizationInfo;
import com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.BasicActionInfo;

import java.util.Collection;
import java.util.Collections;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AtomicActionWrapper.java 2342 2006-03-30 13:06:17Z  $
 */

public class AtomicActionWrapper extends AtomicAction implements BasicActionInfo
{
    private UidInfo uidInfo;
    private ParticipantStore participantStore;

    public AtomicActionWrapper(ParticipantStore participantStore, String type, Uid objUid)
    {
        super(objUid);
        this.participantStore = participantStore;
        uidInfo = new UidInfo(get_uid(), getClass().getName() + "@" + Integer.toHexString(hashCode()));
        
        try
        {
            uidInfo.setCommitted(participantStore.read_committed(objUid, type));
//            uidInfo.setUncommitted(os.read_uncommitted(objUid, type));
        }
        catch (ObjectStoreException e)
        {
        }
    }

    public ParticipantStore getParticipantStore()
    {
        return participantStore;
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
        return getTimeout();
    }

    /**
     * Return the Arjuna concept of the transaction status
     * (as opposed to
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
        if (!getParticipantStore().remove_committed(getSavingUid(), type()))
            throw new ObjectStoreException();
    }

    public boolean isLive()
    {
        return false;
    }
}
