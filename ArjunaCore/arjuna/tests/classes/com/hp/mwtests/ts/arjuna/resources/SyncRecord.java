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
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SyncRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import java.util.ArrayList;
import java.util.Collection;

public class SyncRecord implements SynchronizationRecord
{
    private Uid _theUid = new Uid();
    private boolean _called = false;
    private int status = ActionStatus.CREATED;
    private long beforeTimeStamp = 0;
    private long afterTimeStamp = 0;
    private boolean interposed = false;
    private FailureMode failureMode;
    private Collection<SyncRecord> beforeSynchs = null;
    private Throwable beforeThrowable = null;

    public enum FailureMode
    {
        NONE
        ,BEFORE_FAIL
        ,AFTER_FAIL
    };

    public SyncRecord(boolean interposed, FailureMode failureMode) {
        this.interposed = interposed;

        this.failureMode = failureMode;
    }

    public SyncRecord() {
        this(false, FailureMode.NONE);
    }

    public void registerSynchDuringSynch(SyncRecord syncRecord) {
        if (beforeSynchs == null)
            beforeSynchs = new ArrayList<SyncRecord>();

        beforeSynchs.add(syncRecord);
    }

    public boolean beforeCompletion()
    {
        boolean problem = false;

        beforeTimeStamp = System.currentTimeMillis();

        if (beforeSynchs != null) {
            BasicAction action = ThreadActionData.currentAction();

            if (action != null && action instanceof TwoPhaseCoordinator) {
                TwoPhaseCoordinator twoPC = (TwoPhaseCoordinator) action;

                for (SyncRecord syncRecord : beforeSynchs) {
                    if (twoPC.addSynchronization(syncRecord) != AddOutcome.AR_ADDED) {
                        System.out.printf("Error registering synchronization %s during beforeCompletion%n", syncRecord);
                        problem = true;
                    }
                }
            }
        }

        if (failureMode.equals(FailureMode.BEFORE_FAIL)) {
            return false;
        } else if (beforeThrowable != null) {
            if (beforeThrowable instanceof Error)
                throw (Error) beforeThrowable;
            else if (beforeThrowable instanceof RuntimeException)
                throw (RuntimeException) beforeThrowable;
            else
                throw new RuntimeException(beforeThrowable);
        }

        return !problem;
    }

    public boolean afterCompletion(int status)
    {
        this.status = status;
        _called = true;
        afterTimeStamp = System.currentTimeMillis();

        if (failureMode.equals(FailureMode.AFTER_FAIL))
            return false;

        return true;
    }

    @Override
    public boolean isInterposed() {
        return interposed;
    }

    public int getStatus() {
        return status;
    }

    public long getBeforeTimeStamp() {
        return beforeTimeStamp;
    }

    public long getAfterTimeStamp() {
        return afterTimeStamp;
    }

    public void setBeforeThrowable(Throwable beforeThrowable) {
        this.beforeThrowable = beforeThrowable;
    }

    public boolean called ()
    {
        return _called;
    }

    public Uid get_uid()
    {
        return _theUid;
    }

    public int compareTo(Object o) {
        SynchronizationRecord other = (SynchronizationRecord) o;

        if(this.isInterposed() && (!other.isInterposed()))
            return 1;
        else if((!this.isInterposed()) && other.isInterposed())
            return -1;
        else if(this._theUid.equals(other.get_uid()))
            return 0;
        else
            return this._theUid.lessThan(other.get_uid()) ? -1 : 1;
    }
}

