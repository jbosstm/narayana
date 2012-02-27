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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: LockManager.java 2342 2006-03-30 13:06:17Z  $
 */

package org.jboss.stm.internal.optimistic;

import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.ObjectStatus;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.abstractrecords.PersistenceRecord;
import com.arjuna.ats.internal.arjuna.abstractrecords.RecoveryRecord;
import com.arjuna.ats.internal.txoj.abstractrecords.LockRecord;
import com.arjuna.ats.txoj.ConflictType;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import com.arjuna.ats.txoj.logging.txojLogger;

public class OptimisticLockManager extends LockManager
{
    /*
     * All of this is here to prevent us grabbing a copy of the state of the object when we lock it.
     * 
     * WARNING HERE BE DRAGONS.
     * 
     * (non-Javadoc)
     * @see com.arjuna.ats.txoj.LockManager#setlock(com.arjuna.ats.txoj.Lock, int, int)
     */
    
    public int setlock (Lock toSet, int retry, int sleepTime)
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("OptimisticLockManager::setlock(" + toSet + ", " + retry + ", "
                    + sleepTime + ")");
        }

        int conflict = ConflictType.CONFLICT;
        int returnStatus = LockResult.REFUSED;
        LockRecord newLockR = null;
        boolean modifyRequired = false;
        BasicAction currAct = null;

        if (toSet == null)
        {
            txojLogger.i18NLogger.warn_LockManager_2();

            return LockResult.REFUSED;
        }

        initialise();
        
        currAct = BasicAction.Current();

        if (currAct != null)
        {
            ActionHierarchy ah = currAct.getHierarchy();

            if (ah != null)
                toSet.changeHierarchy(ah);
            else
            {
                txojLogger.i18NLogger.warn_LockManager_3();

                toSet = null;
                
                return LockResult.REFUSED;
            }
        }

        if (super.loadObjectState())
            super.setupStore();

        while ((conflict == ConflictType.CONFLICT)
                && ((retry >= 0) || ((retry == LockManager.waitTotalTimeout) && (sleepTime > 0))))
        {
            Object syncObject = ((currAct == null) ? getMutex() : currAct);
            
            synchronized (super.lockStore.getClass())
            {
                synchronized (syncObject)
                {
                    synchronized (locksHeldLockObject)
                    {
                        conflict = ConflictType.CONFLICT;
        
                        if (loadState())
                        {
                            conflict = lockConflict(toSet);
                        }
                        else
                        {
                            txojLogger.i18NLogger.warn_LockManager_4();
                        }
                        
                        if (conflict != ConflictType.CONFLICT)
                        {
                            /*
                             * When here the conflict was resolved or the retry limit
                             * expired.
                             */
        
                            /* no conflict so set lock */
        
                            modifyRequired = toSet.modifiesObject();
        
                            /* trigger object load from store */
    
                            if (super.activate())
                            {
                                returnStatus = LockResult.GRANTED;
        
                                if (conflict == ConflictType.COMPATIBLE)
                                {
                                    int lrStatus = AddOutcome.AR_ADDED;
        
                                    if (currAct != null)
                                    {
                                        /* add new lock record to action list */
        
                                        newLockR = new OptimisticLockRecord(this, (modifyRequired ? false : true), currAct, true);
    
                                        if ((lrStatus = currAct.add(newLockR)) != AddOutcome.AR_ADDED)
                                        {
                                            newLockR = null;
    
                                            if (lrStatus == AddOutcome.AR_REJECTED)
                                            {
                                                returnStatus = LockResult.REFUSED;
                                            }
                                        }
    
                                    }
        
                                    if (returnStatus == LockResult.GRANTED)
                                    {
                                        locksHeld.insert(toSet); /*
                                                                  * add to local lock
                                                                  * list
                                                                  */
                                    }
                                }
                                else
                                {
                                    if (modifyRequired)
                                        returnStatus = LockResult.GRANTED;
                                }
                            }
                            else
                            {
                                /* activate failed - refuse request */
                                txojLogger.i18NLogger.warn_LockManager_5();
    
                                returnStatus = LockResult.REFUSED;
                            }
                        }
                        
                        /*
                         * Unload internal state into lock store only if lock list was
                         * modified if this fails claim the setlock failed. If we are
                         * using the lock daemon we can arbitrarily throw the lock away
                         * as the daemon has it.
                         */
        
                        if ((returnStatus == LockResult.GRANTED)
                                && (conflict == ConflictType.COMPATIBLE))
                        {
                            if (!unloadState())
                            {
                                txojLogger.i18NLogger.warn_LockManager_6();
    
                                returnStatus = LockResult.REFUSED;
                            }
                        }
                        else
                            freeState();
        
                        /*
                         * Postpone call on modified to here so that semaphore will have
                         * been released. This means when modified invokes save_state
                         * that routine may set another lock without blocking.
                         */
        
                        if (returnStatus == LockResult.GRANTED)
                        {
                            if (modifyRequired)
                            {
                                if (super.modified())
                                {
                                    hasBeenLocked = true;
                                }
                                else
                                {
                                    conflict = ConflictType.CONFLICT;
        
                                    returnStatus = LockResult.REFUSED;
                                }
                            }
                        }
        
                        /*
                         * Make sure we free state while we still have the lock.
                         */
        
                        if (conflict == ConflictType.CONFLICT)
                            freeState();
                    }
                }
            }
            
            if (conflict == ConflictType.CONFLICT)
            {
                if (retry != 0)
                {
                    if (sleepTime > 0)
                    {
                        sleepTime -= conflictManager.wait(retry, sleepTime);
                    }
                    else
                        retry = 0;
                }

                if (retry != LockManager.waitTotalTimeout)
                    retry--;
            }
        }
        
        return returnStatus;
    }
    
    protected boolean doRelease (Uid u, boolean all)
    {
        synchronized (super.lockStore.getClass())
        {
            return super.doRelease(u, all);
        }
    }
    
    public final boolean propagate (Uid from, Uid to)
    {
        synchronized (super.lockStore.getClass())
        {
            return super.propagate(from, to);
        }
    }
    
    /**
     * Overload StateManager.type()
     */

    public String type ()
    {
        return "StateManager/LockManager/OptimisticLockManager";
    }
    
    protected OptimisticLockManager ()
    {
        super();
    }
    
    protected OptimisticLockManager (int ot)
    {
        super(ot);
    }
    
    protected OptimisticLockManager (int ot, int om)
    {
        super(ot, om);
    }
    
    protected OptimisticLockManager (Uid u)
    {
        super(u);
    }
    
    protected OptimisticLockManager (Uid u, int objectModel)
    {
        super(u, ObjectType.ANDPERSISTENT, objectModel);
    }
}
