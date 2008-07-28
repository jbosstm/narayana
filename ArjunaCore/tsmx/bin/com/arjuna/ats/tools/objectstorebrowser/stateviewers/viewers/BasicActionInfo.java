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
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers;

import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import java.util.Collection;

/**
 * Interface to abstract the differences between JTA and JTS
 * actions.
 */
public interface BasicActionInfo
{
    RecordList getFailedList();
    RecordList getHeuristicList();
    RecordList getPendingList();
    RecordList getPreparedList();
    RecordList getReadOnlyList();
    UidInfo getUidInfo();
    int getTxTimeout();
    int getStatus();
    Collection<SynchronizationInfo> getSynchronizationInfo();
    Collection<XAResourceInfo> getResources();
    boolean activate();
    boolean deactivate();
    Uid get_uid();
    String type();
    void remove() throws ObjectStoreException;
    boolean isLive();
}
