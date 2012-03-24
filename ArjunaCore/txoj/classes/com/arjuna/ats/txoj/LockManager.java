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

package com.arjuna.ats.txoj;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.ObjectStatus;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Mutex;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionHierarchy;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.txoj.LockConflictManager;
import com.arjuna.ats.internal.txoj.LockList;
import com.arjuna.ats.internal.txoj.LockListIterator;
import com.arjuna.ats.internal.txoj.abstractrecords.CadaverLockRecord;
import com.arjuna.ats.internal.txoj.abstractrecords.LockRecord;
import com.arjuna.ats.internal.txoj.lockstore.BasicLockStore;
import com.arjuna.ats.internal.txoj.lockstore.BasicPersistentLockStore;
import com.arjuna.ats.txoj.common.txojPropertyManager;
import com.arjuna.ats.txoj.exceptions.LockStoreException;
import com.arjuna.ats.txoj.lockstore.LockStore;
import com.arjuna.ats.txoj.logging.txojLogger;

/**
 * This class provides (transactional) concurrency control for application
 * objects.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: LockManager.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 * @see com.arjuna.ats.arjuna.StateManager
 */

public class LockManager extends StateManager
{

    /**
     * The default retry value which will be used by setlock if no other value
     * is given.
     * 
     * @see #setlock
     */

    public static final int defaultRetry = 100;

    /**
     * The default timeout value which will be used by setlock if no other value
     * is given.
     * 
     * @see #setlock
     */

    public static final int defaultSleepTime = 250;

    /**
     * By default, threads which call setlock with conflicting locks will spin
     * for the specified (or default) number of timeout and retry attempts, and
     * then return failure if the lock could not be acquired. If the *retry*
     * period is set to this value, then such threads will sleep for their total
     * wait period and be signalled if the lock is released within this period
     * of time.
     * 
     * @see #setlock
     * @since JTS 2.1.
     */

    public static final int waitTotalTimeout = -100;

    /**
     * Cleanup. Note we grab the semaphore before destroying the the lock store
     * to ensure the store is deleted cleanly.
     */

    public void finalize () throws Throwable
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("LockManager.finalize() for object-id " + get_uid()
                    + " type " + type());
        }

        /*
         * terminate should have been called. Check and warn/do something about it if this
         * is not the case!
         */
        
        if (status() == ObjectStatus.ACTIVE_NEW)
        {
            BasicAction action = BasicAction.Current();

            if ((action != null) && (action.status() == ActionStatus.RUNNING)) {
                tsLogger.i18NLogger.warn_StateManager_1();
                cleanup(false);
            }
        }
        
        boolean doSignal = false;

        cleanUp();

        if (mutex != null)
        {
            if (mutex.lock() == Mutex.LOCKED)
                doSignal = true;
        }

        locksHeld = null;
        lockStore = null;
        conflictManager = null;

        if (doSignal) // mutex must be set
            mutex.unlock();

        mutex = null;

        super.finalize();
    }

    /**
     * Change lock ownership as nested action commits. All locks owned by the
     * committing action have their owners changed to be the parent of the
     * committing action. BasicAction ensures this is only called at nested
     * commit. This function works by copying the old LockList pointer and then
     * creating a new held lock list. Locks are then moved from the old to the
     * new, propagating en route.
     */

    public boolean propagate (Uid from, Uid to)
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::propagate(" + from + ", " + to + ")");
        }

        boolean result = false;
        int retryCount = 10;

        do
        {
            try
            {
                Object syncObject = ((BasicAction.Current() == null) ? getMutex() : BasicAction.Current());
                
                synchronized (syncObject)
                {
                    synchronized (locksHeldLockObject)
                    {
                        if (loadState())
                        {
                            LockList oldlist = locksHeld;
                            Lock current = null;
    
                            locksHeld = new LockList(); /* create a new one */
    
                            if (locksHeld != null)
                            {
                                /*
                                 * scan through old list of held locks and propagate
                                 * to parent.
                                 */
    
                                while ((current = oldlist.pop()) != null)
                                {
                                    if (current.getCurrentOwner().equals(from))
                                    {
                                        current.propagate();
                                    }
    
                                    if (!locksHeld.insert(current))
                                    {
                                        current = null;
                                    }
                                }
    
                                oldlist = null; /* get rid of old lock list */
    
                                result = true;
                            }
                            else
                            {
                                /*
                                 * Cannot create new locklist - abort and try again.
                                 */
    
                                freeState();
    
                                throw new NullPointerException();
                            }
                        }
    
                        if (result)
                        {
                            result = unloadState();
                        }
                    }
                }
            }
            catch (NullPointerException e)
            {
                result = false;
            }

            if (!result)
            {
                try
                {
                    Thread.sleep(LockManager.DOZE_TIME);
                }
                catch (InterruptedException e)
                {
                }
            }

        }
        while ((!result) && (--retryCount > 0));

        if (!result)
        {
            txojLogger.i18NLogger.warn_LockManager_1();

            synchronized (locksHeldLockObject)
            {
                freeState();
            }
        }

        return result;
    }

    /**
     * Clear out all locks for a given action. Should be triggered automatically
     * at top-level commit but is also user callable so is potentially
     * dangerous.
     */

    public final boolean releaseAll (Uid actionUid)
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::releaseAll(" + actionUid + ")");
        }
        
        return doRelease(actionUid, true);
    }

    /**
     * Release a SINGLE LOCK lock that has the given uid. Breaks two-phase
     * locking rules so watch out!
     */

    public final boolean releaselock (Uid lockUid)
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::releaseLock(" + lockUid + ")");
        }

        return doRelease(lockUid, false);
    }

    /*
     * This is the main user visible operation. Attempts to set the given lock
     * on the current object. If lock cannot be set, then the lock attempt is
     * retried retry times before giving up and returning an error. This gives a
     * simple handle on deadlock. Use the default timeout and retry values.
     * @return <code>LockResult</code> indicating outcome.
     */

    public final int setlock (Lock toSet)
    {
        return setlock(toSet, LockManager.defaultRetry,
                LockManager.defaultSleepTime);
    }

    /*
     * This is the main user visible operation. Attempts to set the given lock
     * on the current object. If lock cannot be set, then the lock attempt is
     * retried retry times before giving up and returning an error. This gives a
     * simple handle on deadlock. Use the default timeout value.
     * @return <code>LockResult</code> indicating outcome.
     */

    public final int setlock (Lock toSet, int retry)
    {
        return setlock(toSet, retry, LockManager.defaultSleepTime);
    }

    /*
     * This is the main user visible operation. Attempts to set the given lock
     * on the current object. If lock cannot be set, then the lock attempt is
     * retried retry times before giving up and returning an error. This gives a
     * simple handle on deadlock.
     * @return <code>LockResult</code> indicating outcome.
     */

    public int setlock (Lock toSet, int retry, int sleepTime)
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::setlock(" + toSet + ", " + retry + ", "
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
    
                                    newLockR = new LockRecord(this, (modifyRequired ? false : true), currAct);

                                    if ((lrStatus = currAct.add(newLockR)) != AddOutcome.AR_ADDED)
                                    {
                                        newLockR = null;

                                        if (lrStatus == AddOutcome.AR_REJECTED)
                                            returnStatus = LockResult.REFUSED;
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
                                hasBeenLocked = true;
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

    /**
     * Print information about this instance on the specified
     * <code>PrintWriter</code>.
     */

    public void print (PrintWriter strm)
    {
        LockListIterator next = new LockListIterator(locksHeld);
        Lock current;

        strm.println("LocalLockManager for object " + get_uid());

        if (!stateLoaded)
            strm.println("No loaded state");
        else if (locksHeld != null)
        {
            strm.println("\tCurrently holding : " + locksHeld.entryCount()
                    + " locks");

            while ((current = next.iterate()) != null)
                current.print(strm);
        }
        else
            strm.println("Currently holding : 0 locks");
    }

    /**
     * Load state into object prior to doing the printing.
     */

    public void printState (PrintWriter strm)
    {
        synchronized (locksHeldLockObject)
        {
            boolean iDeleteState = false;

            if (!stateLoaded)
            {
                loadState();
                iDeleteState = true;
            }

            print(strm);

            if (iDeleteState)
                freeState();
        }
    }

    /**
     * Overload StateManager.type()
     */

    public String type ()
    {
        return "StateManager/LockManager";
    }

    /*
     * Pass on some args to StateManager and initialise internal state. The lock
     * store and semaphore are set up lazily since they depend upon the result
     * of the type() operation which if run in the constructor always give the
     * same answer!
     */

    protected LockManager(Uid storeUid)
    {
        this(storeUid, ObjectType.ANDPERSISTENT, ObjectModel.SINGLE);
    }

    protected LockManager(Uid storeUid, int ot)
    {
        this(storeUid, ot, ObjectModel.SINGLE);
    }
    
    // make default SINGLE

    protected LockManager (Uid storeUid, int ot, int om)
    {
        super(storeUid, ot, om);
        
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::LockManager(" + storeUid + ")");
        }

        systemKey = null;
        locksHeld = new LockList();
        lockStore = null;
        mutex = null;
        stateLoaded = false;
        hasBeenLocked = false;
        objectLocked = false;
        conflictManager = new LockConflictManager();
    }
    
    /*
     * Pass on some args to StateManager and initialise internal state. The lock
     * store and semaphore are set up lazily since they depend upon the result
     * of the type() operation which if run in the constructor always give the
     * same answer!
     */

    protected LockManager()
    {
        this(ObjectType.RECOVERABLE);
    }

    protected LockManager(int ot)
    {
        this(ot, ObjectModel.SINGLE);
    }

    protected LockManager (int ot, int om)
    {
        super(ot, om);
        
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::LockManager(" + ot + ")");
        }

        systemKey = null;
        locksHeld = new LockList();
        lockStore = null;
        mutex = null;
        stateLoaded = false;
        hasBeenLocked = false;
        objectLocked = false;
        conflictManager = new LockConflictManager();
    }
    
    /**
     * This method *must* be called in the finalizer of every object. It ensures
     * that any necessary cleanup work is done in the event that the object goes
     * out of scope within a transaction.
     */

    protected void terminate ()
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::terminate() for object-id " + get_uid());
        }

        cleanUp();

        super.terminate();
    }

    protected final void cleanUp ()
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::cleanUp() for object-id " + get_uid());
        }

        if (hasBeenLocked)
        {
            if ((super.objectModel == ObjectModel.MULTIPLE)
                    && (systemKey == null))
            {
                initialise();
            }

            /*
             * Unlike in the original version of Arjuna, we don't check to see
             * if the invoking thread is within a transaction. We look at
             * whether this object has been used within a transaction, and then
             * act accordingly.
             */

            BasicAction current = BasicAction.Current();

            synchronized (super.usingActions)
            {
                if (super.usingActions != null)
                {
                    Enumeration e = super.usingActions.elements();

                    while (e.hasMoreElements())
                    {
                        BasicAction action = (BasicAction) e.nextElement();

                        if (action != null)  // shouldn't be null!!
                        {
                            /*
                             * Pop actions off using list. Don't check if action
                             * is running below so that cadavers can be created
                             * in commit protocol too.
                             */

                            /*
                             * We need to create a cadaver lock record to
                             * maintain the locks because this object is being
                             * deleted.
                             */
                            
                            AbstractRecord A = new CadaverLockRecord(lockStore,
                                    this, action);

                            if (action.add(A) != AddOutcome.AR_ADDED)
                            {
                                A = null;
                            }
                        }
                    }
                }
            }

            hasBeenLocked = false;
        }
    }

    /*
     * doRelease: Does all the hard work of lock release. Either releases all
     * locks for a given tx uid, or simply one lock with a given uid as
     * appropriate.
     */

    protected boolean doRelease (Uid u, boolean all)
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::doRelease(" + u + ", " + all + ")");
        }
        
        Lock previous = null;
        Lock current = null;
        boolean deleted = false;
        boolean result = false;
        int retryCount = 10;
        boolean loaded = false;
        boolean releasedOK = false;
        BasicAction currAct = BasicAction.Current();
        Object syncObject = ((currAct == null) ? getMutex() : currAct);
        
        synchronized (syncObject)
        {
            synchronized (locksHeldLockObject)
            {
                do
                {
                    if (loadState())
                    {
                        loaded = true;
    
                        /*
                         * Must declare iterator after loadstate or it sees an empty
                         * list!
                         */
    
                        LockListIterator next = new LockListIterator(locksHeld);
                        
                        /*
                         * Now scan through held lock list to find which locks to
                         * release u is either the unique id of the lock owner
                         * (oneOrAll = ALL_LOCKS) or the uid of the actual lock
                         * itself (oneOrAll = SINGLE_LOCK).
                         */
    
                        previous = null;
    
                        while ((current = next.iterate()) != null)
                        {
                            Uid checkUid = null;
    
                            if (all)
                                checkUid = current.getCurrentOwner();
                            else
                                checkUid = current.get_uid();

                            /*
                             * Is this the right lock?
                             */

                            if (u.equals(checkUid))
                            {
                                locksHeld.forgetNext(previous);
                                current = null;
                                deleted = true;
    
                                if (!all)
                                {
                                    break;
                                }
                            }
                            else
                                previous = current;
                        }
    
                        result = true;
                    }
                    else
                    {
                        /*
                         * Free state while we still have the lock.
                         */
    
                        freeState();
    
                        result = false;
                    }
                    
                    if (!result)
                    {
                        try
                        {
                            Thread.sleep(LockManager.DOZE_TIME);
                        }
                        catch (InterruptedException e)
                        {
                        }
                    }
                    else
                    {
                        // if (!stateLoaded)
                        if (!loaded)
                        {
                            txojLogger.i18NLogger.warn_LockManager_7();
                            /*
                             * No need to freeState since we will have done that by now.
                             */
                        }
                        else
                        {
                            if (!deleted)
                            {
                                if (txojLogger.logger.isTraceEnabled()) {
                                    txojLogger.logger.trace(" *** CANNOT locate locks  ***");
                                }
                            }
    
                            int unloadRetryCount = 10;

                            do
                            {
                                if (!unloadState())
                                {
                                    txojLogger.i18NLogger.warn_LockManager_8();
                                }
                                else
                                    releasedOK = true;
    
                            }
                            while ((--unloadRetryCount > 0) && (!releasedOK));
                        }
                    }
                }
                while ((!result) && (--retryCount > 0));
            }
        }

        /*
         * Now signal to any waiting threads that they may try to acquire the
         * lock.
         */

        conflictManager.signal();

        return releasedOK;
    }

    /*
     * Simply free up the semaphore. We do this if we detect conflict. Since the
     * list has not been modified it can simply be discarded. Does not need
     * 'synchronized' as can only be called from synchronized methods.
     */

    protected final void freeState ()
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::freeState()");
        }

        if (mutex != null)
        {
            /*
             * If we are working in a shared lock store mode, then clear the
             * cached lock list. Otherwise, do nothing.
             */

            if (super.objectModel != ObjectModel.SINGLE)
            {
                /* clear out the existing list */

                while (locksHeld.pop() != null)
                    ;

                stateLoaded = false;

                if (objectLocked)
                {
                    objectLocked = false;

                    mutex.unlock();
                }
            }
            else
                stateLoaded = false;
        }
        else
        {
            stateLoaded = false;
            objectLocked = false;
        }
    }

    /*
     * Don't need to protect with a synchronization as this routine can only be
     * called from within other protected methods. Only called if multiple
     * object model is used.
     */

    protected final boolean initialise ()
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::initialise()");
        }

        boolean result = false;

        if (systemKey == null)
        {
            systemKey = type();

            if (mutex == null)
            {
                // TODO add a factory if we ever have more than one implementation
                
                mutex = new com.arjuna.ats.internal.arjuna.common.BasicMutex();
            }

            if (mutex != null)
            {
                if (mutex.lock() == Mutex.LOCKED)
                {
                    /*
                     * At some point we may want to add a factory to hide this, but
                     * since we only have two implementations at the moment it is perhaps
                     * overkill.
                     * 
                     * TODO add factory.
                     */
                    
                    if (lockStore == null)
                    {
                        try
                        {
                            if (lockStoreType.equals(BasicLockStore.class.getName())) {
                                lockStore = new BasicLockStore();
                            } else {
                                ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
                                objectStoreEnvironmentBean.setLocalOSRoot(systemKey);
                                lockStore = new BasicPersistentLockStore(objectStoreEnvironmentBean);
                            }
                        }
                        catch (final Exception ex)
                        {
                            lockStore = null;
                        }
                    }
                }

                mutex.unlock();
            }
        }

        result = (lockStore != null);

        return result;
    }

    protected final boolean isAncestorOf (Lock heldLock)
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::isAncestorOf(" + heldLock.getCurrentOwner()
                    + ")");
        }

        BasicAction action = BasicAction.Current();

        if (action == null)
            return false; /* no action no ancestry! */

        return action.isAncestor(heldLock.getCurrentOwner());
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

            if ((mutex == null) || (mutex.tryLock() == Mutex.WOULD_BLOCK))
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
                                current = new Lock(u);

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
                                    cleanLoad = false;
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

        return stateLoaded;
    }

    /*
     * lockconflict: Here we attempt to determine if the provided lock is in
     * conflict with any of the existing locks. If it is we use nested locking
     * rules to allow children to lock objects already locked by their
     * ancestors.
     */

    protected final int lockConflict (Lock otherLock)
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::lockConflict(" + otherLock.get_uid() + ")");
        }

        boolean matching = false;
        Lock heldLock = null;
        LockListIterator next = new LockListIterator(locksHeld);

        while ((heldLock = next.iterate()) != null)
        {
            if (heldLock.conflictsWith(otherLock))
            {
                if (LockManager.nestedLocking)
                {
                    if (!isAncestorOf(heldLock)) /* not quite Moss's rules */
                        return ConflictType.CONFLICT;
                }
                else
                    return ConflictType.CONFLICT;
            }
            else
            {
                if (heldLock.equals(otherLock))
                    matching = true;
            }
        }

        return (matching ? ConflictType.PRESENT : ConflictType.COMPATIBLE);
    }

    /*
     * Unload the state by writing all the locks to the repository and then
     * freeing the semaphore.
     */

    protected final boolean unloadState ()
    {
        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("LockManager::unloadState()");
        }

        /*
         * Single object model means we don't need a lock store at all.
         */

        if (super.objectModel == ObjectModel.SINGLE)
        {
            stateLoaded = false;

            return true;
        }
        else
        {
            boolean unloadOk = false;
            Lock current = null;
            String otype = type();
            Uid u = get_uid();
            OutputObjectState S = new OutputObjectState(u, otype);
            int lockCount = locksHeld.entryCount();

            /* destroy old state from lock store */

            if (txojLogger.logger.isTraceEnabled()) {
                txojLogger.logger.trace("LockManager::unloadState() unloading " + lockCount
                        + " lock(s)");
            }

            if (lockCount == 0)
            {
                if (lockStore.remove_state(u, otype))
                {
                    unloadOk = true;
                }
                else
                {
                    txojLogger.i18NLogger.warn_LockManager_10(u, otype);
                }
            }
            else
            {
                try
                {
                    /* generate new state */

                    S.packInt(lockCount);

                    unloadOk = true;
                    
                    while ((current = locksHeld.pop()) != null)
                    {
                        UidHelper.packInto(current.get_uid(), S);

                        if (!current.save_state(S, ObjectType.ANDPERSISTENT))
                        {
                            txojLogger.i18NLogger.warn_LockManager_11(current.toString());
                            unloadOk = false;
                        }

                        current = null;
                    }

                    if (unloadOk)
                    {
                        unloadOk = false;
                        
                        /* load image into store */

                        if (S.valid() && lockStore.write_committed(u, otype, S))
                        {
                            unloadOk = true;
                        }
                        else
                        {
                            txojLogger.i18NLogger.warn_LockManager_12(u, otype);
                        }
                    }
                }
                catch (IOException e)
                {
                    unloadOk = false;

                    txojLogger.i18NLogger.warn_LockManager_13(u, otype);
                }
            }

            stateLoaded = false;

            if (objectLocked)
            {
                objectLocked = false;

                if (mutex != null) // means object model != SINGLE
                    mutex.unlock(); // and exit mutual exclusion
            }

            return unloadOk;
        }
    }

    protected String lockStoreType = txojPropertyManager.getTxojEnvironmentBean().getLockStoreType();

    protected String systemKey; /* used in accessing system resources */

    protected LockList locksHeld; /* the actual list of locks set */

    protected final Object locksHeldLockObject = new Object(); // mutex for sync
                                                             // on locksHeld.
                                                             // Can't use
                                                             // locksHeld
                                                             // itself, it's
                                                             // mutable.

    protected LockStore lockStore; /* locks held in shared memory */

    protected boolean stateLoaded;

    protected boolean hasBeenLocked;/* Locked at least once */

    protected boolean objectLocked;/* Semaphore grabbed */

    protected com.arjuna.ats.internal.arjuna.common.BasicMutex mutex; /* Controls access to the lock store */

    protected LockConflictManager conflictManager;

    protected static final int DOZE_TIME = 1000000;

    protected static boolean nestedLocking = true;

    static
    {
        nestedLocking = txojPropertyManager.getTxojEnvironmentBean().isAllowNestedLocking();
    }

}