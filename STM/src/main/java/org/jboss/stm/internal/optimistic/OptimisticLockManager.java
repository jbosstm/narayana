/*
 * SPDX short identifier: Apache-2.0
 */



package org.jboss.stm.internal.optimistic;

import java.io.IOException;

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
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.txoj.abstractrecords.LockRecord;
import com.arjuna.ats.txoj.ConflictType;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import com.arjuna.ats.txoj.exceptions.LockStoreException;
import com.arjuna.ats.txoj.logging.txojLogger;

public class OptimisticLockManager extends LockManager
{
    /*
     * Some of these methods are copied directly from LockManager and do 99% the same thing.
     * The only way they differ is with the types of Locks or LockRecords that they create.
     * Probably should refactor later to reduce the amount of copied code.
     */
    
    /*
     * All of this is here to prevent us grabbing a copy of the state of the object when we lock it.
     * Optimistic and pessimistic locks cannot be used in the same transaction on the same object.
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

        if (!(toSet instanceof OptimisticLock))
            return LockResult.REFUSED;
        
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
    
    /*
     * Lock and load the concurrency control state. First we grab the semaphore
     * to ensure exclusive access and then we build the held lock list by
     * retreiving the locks from the lock repository. If there is only one
     * server we do not bother doing this since all the locks can stay in the
     * server's memory. This is yet another consequence of not having
     * multi-threaded servers. Does not require synchronized since it can only
     * be called from other synchronized methods.
     */

    protected final boolean loadState ()
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::loadState()");
        }

        if (super.objectModel == ObjectModel.SINGLE)
        {
            stateLoaded = true;

            return true;
        }
        else
        {
            InputObjectState S = null;

            if ((systemKey == null) && !initialise())
            {
                return false; /* init failed */
            }

            if ((mutex == null) || (!mutex.tryLock()))
            {
                return false;
            }

            stateLoaded = false;
            objectLocked = true;

            /*
             * An exception indicates some form of error and NOT that the state
             * cannot be found, which is indicated by S being null.
             */

            try
            {
                S = lockStore.read_state(get_uid(), type());

                /* Pick returned state apart again */

                if (S != null)
                {
                    Uid u = null; /*
                                                     * avoid system calls in Uid
                                                     * creation
                                                     */
                    Lock current = null;
                    int count = 0;

                    try
                    {
                        count = S.unpackInt();

                        boolean cleanLoad = true;

                        if (txojLogger.logger.isTraceEnabled()) {
                            txojLogger.logger.trace("LockManager::loadState() loading " + count
                                    + " lock(s)");
                        }

                        /*
                         * Carefully rebuild the internal state - if we fail
                         * throw it away and return.
                         */

                        for (int i = 0; (i < count) && cleanLoad; i++)
                        {
                            try
                            {
                                u = UidHelper.unpackFrom(S);
                                current = new OptimisticLock(u);

                                if (current != null)
                                {
                                    if (current.restore_state(S,
                                            ObjectType.ANDPERSISTENT))
                                    {
                                        locksHeld.push(current);
                                    }
                                    else
                                    {
                                        current = null;
                                        cleanLoad = false;
                                    }
                                }
                                else
                                {
                                    cleanLoad = false;
                                }
                            }
                            catch (IOException e)
                            {
                                cleanLoad = false;
                            }
                        }

                        if (cleanLoad)
                            stateLoaded = true;
                        else
                        {
                            while ((current = locksHeld.pop()) != null)
                                current = null;
                        }
                    }
                    catch (IOException e)
                    {
                    }

                    S = null;
                }
                else
                    stateLoaded = true;
            }
            catch (LockStoreException e)
            {
                txojLogger.logger.warn(e);
            }
        }

        if (!stateLoaded)
        {
            if (mutex != null) // means object model != SINGLE
            {
                mutex.unlock(); // and exit mutual exclusion
            }
            
            objectLocked = false;
        }
        
        return stateLoaded;
    }
    
    protected boolean doRelease (Uid u, boolean all)
    {
        synchronized (super.lockStore.getClass())
        {
            return super.doRelease(u, all);
        }
    }
    
    public boolean propagate (Uid from, Uid to)
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