/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.utils.ThreadUtil;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.internal.arjuna.Header;
import com.arjuna.ats.internal.arjuna.abstractrecords.LastResourceRecord;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

/**
 * BasicAction does most of the work of an atomic action, but does not manage
 * thread scoping. This is the responsibility of any derived classes.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: BasicAction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class BasicAction extends StateManager
{

    public BasicAction ()
    {
        super(ObjectType.NEITHER);

        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::BasicAction()");
        }

        pendingList = null;
        preparedList = null;
        readonlyList = null;
        failedList = null;
        heuristicList = null;

        currentHierarchy = null;
        transactionStore = null;
        savedIntentionList = false;

        actionStatus = ActionStatus.CREATED;
        actionType = ActionType.NESTED;

        parentAction = null;
        recordBeingHandled = null;

        heuristicDecision = TwoPhaseOutcome.PREPARE_OK;
        _checkedAction = arjPropertyManager
                .getCoordinatorEnvironmentBean().getCheckedActionFactory()
                .getCheckedAction(get_uid(), type());

        _childThreads = null;
        _childActions = null;
    }

    /**
     * BasicAction constructor with a Uid. This constructor is for recreating an
     * BasicAction, typically during crash recovery.
     */

    public BasicAction (Uid objUid)
    {
        super(objUid, ObjectType.NEITHER);

        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::BasicAction("
                    + objUid + ")");
        }

        pendingList = null;
        preparedList = null;
        readonlyList = null;
        failedList = null;
        heuristicList = null;

        currentHierarchy = null;
        transactionStore = null;
        savedIntentionList = false;

        actionStatus = ActionStatus.CREATED;
        actionType = ActionType.NESTED;

        parentAction = null;
        recordBeingHandled = null;

        heuristicDecision = TwoPhaseOutcome.PREPARE_OK;
        _checkedAction = arjPropertyManager
                .getCoordinatorEnvironmentBean().getCheckedActionFactory()
                .getCheckedAction(get_uid(), type());

        _childThreads = null;
        _childActions = null;
    }

    /**
     * BasicAction destructor. Under normal circumstances we do very little.
     * However there exists the possibility that this action is being deleted
     * while still running (user forgot to commit/abort) - in which case we do
     * an abort for him and mark all our parents as unable to commit.
     * Additionally due to scoping we may not be the current action - but in
     * that case the current action must be one of our nested actions so by
     * applying abort to it we should end up at ourselves!
     */

    public void finalizeInternal()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::finalize()");
        }

        if ((actionStatus == ActionStatus.RUNNING)
                || (actionStatus == ActionStatus.ABORT_ONLY)) {
            /* If current action is one of my children there's an error */

            BasicAction currentAct = BasicAction.Current();

            if ((currentAct != null) && (currentAct != this)) {
                /*
                 * Is the current action a child of this action? If so, abort
                 * until we get to the current action. This works even in a
                 * multi-threaded environment where each thread may have a
                 * different notion of current, since Current returns the thread
                 * specific current.
                 */

                if (currentAct.isAncestor(get_uid())) {
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_1(get_uid());

                    while ((currentAct != this) && (currentAct != null)) {
                        tsLogger.i18NLogger.warn_coordinator_BasicAction_2(currentAct.get_uid());

                        currentAct.Abort();

                        currentAct = BasicAction.Current();
                    }
                }
            }

            BasicAction parentAct = parent();

            /* prevent commit of parents (safety) */

            while (parentAct != null) {
                parentAct.preventCommit();
                parentAct = parentAct.parent();
            }

            tsLogger.i18NLogger.warn_coordinator_BasicAction_3(get_uid());

            /* This will also kill any children */

            Abort();
        }
        else
        {
            if (actionStatus == ActionStatus.PREPARED)
                Thread.yield();
        }

        pendingList = null;
        preparedList = null;
        readonlyList = null;
        failedList = null;
        heuristicList = null;

        transactionStore = null;
        currentHierarchy = null;

        _checkedAction = null;

        if (_childThreads != null)
        {
            _childThreads.clear();
            _childThreads = null;
        }

        if (_childActions != null)
        {
            _childActions.clear();
            _childActions = null;
        }
    }

    /**
     * Return the action hierarchy for this transaction.
     */

    public final ActionHierarchy getHierarchy ()
    {
        return currentHierarchy;
    }

    /**
     * Force the only outcome for the transaction to be to rollback. Only
     * possible if this transaction has not (or is not) terminated.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public final boolean preventCommit ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::preventCommit( " + this + ")");
        }

        boolean res = false;

        //	if (lockMutex())
        {
            /*
             * If we are active then change status. Otherwise it may be an error so check status.
             */

            synchronizationLock.lock();

            try {
                if (actionStatus == ActionStatus.RUNNING)
                    actionStatus = ActionStatus.ABORT_ONLY;
            } finally {
                synchronizationLock.unlock();
            }

            /*
             * Since the reason to call this method is to make sure the transaction
             * only aborts, check the status now and if it has aborted or will abort then
             * we'll consider it a success.
             */

            res = ((actionStatus == ActionStatus.ABORT_ONLY) || (actionStatus == ActionStatus.ABORTED) || (actionStatus == ActionStatus.ABORTING));

            //	    unlockMutex();
        }

        return res;
    }

    /**
     * @return the number of threads associated with this transaction.
     */

    public final int activeThreads ()
    {
        if (_childThreads != null)
            return _childThreads.size();
        else
            return 0;
    }

    /**
     * Add a record to the atomic action. This function returns AR_ADDED if the
     * record is added. AR_REJECTED if the record cannot be added because the
     * action is past the prepare phase, and IGNORED otherwise.
     *
     * @return <code>AddOutcome</code> indicating outcome.
     */

    public final int add (AbstractRecord A)
    {
        synchronizationLock.lock();

        try {
            int result = AddOutcome.AR_REJECTED;

            criticalStart();

            if ((actionStatus <= ActionStatus.ABORTING)
                    && ((recordBeingHandled == null) || !(recordBeingHandled.equals(A))))
            {
                if (pendingList == null)
                    pendingList = new RecordList();

                result = (pendingList.insert(A) ? AddOutcome.AR_ADDED
                        : AddOutcome.AR_DUPLICATE);
            }

            criticalEnd();

            return result;
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * @return the depth of the current transaction hierarchy.
     */

    public final int hierarchyDepth ()
    {
        if (currentHierarchy != null)
            return currentHierarchy.depth();
        else
            return 0; /* should never happen */
    }

    /**
     * boolean function that checks whether the Uid passed as an argument is the
     * Uid for an ancestor of the current atomic action.
     *
     * @return <code>true</code> if the parameter represents an ancestor,
     *         <code>false</code> otherwise.
     */

    public final boolean isAncestor (Uid ancestor)
    {
        boolean res = false;

        if (get_uid().equals(ancestor)) /* actions are their own ancestors */
            res = true;
        else
        {
            if ((parentAction != null) && (actionType != ActionType.TOP_LEVEL))
                res = parentAction.isAncestor(ancestor);
        }

        return res;
    }

    /**
     * @return a reference to the parent BasicAction
     */

    public final BasicAction parent ()
    {
        if (actionType == ActionType.NESTED)
            return parentAction;
        else
            return null;
    }

    public final int typeOfAction ()
    {
        return actionType;
    }

    /**
     * @return the status of the BasicAction
     */

    public final int status ()
    {
        int s = ActionStatus.INVALID;

        //	if (tryLockMutex())
        {
            s = actionStatus;

            //	    unlockMutex();
        }

        return s;
    }

    /**
     * Set up an object store and assign it to the participantStore variable.
     *
     * @return the object store implementation to use.
     * @see com.arjuna.ats.arjuna.objectstore.ObjectStore
     */

    public ParticipantStore getStore ()
    {
        if (transactionStore == null)
        {
            transactionStore = StoreManager.getParticipantStore();
        }

        return transactionStore;
    }

    /**
     * The following function returns the Uid of the top-level atomic action. If
     * this is the top-level transaction then it is equivalent to calling
     * get_uid().
     *
     * @return the top-level transaction's <code>Uid</code>.
     */

    public final Uid topLevelActionUid ()
    {
        BasicAction root = this;

        while (root.parent() != null)
            root = root.parent();

        return root.get_uid();
    }

    /**
     * @return a reference to the top-level transaction. If this is the
     *         top-level transaction then a reference to itself will be
     *         returned.
     */

    public final BasicAction topLevelAction ()
    {
        BasicAction root = this;

        while (root.parent() != null)
            root = root.parent();

        return root;
    }

    /**
     * Overloaded version of activate -- sets up the store, performs read_state
     * followed by restore_state. The store root is <code>null</code>.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public boolean activate ()
    {
        return activate(null);
    }

    /**
     * Overloaded version of activate -- sets up the store, performs read_state
     * followed by restore_state. The root of the object store to use is
     * specified in the <code>root</code> parameter.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public boolean activate (String root)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::activate() for action-id "
                    + get_uid());
        }

        boolean restored = false;

        // Set up store
        ParticipantStore aaStore = getStore();

        if (aaStore == null)
            return false;

        try
        {
            // Read object state

            InputObjectState oState = aaStore.read_committed(getSavingUid(), type());

            if (oState != null)
            {
                synchronizationLock.lock();

                try {
                    restored = restore_state(oState, ObjectType.ANDPERSISTENT);
                } finally {
                    synchronizationLock.unlock();
                }

                oState = null;
            }
            else {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_5(get_uid(), type());

                restored = false;
            }

            return restored;
        }
        catch (ObjectStoreException e)
        {
            tsLogger.i18NLogger.warn_could_not_activate_type_at_object_store(type(), aaStore, e);

            return false;
        }
    }

    /**
     * This operation deactivates a persistent object. It behaves in a similar
     * manner to the activate operation, but has an extra argument which defines
     * whether the object's state should be committed or written as a shadow.
     *
     * The root of the object store is <code>null</code>. It is assumed that
     * this is being called during a transaction commit.
     *
     * @return <code>true</code> on success, <code>false</code> otherwise.
     *
     */

    public boolean deactivate ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::deactivate() for action-id "
                    + get_uid());
        }

        boolean deactivated = false;

        // Set up store
        ParticipantStore aaStore = getStore();

        if (aaStore == null)
            return false;

        try
        {
            // Write object state
            OutputObjectState oState = new OutputObjectState();

            if (save_state(oState, ObjectType.ANDPERSISTENT))
            {
                deactivated = aaStore.write_committed(getSavingUid(), type(), oState);

                oState = null;
            }
            else
            {
                deactivated = false;
            }

            /** If we failed to deactivate then output warning * */
            if (!deactivated) {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_5a(get_uid(), type());
            }
        }
        catch (ObjectStoreException e)
        {
            tsLogger.logger.warn(e);

            deactivated = false;
        }

        return deactivated;
    }

    /**
     * Add the current thread to the list of threads associated with this
     * transaction.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public final boolean addChildThread () // current thread
    {
        return addChildThread(Thread.currentThread());
    }

    /**
     * Add the specified thread to the list of threads associated with this
     * transaction.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public final boolean addChildThread (Thread t)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::addChildThread () action "+get_uid()+" adding "+t);
        }

        if (t == null)
            return false;

        boolean result = false;

        criticalStart();

        synchronizationLock.lock();

        try {
            if (actionStatus <= ActionStatus.ABORTING)
            {
                if (_childThreads == null)
                    _childThreads = new Hashtable<String, Thread>();

                _childThreads.put(ThreadUtil.getThreadId(t), t); // makes sure so we don't get
                // duplicates

                result = true;
            }
        } finally {
            synchronizationLock.unlock();
        }

        criticalEnd();

        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::addChildThread () action "+get_uid()+" adding "+t+" result = "+result);
        }

        return result;
    }

    /*
     * Can be done at any time (Is this correct?)
     */

    /**
     * Remove a child thread. The current thread is removed.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public final boolean removeChildThread () // current thread
    {
        return removeChildThread(ThreadUtil.getThreadId(Thread.currentThread()));
    }

    /**
     * Remove the specified thread from the transaction.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public final boolean removeChildThread (String threadId)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::removeChildThread () action "+get_uid()+" removing "+threadId);
        }

        if (threadId == null)
            return false;

        boolean result = false;

        criticalStart();

        synchronizationLock.lock();

        try {
            if (_childThreads != null)
            {
                _childThreads.remove(threadId);
                result = true;
            }
        } finally {
            synchronizationLock.unlock();
        }

        criticalEnd();

        if (tsLogger.logger.isTraceEnabled())
        {
            tsLogger.logger.trace("BasicAction::removeChildThread () action "+get_uid()+" removing "+threadId+" result = "+result);
        }

        return result;
    }

    /**
     * Add a new child action to the atomic action.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public final boolean addChildAction (BasicAction act)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::addAction () action "+get_uid()+" adding "+((act != null) ? act.get_uid() : Uid.nullUid()));
        }

        if (act == null)
            return false;

        boolean result = false;

        criticalStart();

        synchronizationLock.lock();

        try {
            /*
             * Must be <= as we sometimes need to do processing during commit
             * phase.
             */

            if (actionStatus <= ActionStatus.ABORTING)
            {
                if (_childActions == null)
                    _childActions = new Hashtable<BasicAction, BasicAction>();

                _childActions.put(act, act);
                result = true;
            }
        } finally {
            synchronizationLock.unlock();
        }

        criticalEnd();

        if (tsLogger.logger.isTraceEnabled())
        {
            tsLogger.logger.trace("BasicAction::addChildAction () action "+get_uid()+" adding "+act.get_uid()+" result = "+result);
        }

        return result;
    }

    /*
     * Can be done at any time (Is this correct?)
     */

    /**
     * Redefined version of save_state and restore_state from StateManager.
     *
     * Normal operation (no crashes):
     *
     * BasicAction.save_state is called after a successful prepare. This causes
     * and BasicAction object to be saved in the object store. This object
     * contains primarily the "intentions list" of the BasicAction. After
     * successfully completing phase 2 of the commit protocol, the BasicAction
     * object is deleted from the store.
     *
     * Failure cases:
     *
     * If a server crashes after successfully preparing, then upon recovery the
     * action must be resolved (either committed or aborted) depending upon
     * whether the co-ordinating atomic action committed or aborted. Upon server
     * recovery, the crash recovery mechanism detects ServerBasicAction objects
     * in the object store and attempts to activate the BasicAction object of
     * the co-ordinating action. If this is successful then the SAA is committed
     * else aborted.
     *
     * If, when processing phase 2 of the commit protocol, the co-ordinator
     * experiences a failure to commit from one of the records then the
     * BasicAction object is NOT deleted. It is rewritten when a new state which
     * contains a list of the records that failed during phase 2 commit. This
     * list is called the "failedList".
     *
     * The crash recovery manager will detect local BasicAction objects in
     * addition to SAA objects in the objectstore. An attempt will be made to
     * commit these actions. If the action contained a call to a now dead
     * server, this action can never be resolved and the AA object can never be
     * removed. However, if the action is purely local then after the processing
     * is complete the removed by crash recovery.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public boolean save_state (OutputObjectState os, int ot)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::save_state ()");
        }

        try
        {
            packHeader(os, new Header(get_uid(), Utility.getProcessUid()));

            os.packBoolean(pastFirstParticipant);
        }
        catch (IOException e)
        {
            tsLogger.i18NLogger.warn_coordinator_BasicAction_73(e.getMessage());

            return false;
        }

        /*
         * In a presumed abort scenario, this routine is called: a) After a
         * successful prepare - to save the intentions list. b) After a failure
         * during phase 2 of commit - to overwrite the intentions list by the
         * failedList.
         *
         * If we're using presumed nothing, then it could be called: a) Whenever
         * a participant is registered.
         */

        RecordList listToSave = null;
        boolean res = true;

        /*
         * If we have a failedList then we are re-writing a BasicAction object
         * after a failure during phase 2 commit
         */

        if ((failedList != null) && (failedList.size() > 0))
        {
            listToSave = failedList;
        }
        else
        {
            listToSave = preparedList;
        }

        AbstractRecord first = ((listToSave != null) ? listToSave.getFront()
                : null);
        AbstractRecord temp = first;
        boolean havePacked = ((listToSave == null) ? false : true);

        while ((res) && (temp != null))
        {
            listToSave.putRear(temp);

            /*
             * First check to see if we need to call save_state. If we do then
             * we must first save the record type (and enum) and then save the
             * unique identity of the record (a string). The former is needed to
             * determine what type of record we are restoring, while the latter
             * is required to re-create the actual record.
             */

            /*
             * First check to see if we need to call save_state. If we do then
             * we must first save the record type. This is used to determine
             * which type of record to create when restoring.
             */

            if (tsLogger.logger.isTraceEnabled())
            {
                tsLogger.logger.trace("BasicAction::save_state - next record to pack is a "+temp.typeIs()
                        +" record "+temp.type()+" should save it? = "+temp.doSave());
            }

            if (temp.doSave())
            {
                res = true;

                try
                {
                    if (tsLogger.logger.isTraceEnabled()) {
                        tsLogger.logger.trace("Packing a "+temp.typeIs()+" record");
                    }

                    os.packInt(temp.typeIs());
                    res = temp.save_state(os, ot);
                }
                catch (IOException e)
                {
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_72(e.getMessage());

                    res = false;
                }
            }

            temp = listToSave.getFront();

            if (temp == first)
            {
                listToSave.putFront(temp);
                temp = null;
            }
        }

        /*
         * If we only ever had a heuristic list (e.g., one-phase commit) then
         * pack a record delimiter.
         */

        if (res && (os.notempty() || !havePacked))
        {
            try
            {
                if (tsLogger.logger.isTraceEnabled()) {
                    tsLogger.logger.trace("Packing a NONE_RECORD");
                }

                os.packInt(RecordType.NONE_RECORD);
            }
            catch (IOException e)
            {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_72(e.getMessage());

                res = false;
            }
        }

        if (res)
        {
            // Now deal with anything on the heuristic list!

            int hSize = ((heuristicList == null) ? 0 : heuristicList.size());

            try
            {
                os.packInt(hSize);
            }
            catch (IOException e)
            {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_72(e.getMessage());

                res = false;
            }

            if (res && (hSize > 0))
            {
                first = heuristicList.getFront();
                temp = first;

                while (res && (temp != null))
                {
                    heuristicList.putRear(temp);

                    if (temp.doSave())
                    {
                        res = true;

                        try
                        {
                            if (tsLogger.logger.isTraceEnabled()) {
                                tsLogger.logger.trace("HeuristicList - packing a "+temp.typeIs()+" record");
                            }

                            os.packInt(temp.typeIs());
                            res = temp.save_state(os, ot);
                        }
                        catch (IOException e)
                        {
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_72(e.getMessage());

                            res = false;
                        }
                    }

                    temp = heuristicList.getFront();

                    if (temp == first)
                    {
                        heuristicList.putFront(temp);
                        temp = null;
                    }
                }

                if (res && os.notempty())
                {
                    try
                    {
                        if (tsLogger.logger.isTraceEnabled()) {
                            tsLogger.logger.trace("HeuristicList - packing a NONE_RECORD");
                        }

                        os.packInt(RecordType.NONE_RECORD);
                    }
                    catch (IOException e)
                    {
                        tsLogger.i18NLogger.warn_coordinator_BasicAction_72(e.getMessage());

                        res = false;
                    }
                }
            }
        }

        if (res && os.notempty())
        {
            try
            {
                if (tsLogger.logger.isTraceEnabled()) {
                    tsLogger.logger.trace("Packing action status of "+ActionStatus.stringForm(actionStatus));
                }

                os.packInt(actionStatus);
                os.packInt(actionType); // why pack since only top-level?
                os.packInt(heuristicDecision); // can we optimize?
            }
            catch (IOException e)
            {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_72(e.getMessage());

                res = false;
            }
        }

        return res;
    }

    /**
     * Remove a child action.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public final boolean removeChildAction (BasicAction act)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::removeChildAction () action "+get_uid()+" removing "+((act != null) ? act.get_uid() : Uid.nullUid()));
        }

        if (act == null)
            return false;

        boolean result = false;

        criticalStart();

        synchronizationLock.lock();

        try {
            if (_childActions != null)
            {
                _childActions.remove(act);
                result = true;
            }
        } finally {
            synchronizationLock.unlock();
        }

        criticalEnd();

        if (tsLogger.logger.isTraceEnabled())
        {
            tsLogger.logger.trace("BasicAction::removeChildAction () action "+get_uid()+" removing "+act.get_uid()+" result = "+result);
        }

        return result;
    }

    /**
     * Add the specified CheckedAction object to this transaction.
     *
     * @see com.arjuna.ats.arjuna.coordinator.CheckedAction
     */

    protected final void setCheckedAction (CheckedAction c)
    {
        synchronizationLock.lock();

        try {
            criticalStart();

            _checkedAction = c;

            criticalEnd();
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * @return the Uid that the transaction's intentions list will be saved
     *         under.
     */

    public Uid getSavingUid ()
    {
        return get_uid();
    }

    /**
     * Overloads Object.toString()
     */

    public String toString ()
    {
        return new String("BasicAction: " + get_uid() + " status: "
                + ActionStatus.stringForm(actionStatus));
    }

    /**
     * This assumes the various lists are zero length when it is called.
     *
     * @return <code>true</code> if successful, <code>false</code>
     *         otherwise.
     */

    public boolean restore_state (InputObjectState os, int ot)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::restore_state ()");
        }

        createPreparedLists();

        boolean res = true;
        int record_type = RecordType.NONE_RECORD;
        int tempActionStatus = ActionStatus.INVALID;
        int tempActionType = ActionType.TOP_LEVEL;
        int tempHeuristicDecision = TwoPhaseOutcome.PREPARE_OK;

        /*
         * Unpack the prepared list. Note: This may either be a full intentions
         * list or just the failedList, either way, restore it as the prepared
         * list.
         */

        try
        {
            Header hdr = new Header();

            unpackHeader(os, hdr);

            pastFirstParticipant = os.unpackBoolean();
        }
        catch (IOException e)
        {
            tsLogger.i18NLogger.warn_coordinator_BasicAction_73(e.getMessage());

            return false;
        }

        try
        {
            record_type = os.unpackInt();

            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("Unpacked a "+record_type+" record");
            }
        }
        catch (IOException e)
        {
            tsLogger.i18NLogger.warn_coordinator_BasicAction_73(e.getMessage());

            res = false;
        }

        while ((res) && (record_type != RecordType.NONE_RECORD))
        {
            AbstractRecord record = AbstractRecord.create(record_type);

            if (record == null) {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_21(Integer.toString(record_type));

                res = false;
            }
            else
                res = (record.restore_state(os, ot) && preparedList.insert(record));

            if (res)
            {
                try
                {
                    record_type = os.unpackInt();

                    if (tsLogger.logger.isTraceEnabled()) {
                        tsLogger.logger.trace("Unpacked a "+record_type+" record");
                    }
                }
                catch (IOException e)
                {
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_73(e.getMessage());

                    res = false;
                }
            }
        }

        // Now deal with the heuristic list!

        int hSize = 0;

        if (res)
        {
            try
            {
                hSize = os.unpackInt();

                if (tsLogger.logger.isTraceEnabled()) {
                    tsLogger.logger.trace("HeuristicList - Unpacked heuristic list size of "+hSize);
                }
            }
            catch (IOException e)
            {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_73(e.getMessage());

                res = false;
            }
        }

        if (hSize > 0)
        {
            tsLogger.logger.warn("Transaction "+get_uid()+" has "+hSize+" heuristic participant(s)!");

            try
            {
                record_type = os.unpackInt();

                if (tsLogger.logger.isTraceEnabled()) {
                    tsLogger.logger.trace("HeuristicList - Unpacked a "+record_type+" record");
                }
            }
            catch (IOException e)
            {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_73(e.getMessage());

                res = false;
            }

            while ((res) && (record_type != RecordType.NONE_RECORD))
            {
                AbstractRecord record = AbstractRecord.create(record_type);

                try
                {
                    res = (record.restore_state(os, ot) && heuristicList.insert(record));

                    record_type = os.unpackInt();

                    tsLogger.logger.warn("Transaction "+get_uid()+" restored heuristic participant "+record);


                    if (tsLogger.logger.isTraceEnabled()) {
                        tsLogger.logger.trace("HeuristicList - Unpacked a "+record_type+" record");
                    }
                }
                catch (IOException e)
                {
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_73(e.getMessage());

                    res = false;
                }
                catch (final NullPointerException ex) {
                    tsLogger.i18NLogger.warn_coordinator_norecordfound(Integer.toString(record_type));

                    res = false;
                }
            }
        }

        if (res)
        {
            try
            {
                tempActionStatus = os.unpackInt();
                tempActionType = os.unpackInt();
                tempHeuristicDecision = os.unpackInt();
            }
            catch (IOException e) {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_24();

                res = false;
            }
        }

        if (res)
        {
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("Restored action status of "+ActionStatus.stringForm(tempActionStatus)+
                        " "+Integer.toString(tempActionStatus));

                tsLogger.logger.trace("Restored action type "+
                        ((tempActionType == ActionType.NESTED) ? "Nested" : "Top-level")+
                        " "+Integer.toString(tempActionType));

                tsLogger.logger.trace(" Restored heuristic decision of "+
                        TwoPhaseOutcome.stringForm(tempHeuristicDecision)+" "+Integer.toString(tempHeuristicDecision));
            }

            actionStatus = tempActionStatus;
            actionType = tempActionType;
            heuristicDecision = tempHeuristicDecision;
            savedIntentionList = true;
        }

        return res;
    }

    /**
     * Overloads StateManager.type()
     */

    public String type ()
    {
        return "/StateManager/BasicAction";
    }

    /**
     * @return the thread's notion of the current transaction.
     */

    public static BasicAction Current ()
    {
        return ThreadActionData.currentAction();
    }

    /**
     * If heuristic outcomes are returned, by default we will not save the state
     * once the forget method has been called on them (which happens as soon as
     * we have received all outcomes from registered resources). By specifying
     * otherwise, we will always maintain the heuristic information, which may
     * prove useful for logging and off-line resolution.
     *
     * @return <code>true</code> if the transaction should save its heuristic
     *         information, <code>false</code> otherwise.
     */

    public static boolean maintainHeuristics ()
    {
        return TxControl.maintainHeuristics;
    }

    /**
     * Overloads <code>StateManager.destroy</code> to prevent destroy being
     * called on a BasicAction. Could be a *very* bad idea!!
     *
     * @return <code>false</code>.
     * @see com.arjuna.ats.arjuna.StateManager
     */

    public boolean destroy ()
    {
        return true;
    }

    /**
     * @return the list of child transactions. Currently only their ids are
     *         given.
     * @since JTS 2.2.
     */

    public final Object[] childTransactions ()
    {
        int size = ((_childActions == null) ? 0 : _childActions.size());

        if (size > 0)
        {
            Collection<BasicAction> c = _childActions.values();

            return c.toArray();
        }

        return null;
    }

    /**
     * Get any Throwable that was caught during commit processing but not directly rethrown.
     * @return a list of ThrowableS, if any
     */
    public List<Throwable> getDeferredThrowables()
    {
        return deferredThrowables;
    }

    @Override
    public boolean equals (java.lang.Object obj)
    {
        if (obj instanceof BasicAction)
        {
            if (((BasicAction) obj).get_uid().equals(get_uid()))
                return true;
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return get_uid().hashCode();
    }

    /**
     * Forget any heuristics we may have received, and tell the resources which
     * generated them to forget too.
     *
     * @return <code>true</code> if heuristic information (if any) was
     *         successfully forgotten, <code>false</code> otherwise.
     */

    protected boolean forgetHeuristics ()
    {
        if ((heuristicList != null) && (heuristicList.size() > 0))
        {
            doForget(heuristicList);
            updateState();

            if (heuristicList.size() == 0)
                return true;
            else
                return false;
        }
        else
            return true;
    }

    /**
     * Atomic action Begin operation. Does not change the calling thread's
     * notion of the current transaction.
     *
     * @return <code>ActionStatus</code> indicating outcome.
     */

    protected int Begin (BasicAction parentAct)
    {
        synchronizationLock.lock();

        try {
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("BasicAction::Begin() for action-id "
                        + get_uid());
            }

            // check to see if transaction system is enabled

            if (!TxControl.isEnabled()) {
                /*
                 * Prevent transaction from making forward progress.
                 */

                actionStatus = ActionStatus.ABORT_ONLY;

                tsLogger.i18NLogger.warn_coordinator_notrunning();
            }
            else
            {
                if (actionStatus != ActionStatus.CREATED) {
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_29(get_uid(), ActionStatus.stringForm(actionStatus));
                }
                else
                {
                    actionInitialise(parentAct);
                    actionStatus = ActionStatus.RUNNING;

                    if ((actionType != ActionType.TOP_LEVEL)
                            && ((parentAct == null) || (parentAct.status() > ActionStatus.RUNNING)))
                    {
                        actionStatus = ActionStatus.ABORT_ONLY;

                        if (parentAct == null) {
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_30(get_uid());
                        }
                        else
                        {
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_31(get_uid(), parentAct.get_uid(), Integer.toString(parentAct.status()));
                        }
                    }

                    ActionManager.manager().put(this);

                    if(finalizeBasicActions) {
                        finalizerObject = new BasicActionFinalizer(this);
                    }

                    if (TxStats.enabled())
                    {
                        TxStats.getInstance().incrementTransactions();

                        if (parentAct != null)
                            TxStats.getInstance().incrementNestedTransactions();
                    }
                }
            }

            return actionStatus;
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * End the atomic action by committing it. This invokes the prepare()
     * operation. If this succeeds then the pendingList should be empty and the
     * records that were formally on it will have been distributed between the
     * preparedList and the readonlyList, also if the action is topLevel then
     * the intention list will have been written to the object store. Then
     * invoke phase2Commit and clean up the object store if necessary
     *
     * If prepare() fails invoke phase2Abort. In this case the pendingList may
     * still contain records but phase2Abort takes care of these. Also in this
     * case no intention list has been written.
     *
     * Does not change the calling thread's notion of the current transaction.
     *
     * Any heuristic outcomes will only be reported if the parameter is
     * <code>true</code>.
     *
     * @return <code>ActionStatus</code> indicating outcome.
     */

    protected int End (boolean reportHeuristics)
    {
        synchronizationLock.lock();

        try {
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("BasicAction::End() for action-id "
                        + get_uid());
            }

            /* Check for superfluous invocation */

            if ((actionStatus != ActionStatus.RUNNING)
                    && (actionStatus != ActionStatus.ABORT_ONLY)) {
                switch (actionStatus) {
                    case ActionStatus.CREATED:
                        tsLogger.i18NLogger.warn_coordinator_BasicAction_33(get_uid());
                        break;
                    case ActionStatus.COMMITTED:
                        tsLogger.i18NLogger.warn_coordinator_BasicAction_34(get_uid());
                        break;
                    default:
                        tsLogger.i18NLogger.warn_coordinator_BasicAction_35(get_uid());
                        break;
                }

                return actionStatus;
            }

            /*
             * Check we are the current action. Abort parents if not true. Check we
             * have not children (threads or actions).
             */

            if (!checkIsCurrent() || checkChildren(true)
                    || (actionStatus == ActionStatus.ABORT_ONLY))
            {
                return Abort();
            }

            Long startTime = TxStats.enabled() ? System.nanoTime() : null;

            if (pendingList != null)
            {
                /*
                 * If we only have a single item on the prepare list then we can try
                 * to commit in a single phase.
                 */

                if (doOnePhase())
                {
                    onePhaseCommit(reportHeuristics, true);

                    ActionManager.manager().remove(get_uid());
                }
                else
                {
                    int prepareStatus = prepare(reportHeuristics);

                    if (prepareStatus == TwoPhaseOutcome.PREPARE_NOTOK
                            || prepareStatus == TwoPhaseOutcome.ONE_PHASE_ERROR) {
                        tsLogger.i18NLogger.warn_coordinator_BasicAction_36(get_uid());

                        if (heuristicDecision != TwoPhaseOutcome.PREPARE_OK) {
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_37(TwoPhaseOutcome.stringForm(heuristicDecision));
                        }

                        tsLogger.i18NLogger.warn_coordinator_BasicAction_38();

                        if (!reportHeuristics && TxControl.asyncCommit
                                && (parentAction == null)) {
                            TwoPhaseCommitThreadPool.submitJob(new AsyncCommit(this, false));
                        } else
                            phase2Abort(reportHeuristics); /* first phase failed */
                    }
                    else
                    {
                        if (!reportHeuristics && TxControl.asyncCommit
                                && (parentAction == null))
                        {
                            TwoPhaseCommitThreadPool.submitJob(new AsyncCommit(this, true));
                        }
                        else
                            phase2Commit(reportHeuristics); /* first phase succeeded */
                    }
                }
            }
            else
            {
                ActionManager.manager().remove(get_uid());

                actionStatus = ActionStatus.COMMITTED;

                if (TxStats.enabled())
                {
                    if (heuristicDecision != TwoPhaseOutcome.HEURISTIC_ROLLBACK)
                    {
                        if (startTime == null)
                            TxStats.getInstance().incrementCommittedTransactions(0L);
                        else
                            TxStats.getInstance().incrementCommittedTransactions(System.nanoTime() - startTime);
                    }
                }
            }

            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.tracef("BasicAction::End() result for action-id (%s) is (%s) node id: (%s)",
                        get_uid(),
                        TwoPhaseOutcome.stringForm(heuristicDecision),
                        arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier());
            }

            boolean returnCurrentStatus = false;

            if (reportHeuristics || (!reportHeuristics && !TxControl.asyncCommit))
                returnCurrentStatus = true;

            if (returnCurrentStatus)
            {
                if (reportHeuristics)
                {
                    switch (heuristicDecision)
                    {
                        case TwoPhaseOutcome.PREPARE_OK:
                        case TwoPhaseOutcome.FINISH_OK:
                            break;
                        case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
                            return ActionStatus.H_ROLLBACK;
                        case TwoPhaseOutcome.HEURISTIC_COMMIT:
                            return ActionStatus.H_COMMIT;
                        case TwoPhaseOutcome.HEURISTIC_MIXED:
                            return ActionStatus.H_MIXED;
                        case TwoPhaseOutcome.HEURISTIC_HAZARD:
                        default:
                            return ActionStatus.H_HAZARD;
                    }
                }

                /*
                 * If we have a heuristic decision then we only report it
                 * if required. Otherwise we return committed as per OTS rules.
                 */

                switch (actionStatus)
                {
                    case ActionStatus.H_COMMIT:
                    case ActionStatus.H_ROLLBACK:
                    case ActionStatus.H_HAZARD:
                    case ActionStatus.H_MIXED:
                        if (!reportHeuristics)
                            return ActionStatus.COMMITTED;
                    default:
                        return actionStatus;
                }
            }
            else
                return ActionStatus.COMMITTING; // if asynchronous then fake it.
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * This is the user callable abort operation. It is invoked prior to the
     * start of two-phase commit and hence only processes records in the
     * pendingList (the other lists should be empty).
     *
     * Does not change the calling thread's notion of the current transaction.
     *
     * @return <code>ActionStatus</code> indicating outcome.
     */
    protected int Abort () {
        synchronizationLock.lock();

        try {
            return Abort(false);
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * This is the user callable abort operation. It is invoked prior to the
     * start of two-phase commit and hence only processes records in the
     * pendingList (the other lists should be empty).
     *
     * Does not change the calling thread's notion of the current transaction.
     *
     * @param applicationAbort indicates whether or not this is an application abort
     *
     * @return <code>ActionStatus</code> indicating outcome.
     */
    protected int Abort (boolean applicationAbort)
    {
        synchronizationLock.lock();

        try {
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("BasicAction::Abort() for action-id "
                        + get_uid());
            }

            /* Check for superfluous invocation */

            if ((actionStatus != ActionStatus.RUNNING)
                    && (actionStatus != ActionStatus.ABORT_ONLY)
                    && (actionStatus != ActionStatus.COMMITTING)) {
                switch (actionStatus) {
                    case ActionStatus.CREATED:
                        tsLogger.i18NLogger.warn_coordinator_BasicAction_39(get_uid());
                        break;
                    case ActionStatus.ABORTED:
                        tsLogger.i18NLogger.warn_coordinator_BasicAction_40(get_uid());
                        break;
                    default:
                        tsLogger.i18NLogger.warn_coordinator_BasicAction_41(get_uid());
                        break;
                }

                return actionStatus;
            }

            /*
             * Check we are the current action. Abort parents if not true. Some
             * implementations may want to override this.
             */

            checkIsCurrent();

            /*
             * Check we have no children (threads or actions).
             */

            checkChildren(false);

            if (pendingList != null)
            {
                actionStatus = ActionStatus.ABORTING;

                while (pendingList.size() > 0)
                    doAbort(pendingList, false); // turn off heuristics reporting

                /*
                 * In case we get here because an End has failed. In this case we
                 * still need to tell the heuristic resources to forget their
                 * decision.
                 */

                forgetHeuristics();
            }

            ActionManager.manager().remove(get_uid());

            actionStatus = ActionStatus.ABORTED;

            if (TxStats.enabled()) {
                TxStats.getInstance().incrementAbortedTransactions();

                if (applicationAbort)
                    TxStats.getInstance().incrementApplicationRollbacks();
            }

            return actionStatus;
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * Create a transaction of the specified type.
     */

    protected BasicAction (int at)
    {
        super(ObjectType.NEITHER);

        pendingList = null;
        preparedList = null;
        readonlyList = null;
        failedList = null;
        heuristicList = null;

        currentHierarchy = null;
        transactionStore = null;
        savedIntentionList = false;

        actionStatus = ActionStatus.CREATED;
        actionType = at;
        parentAction = null;
        recordBeingHandled = null;

        heuristicDecision = TwoPhaseOutcome.PREPARE_OK;
        _checkedAction = arjPropertyManager
                .getCoordinatorEnvironmentBean().getCheckedActionFactory()
                .getCheckedAction(get_uid(), type());

        _childThreads = null;
        _childActions = null;
    }

    /**
     * Recreate the specified transaction. Used for crash recovery purposes.
     */

    protected BasicAction (Uid u, int at)
    {
        super(u, ObjectType.NEITHER);

        pendingList = null;
        preparedList = null;
        readonlyList = null;
        failedList = null;
        heuristicList = null;

        currentHierarchy = null;
        transactionStore = null;
        savedIntentionList = false;

        actionStatus = ActionStatus.CREATED;
        actionType = at;
        parentAction = null;
        recordBeingHandled = null;

        heuristicDecision = TwoPhaseOutcome.PREPARE_OK;
        _checkedAction = arjPropertyManager
                .getCoordinatorEnvironmentBean().getCheckedActionFactory()
                .getCheckedAction(get_uid(), type());

        _childThreads = null;
        _childActions = null;
    }

    /**
     * Defines the start of a critical region by setting the critical flag. If
     * the signal handler is called the class variable abortAndExit is set. The
     * value of this variable is checked in the corresponding operation to end
     * the critical region.
     */

    protected final void criticalStart ()
    {
        //	_lock.lock();
    }

    /**
     * Defines the end of a critical region by resetting the critical flag. If
     * the signal handler is called the class variable abortAndExit is set. The
     * value of this variable is checked when ending the critical region.
     */

    protected final void criticalEnd ()
    {
        //	_lock.unlock();
    }

    /**
     * Cleanup phase for actions. If an action is in the PREPARED state when a
     * terminate signal is delivered (ie the coordinator node has crashed) then
     * we need to cleanup. This is essentially the same as phase2Abort but we
     * call cleanup ops rather than abort ops and let the records take care of
     * appropriate cleanup.
     *
     * The pendingList is processed because it may not be empty - since
     * prepare() stops processing the list at the first PREPARE_NOTOK result.
     *
     * The read_only list is processed to ensure that actions are aborted
     * immediately and any servers killed at that point since they need not hang
     * around. This contrasts with commit where readonlyList entries are simply
     * merged with the parent list or discarded
     */

    protected final void phase2Cleanup ()
    {
        synchronizationLock.lock();

        try {
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("BasicAction::phase2Cleanup() for action-id "
                        + get_uid());
            }

            criticalStart();

            actionStatus = ActionStatus.CLEANUP;

            while ((preparedList != null) && (preparedList.size() > 0))
                doCleanup(preparedList);

            while ((readonlyList != null) && (readonlyList.size() > 0))
                doCleanup(readonlyList);

            while ((pendingList != null) && (pendingList.size() > 0))
                doCleanup(pendingList);

            criticalEnd();
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * Second phase of the two-phase commit protocol for committing actions.
     * Phase 2 should only be called if phase 1 has completed successfully.
     * This operation first invokes the doCommit operation on the preparedList.
     * This ensures that the appropriate commit operation is performed on each
     * entry which is then either deleted (top_level) or merged into the
     * parent's pendingList.
     *
     * Processing of the readonlyList is different in that if the action is
     * top_level then all records in the readonlyList are deleted without
     * further processing. If nested the records must be merged. This is an
     * optimisation to avoid unnecessary processing.
     *
     * Note that at this point the pendingList SHOULD be empty due to the prior
     * invocation of prepare().
     *
     * @throws Error JBTM-895 tests, byteman limitation
     */

    protected final void phase2Commit (boolean reportHeuristics) throws Error
    {
        synchronizationLock.lock();

        try {
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("BasicAction::phase2Commit() for action-id "
                        + get_uid());
            }

            if ((pendingList != null) && (pendingList.size() > 0)) {
                int size = ((pendingList == null) ? 0 : pendingList.size());

                tsLogger.i18NLogger.warn_coordinator_BasicAction_42(get_uid(), Integer.toString(size), pendingList.toString());

                phase2Abort(reportHeuristics);
            }
            else
            {
                Long startTime = TxStats.enabled() ? System.nanoTime() : null;

                criticalStart();

                actionStatus = ActionStatus.COMMITTING;

                /*
                 * If we get a heuristic during commit then we continue to commit
                 * since we may have already told some records to commit. We could
                 * optimise this if the first record raises the heuristic by
                 * aborting (or going with the heuristic decision).
                 */

                doCommit(preparedList, reportHeuristics); /*
                 * process the
                 * preparedList
                 */

                /*
                 * Now check any heuristic decision. If we received one then we may
                 * have to raise HEURISTIC_MIXED since we will have committed some
                 * resources, whereas others may have aborted.
                 */

                if (heuristicDecision != TwoPhaseOutcome.PREPARE_OK)
                {
                    /*
                     * Heuristic decision matched the actual outcome!
                     */

                    if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_COMMIT)
                        heuristicDecision = TwoPhaseOutcome.FINISH_OK;
                }

                /* The readonlyList requires special attention */

                if ((readonlyList != null) && (readonlyList.size() > 0))
                {
                    if (!TxControl.readonlyOptimisation)
                    {
                        if (readonlyList != null)
                            doCommit(readonlyList, reportHeuristics);
                    }

                    // now still process the list.

                    while (((recordBeingHandled = readonlyList.getFront()) != null))
                    {
                        if ((actionType == ActionType.NESTED)
                                && (recordBeingHandled.propagateOnCommit()))
                        {
                            merge(recordBeingHandled);
                        }
                        else
                        {
                            recordBeingHandled = null;
                        }
                    }
                }

                forgetHeuristics();

                actionStatus = ActionStatus.COMMITTED;

                updateState();

                ActionManager.manager().remove(get_uid());

                criticalEnd();

                // ok count this as a commit unless we got a heuristic rollback in which case phase2Abort
                // will have been called and will already have counted it as an abort

                if (TxStats.enabled()) {
                    if (heuristicDecision != TwoPhaseOutcome.HEURISTIC_ROLLBACK) {
                        // NB statistics monitoring could have been dynamically enabled after starting this transaction
                        if (startTime == null)
                            TxStats.getInstance().incrementCommittedTransactions(0L);
                        else
                            TxStats.getInstance().incrementCommittedTransactions(System.nanoTime() - startTime);
                    }
                }

            }
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * Second phase of the two phase commit protocol for aborting actions.
     * Actions are aborted by invoking the doAbort operation on the
     * preparedList, the readonlyList, and the pendingList.
     *
     * The pendingList is processed because it may not be empty - since
     * prepare() stops processing the list at the first PREPARE_NOTOK result.
     *
     * By default, records that responsed PREPARE_READONLY will not be contacted
     * during second-phase abort, just as they are not during second-phase
     * commit. This can be overridden at runtime using the READONLY_OPTIMISATION
     * variable.
     */

    protected final void phase2Abort (boolean reportHeuristics)
    {
        synchronizationLock.lock();

        try {
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("BasicAction::phase2Abort() for action-id "
                        + get_uid());
            }

            criticalStart();

            actionStatus = ActionStatus.ABORTING;

            if (preparedList != null)
                doAbort(preparedList, reportHeuristics);

            if (!TxControl.readonlyOptimisation)
            {
                if (readonlyList != null)
                    doAbort(readonlyList, reportHeuristics);
            }

            if (pendingList != null)
                doAbort(pendingList, reportHeuristics);

            /*
             * Check heuristic decision, and try to make it match outcome.
             */

            if (heuristicDecision != TwoPhaseOutcome.PREPARE_OK)
            {
                if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
                    heuristicDecision = TwoPhaseOutcome.FINISH_OK;
            }

            forgetHeuristics();

            actionStatus = abortStatus();

            updateState(); // we may end up saving more than the heuristic list
            // here!

            ActionManager.manager().remove(get_uid());

            criticalEnd();

            /*
             * To get to this stage we had to try to commit, which means that we're
             * rolling back because of a resource problem or an internal error.
             */

            if (TxStats.enabled()) {
                if (internalError)
                    TxStats.getInstance().incrementSystemRollbacks();
                else
                    TxStats.getInstance().incrementResourceRollbacks();
                TxStats.getInstance().incrementAbortedTransactions();
            }
        } finally {
            synchronizationLock.unlock();
        }
    }

    /*
     * Various optimisations which are possible during synchronous prepare are
     * not possible for asynchronous prepare due to lack of ordering. For instance
     * dynamically determining one-phase optimisation while running through the
     * intentions list and getting read-only responses from N-1 participants.
     */

    protected int async_prepare(boolean reportHeuristics) {
        int p = TwoPhaseOutcome.PREPARE_OK;
        Collection<AbstractRecord> lastResourceRecords = new ArrayList<AbstractRecord>();
        Collection<Future<Integer>> tasks = new ArrayList<Future<Integer>>();

        AbstractRecord last2PCAwareRecord = pendingList.getRear();

        // Get 1PC aware resources
        while (last2PCAwareRecord != null && last2PCAwareRecord.typeIs() == RecordType.LASTRESOURCE) {
            lastResourceRecords.add(last2PCAwareRecord);
            last2PCAwareRecord = pendingList.getRear();
        }

        // Prepare 2PC aware resources
        while (pendingList.size() != 0) {
            tasks.add(TwoPhaseCommitThreadPool.submitJob(new AsyncPrepare(this, reportHeuristics, pendingList.getFront())));
        }

        // Prepare the last (or only) 2PC aware resource on the callers thread
        if (last2PCAwareRecord != null) {
            p = doPrepare(reportHeuristics, last2PCAwareRecord);
        }

        // Get results of the 2PC aware resources prepare
        for (Future<Integer> task : tasks) {
            try {
                int outcome = task.get();

                if (p == TwoPhaseOutcome.PREPARE_OK) {
                    p = outcome;
                }
            } catch (InterruptedException | ExecutionException e) {
                tsLogger.i18NLogger.unexpected_exception(e);
            }
        }

        // Commit one phase aware resources
        for (AbstractRecord lastResourceRecord : lastResourceRecords) {
            if (p == TwoPhaseOutcome.PREPARE_OK) {
                p = doPrepare(reportHeuristics, lastResourceRecord);
            } else {
                // Prepare failed, put remaining records back to the pendingList.
                pendingList.insert(lastResourceRecord);
            }
        }

        return p;
    }

    /**
     * Phase one of a two phase commit protocol. This function returns the
     * ouctome of the prepare operation. If all goes well it will be PREPARE_OK,
     * if not PREPARE_NOTOK. The value PREPARE_READONLY may also be returned if
     * all the records indicate that they are readonly records. Such records do
     * not take part in the second phase commit processing.
     *
     * @return <code>TwoPhaseOutcome</code> indicating outcome. Note that if
     * 1PC optimisation is enabled then it is possible for prepare to dynamically
     * optimise and commit if the first N-1 participants return read-only, causing the
     * protcol to commit the last participant rather than go through prepare.
     */

    protected final int prepare (boolean reportHeuristics)
    {
        synchronizationLock.lock();

        try {
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("BasicAction::prepare () for action-id "
                        + get_uid());
            }

            boolean commitAllowed = (actionStatus != ActionStatus.ABORT_ONLY);

            actionStatus = ActionStatus.PREPARING;

            /* If we cannot commit - say the prepare failed */

            if (!commitAllowed) {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_43(get_uid());

                actionStatus = ActionStatus.PREPARED;

                return TwoPhaseOutcome.PREPARE_NOTOK;
            }

            /*
             * Make sure the object store is set up for a top-level atomic action.
             */

            if (actionType == ActionType.TOP_LEVEL)
            {
                if (getStore() == null)
                {
                    actionStatus = ActionStatus.ABORT_ONLY;
                    internalError = true;
                    return TwoPhaseOutcome.PREPARE_NOTOK;
                }
            }

            criticalStart();

            createPreparedLists();

            /*
             * Here is the start of the hard work. Walk down the pendingList
             * invoking the appropriate prepare operation. If it succeeds put the
             * record on either the preparedList or the read_only list and continue
             * until the pendingList is exhausted.
             *
             * If prepare fails on any record stop processing immediately and put
             * the offending record back on the pendingList
             */

            int p = TwoPhaseOutcome.PREPARE_OK;

            /*
             * If asynchronous prepare, then spawn a separate thread to handle each
             * entry in the intentions list. Could have some configurable option to
             * allow more limited number of threads to divide up the intentions
             * list.
             */

            if ((actionType == ActionType.TOP_LEVEL) && (TxControl.asyncPrepare))
            {
                p = async_prepare(reportHeuristics);
            }
            else
            {
                // single threaded prepare

                // createPreparedLists will have ensured list exists, but it may be empty
                if(pendingList.size() > 0) {
                    p = doPrepare(reportHeuristics);
                }
            }

            /*
             * Now let's see if we are able to dynamically optimise 1PC. As we went
             * through prepare, if the first N-1 participants returned read-only
             * then we returned read-only from doPrepare but left one entry on
             * the intentions list.
             */

            if ((p == TwoPhaseOutcome.PREPARE_READONLY) && (pendingList.size() == 1))
            {
                onePhaseCommit(reportHeuristics, false);

                ActionManager.manager().remove(get_uid());

                return actionStatus == ActionStatus.ABORTED
                        ? TwoPhaseOutcome.ONE_PHASE_ERROR : TwoPhaseOutcome.PREPARE_ONE_PHASE_COMMITTED;
            }

            if ((p != TwoPhaseOutcome.PREPARE_OK)
                    && (p != TwoPhaseOutcome.PREPARE_READONLY))
            {
                if ((actionType == ActionType.NESTED)
                        && ((preparedList.size() > 0) && (p == TwoPhaseOutcome.ONE_PHASE_ERROR)))
                {
                    /*
                     * For the OTS we must merge those records told to commit with
                     * the parent, as the rollback invocation must come from that
                     * since they have already been told this transaction has
                     * committed!
                     */

                    AbstractRecord tmpRec = preparedList.getFront();

                    while (tmpRec != null)
                    {
                        merge(tmpRec);
                        tmpRec = preparedList.getFront();
                    }

                    if (parentAction != null)
                        parentAction.preventCommit();
                    else {
                        tsLogger.i18NLogger.warn_coordinator_BasicAction_44();
                    }
                }

                criticalEnd();

                return TwoPhaseOutcome.PREPARE_NOTOK;
            }

            /*
             * Now work out whether there is any state to save. Since we should be
             * single threaded once again, there is no need to protect the lists
             * with a synchronization.
             */

            /*
             * Could do this as we traverse the lists above, but would need some
             * compound class for return values.
             */

            boolean stateToSave = false;
            RecordListIterator iter = new RecordListIterator(preparedList);

            /*
             * First check the prepared list.
             */

            while (((recordBeingHandled = iter.iterate()) != null))
            {
                if (!stateToSave)
                    stateToSave = recordBeingHandled.doSave();

                if (stateToSave)
                    break;
            }

            iter = null;

            if (!stateToSave)
            {
                iter = new RecordListIterator(heuristicList);

                /*
                 * Now check the heuristic list.
                 */

                while (((recordBeingHandled = heuristicList.getFront()) != null))
                {
                    if (!stateToSave)
                        stateToSave = recordBeingHandled.doSave();

                    if (stateToSave)
                        break;
                }

                iter = null;
            }

            /*
             * The actual state we want to write depends upon whether or not we are
             * in charge of the transaction outcome:
             *
             * (i) if we are a root transaction, or an interposed transaction which
             * received a commit_one_phase call, then we have complete control over
             * what the transaction outcome will be. So, we will always try to
             * commit, and can set the state to committing.
             *
             * (ii) if we are an interposed transaction and it receives a complete
             * two-phase protocol, then the root is in control. So, we set the state
             * to prepared.
             *
             * (iii) nested transactions never write state, so the state is set to
             * prepared anyway.
             */

            if (actionType == ActionType.TOP_LEVEL)
                actionStatus = preparedStatus();
            else
                actionStatus = ActionStatus.PREPARED;

            /*
             * If we are here then everything went okay so save the intention list
             * in the ObjectStore in case of a node crash providing that its not
             * empty
             */

            if ((actionType == ActionType.TOP_LEVEL) && (stateToSave)
                    && ((preparedList.size() > 0) || (heuristicList.size() > 0)))
            {
                /* Only do this if we have some records worth saving! */

                Uid u = getSavingUid();
                String tn = type();
                OutputObjectState state = new OutputObjectState(u, tn);

                if (!save_state(state, ObjectType.ANDPERSISTENT)) {
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_45(get_uid());

                    criticalEnd();

                    internalError = true;

                    return TwoPhaseOutcome.PREPARE_NOTOK;
                }

                if (state.notempty())
                {
                    try
                    {
                        if (!transactionStore.write_committed(u, tn, state)) {
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_46(get_uid());

                            criticalEnd();

                            internalError = true;

                            return TwoPhaseOutcome.PREPARE_NOTOK;
                        }
                        else
                            savedIntentionList = true;
                    }
                    catch (ObjectStoreException e)
                    {
                        criticalEnd();

                        internalError = true;

                        return TwoPhaseOutcome.PREPARE_NOTOK;
                    }
                }
            }

            criticalEnd();

            if ((preparedList.size() == 0) && (readonlyList.size() >= 0))
                return TwoPhaseOutcome.PREPARE_READONLY;
            else
                return TwoPhaseOutcome.PREPARE_OK;
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * There is only one record on the intentions list. Only called from
     * synchronized methods. Don't bother about creating separate threads here!
     */
    protected void onePhaseCommit (boolean reportHeuristics)
    {
        onePhaseCommit(reportHeuristics, true);
    }

    /**
     * See {@link #onePhaseCommit(boolean)}
     *
     * This method may be called during 2PC prepare when the first resource returns read-only.
     */
    protected void onePhaseCommit (boolean reportHeuristics, boolean permitReportStatistics)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::onePhaseCommit() for action-id "
                    + get_uid());
        }

        /* Are we forced to abort? */

        if (actionStatus == ActionStatus.ABORT_ONLY) {
            tsLogger.i18NLogger.warn_coordinator_BasicAction_43(get_uid());

            Abort();

            return;
        }

        Long startTime = TxStats.enabled() ? System.nanoTime() : null;

        actionStatus = ActionStatus.COMMITTING;

        criticalStart();

        if ((heuristicList == null) && reportHeuristics)
            heuristicList = new RecordList();

        if (failedList == null)
            failedList = new RecordList();

        /*
         * Since it is one-phase, the outcome from the record is the outcome of
         * the transaction. Therefore, we don't need to save much intermediary
         * transaction state - only heuristics in the case of interposition.
         */

        boolean stateToSave = false;

        recordBeingHandled = pendingList.getFront();

        int p = ((actionType == ActionType.TOP_LEVEL) ? recordBeingHandled.topLevelOnePhaseCommit()
                : recordBeingHandled.nestedOnePhaseCommit());

        if ((p == TwoPhaseOutcome.FINISH_OK)
                || (p == TwoPhaseOutcome.PREPARE_READONLY))
        {
            if ((actionType == ActionType.NESTED)
                    && recordBeingHandled.propagateOnCommit())
            {
                merge(recordBeingHandled);
            }
            else
            {
                recordBeingHandled = null;
            }

            actionStatus = ActionStatus.COMMITTED;
        }
        else
        {
            if ((p == TwoPhaseOutcome.FINISH_ERROR) || (p == TwoPhaseOutcome.ONE_PHASE_ERROR))
            {
                /*
                 * If ONE_PHASE_ERROR then the resource has rolled back. Otherwise we
                 * don't know and will ask recovery to keep trying. We differentiate
                 * this kind of failure from a heuristic failure so that we can allow
                 * recovery to retry the commit attempt periodically.
                 */

                if (p == TwoPhaseOutcome.ONE_PHASE_ERROR) {
                    addDeferredThrowables(recordBeingHandled, deferredThrowables);
                }

                if (p == TwoPhaseOutcome.FINISH_ERROR)
                {
                    /*
                     * We still add to the failed list because this may not mean
                     * that the transaction has aborted.
                     */

                    if (!failedList.insert(recordBeingHandled))
                        recordBeingHandled = null;
                    else
                    {
                        addDeferredThrowables(recordBeingHandled, deferredThrowables);
                        if (!stateToSave)
                            stateToSave = recordBeingHandled.doSave();
                    }

                    /*
                     * There's been a problem and we need to retry later. Assume
                     * transaction has committed until we have further information.
                     * This also ensures that recovery will kick in periodically.
                     */

                    actionStatus = ActionStatus.COMMITTED;
                }
                else
                    actionStatus = ActionStatus.ABORTED;
            }
            else {
                /*
                 * Heuristic decision!!
                 */

                tsLogger.i18NLogger.warn_coordinator_BasicAction_47(get_uid(), TwoPhaseOutcome.stringForm(p));

                if (reportHeuristics) {
                    updateHeuristic(p, true);

                    if (!heuristicList.insert(recordBeingHandled))
                        recordBeingHandled = null;
                    else {
                        addDeferredThrowables(recordBeingHandled, deferredThrowables);
                        if (!stateToSave)
                            stateToSave = recordBeingHandled.doSave();
                    }
                }

                if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_ROLLBACK) {
                    /*
                     * Signal that the action outcome is the same as the
                     * heuristic decision.
                     */

                    heuristicDecision = TwoPhaseOutcome.PREPARE_OK; // means no
                    // heuristic
                    // was
                    // raised.

                    actionStatus = ActionStatus.ABORTED;
                } else if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_COMMIT) {
                    heuristicDecision = TwoPhaseOutcome.PREPARE_OK;
                    actionStatus = ActionStatus.COMMITTED;
                } else
                    actionStatus = ActionStatus.H_HAZARD; // can't really say
                // (could have
                // aborted)
            }
        }

        if (actionType == ActionType.TOP_LEVEL)
        {
            if (stateToSave && ((heuristicList.size() > 0) || (failedList.size() > 0)))
            {
                if (getStore() == null)
                {
                    tsLogger.i18NLogger.fatal_coordinator_BasicAction_48();

                    throw new com.arjuna.ats.arjuna.exceptions.FatalError(
                            tsLogger.i18NLogger.get_coordinator_BasicAction_69()
                                    + get_uid());
                }

                updateState();
            }
        }

        forgetHeuristics();

        ActionManager.manager().remove(get_uid());

        criticalEnd();

        if (TxStats.enabled() && permitReportStatistics) {
            if (actionStatus == ActionStatus.ABORTED) {
                TxStats.getInstance().incrementAbortedTransactions();
            } else {
                if (startTime == null)
                    TxStats.getInstance().incrementCommittedTransactions(0L);
                else
                    TxStats.getInstance().incrementCommittedTransactions(System.nanoTime() - startTime);
            }
        }

    }

    /**
     * @return the current heuristic decision. Each time a heuristic outcome is
     *         received, we need to merge it with any previous outcome to
     *         determine what the overall heuristic decision is (e.g., a
     *         heuristic rollback followed by a heuristic commit means the
     *         overall decision is heuristic mixed.)
     */

    protected final int getHeuristicDecision ()
    {
        synchronizationLock.lock();

        try {
            return heuristicDecision;
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * WARNING: use with extreme care!
     */

    protected final void setHeuristicDecision (int p)
    {
        synchronizationLock.lock();

        try {
            heuristicDecision = p;
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * Add the specified abstract record to the transaction. Does not do any of
     * the runtime checking of BasicAction.add, so should be used with care.
     * Currently used by crash recovery.
     */

    protected final void addRecord (AbstractRecord A)
    {
        synchronizationLock.lock();

        try {
            preparedList.insert(A);
        } finally {
            synchronizationLock.unlock();
        }
    }

    /**
     * @return the transaction's prepared status.
     *
     * @since JTS 2.0.
     */

    protected int preparedStatus ()
    {
        if (actionType == ActionType.TOP_LEVEL)
            return ActionStatus.COMMITTING;
        else
            return ActionStatus.PREPARED;
    }

    protected int abortStatus ()
    {
        return ActionStatus.ABORTED;
    }

    protected int commitStatus ()
    {
        return ActionStatus.COMMITTED;
    }


    /*
     * The single-threaded version of doPrepare. If we do not use asynchronous
     * prepare, then we don't need to lock the RecordLists - only one thread can
     * access them anyway!
     */
    private int doPrepare (boolean reportHeuristics)
    {
        /*
         * Here is the start of the hard work. Walk down the pendingList
         * invoking the appropriate prepare operation. If it succeeds put the
         * record on either the preparedList or the read_only list and continue
         * until the pendingList is exhausted.
         *
         * If prepare fails on any record stop processing immediately and put
         * the offending record back on the pendingList.
         */

        int overallTwoPhaseOutcome = TwoPhaseOutcome.PREPARE_READONLY;

        /*
         * March down the pendingList and pass the head of the list to the
         * main work routine until either we run out of elements, or one of
         * them fails.
         */
        boolean keepGoing = true;
        AbstractRecord prevLastResource = null;
        while(pendingList.size() > 0 && keepGoing) {
            AbstractRecord record = pendingList.getFront();
            /*
             * If a failure occurs then the record will be put back on to
             * the pending list. Otherwise it is moved to another list or
             * dropped if readonly.
             */
            boolean isLastResource = record.typeIs() == RecordType.LASTRESOURCE;
            if (isLastResource) {
                record.clearAnyCachedData();
            }

            if (isLastResource && prevLastResource == null) {
                // remember that we've processed a last resource so that if we have two such last resources
                // we can detect and report heuristics caused by allowing multiple last resources
                prevLastResource = record;
            }
            int individualTwoPhaseOutcome = doPrepare(reportHeuristics, record);

            if (individualTwoPhaseOutcome == TwoPhaseOutcome.ONE_PHASE_ERROR &&
                    isLastResource && // could probably infer isLastResource being true because of the ONE_PHASE_ERROR
                    record != prevLastResource) {
                /*
                 * Since prevLastResource, which is a last resource, committed (otherwise the decision would
                 * have switched to rollback) and the current record did not (because record != prevLastResource
                 * and the ONE_PHASE_ERROR) there is a mixed outcome.
                 * 
                 * The individual and overall outcome will be TwoPhaseOutcome.ONE_PHASE_ERROR so the
                 * two phase participants will be told to rollback
                 */
                tsLogger.i18NLogger.warn_coordinator_BasicAction_71(get_uid(),
                        TwoPhaseOutcome.stringForm(individualTwoPhaseOutcome));

                LastResourceRecord lrr = (LastResourceRecord) record; // the cast is safe because isLastResource == true

                // the resource should report the mixed outcome during rollback
                lrr.setOutcome(TwoPhaseOutcome.HEURISTIC_MIXED);
            }

            if(individualTwoPhaseOutcome != TwoPhaseOutcome.PREPARE_READONLY) {
                overallTwoPhaseOutcome = individualTwoPhaseOutcome;
            }

            keepGoing = ( individualTwoPhaseOutcome == TwoPhaseOutcome.PREPARE_OK) || ( individualTwoPhaseOutcome == TwoPhaseOutcome.PREPARE_READONLY);

            /*
             * If we are allowed to do dynamic 1PC optimisation then check to see if the first N-1
             * participants returned read-only and there's a single entry left on the
             * intentions list.
             */

            if (!subordinate && keepGoing && TxControl.dynamic1PC)
            {
                /*
                 * If N-1 returned read-only and 1 record left then exit prepare now and force
                 * a call to commitOnePhase on the last record.
                 */

                if ((pendingList.size() == 1) && (overallTwoPhaseOutcome == TwoPhaseOutcome.PREPARE_READONLY))
                    keepGoing = false;
            }
        }

        return overallTwoPhaseOutcome;
    }

    /*
     * The multi-threaded version of doPrepare. Each thread was given the record
     * it should process when it was created so that if a failure occurs we can
     * put it back onto the pendingList at the right place. It also cuts down on
     * the amount of synchronisation we must do.
     */

    protected int doPrepare (boolean reportHeuristics, AbstractRecord record)
    {
        /*
         * Here is the start of the hard work. Walk down the pendingList
         * invoking the appropriate prepare operation. If it succeeds put the
         * record on either the preparedList or the read_only list and continue
         * until the pendingList is exhausted.
         *
         * If prepare fails on any record stop processing immediately and put
         * the offending record back on the pendingList.
         */

        int p = TwoPhaseOutcome.PREPARE_NOTOK;

        p = ((actionType == ActionType.TOP_LEVEL) ? record.topLevelPrepare()
                : record.nestedPrepare());

        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.tracef(
                    "BasicAction::doPrepare() result for action-id (%s) on record id: (%s) is (%s) node id: (%s)",
                    get_uid(), record.order(), TwoPhaseOutcome.stringForm(p),
                    arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier());
        }

        if (p == TwoPhaseOutcome.PREPARE_OK)
        {
            record = insertRecord(preparedList, record);
        }
        else
        {
            if (p == TwoPhaseOutcome.PREPARE_READONLY)
            {
                record = insertRecord(readonlyList, record);
            }
            else
            {
                if ((p == TwoPhaseOutcome.PREPARE_NOTOK)
                        || (p == TwoPhaseOutcome.ONE_PHASE_ERROR)
                        || (!reportHeuristics))
                {
                    /*
                     * If we are a subtransaction and this is an OTS
                     * resource then we may be in trouble: we may have
                     * already told other records to commit.
                     */

                    if (actionType == ActionType.NESTED)
                    {
                        if ((preparedList.size() > 0)
                                && (p == TwoPhaseOutcome.ONE_PHASE_ERROR)) {
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_49(get_uid());

                            /*
                             * Force parent to rollback. If this is not the
                             * desired result then we may need to check some
                             * environment variable (either here or in the
                             * OTS) and act accordingly. If we check in the
                             * OTS then we need to return something other
                             * than PREPARE_NOTOK.
                             */

                            /*
                             * For the OTS we must merge those records told
                             * to commit with the parent, as the rollback
                             * invocation must come from that since they
                             * have already been told this transaction has
                             * committed!
                             *
                             * However, since we may be multi-threaded
                             * (asynchronous prepare) we don't do the
                             * merging yet. Wait until all threads have
                             * terminated and then do it.
                             *
                             * Therefore, can't force parent to rollback
                             * state at present, or merge will fail.
                             */
                        }
                    }

                    addDeferredThrowables(record, deferredThrowables);

                    /*
                     * Prepare on this record failed - we are in trouble.
                     * Add the record back onto the pendingList and return.
                     */

                    record = insertRecord(pendingList, record);

                    record = null;

                    actionStatus = ActionStatus.PREPARED;

                    return p;
                }
                else {
                    /*
                     * Heuristic decision!!
                     */

                    /*
                     * Only report if request to do so.
                     */

                    tsLogger.i18NLogger.warn_coordinator_BasicAction_50(get_uid(), TwoPhaseOutcome.stringForm(p));

                    if (reportHeuristics)
                        updateHeuristic(p, false);

                    addDeferredThrowables(record, deferredThrowables);

                    /*
                     * Don't add to the prepared list. We process heuristics
                     * separately during phase 2. The processing of records
                     * will not be in the same order as during phase 1, but
                     * does this matter for heuristic decisions? If so, then
                     * we need to modify RecordList so that records can
                     * appear on multiple lists at the same time.
                     */

                    record = insertRecord(heuristicList, record);

                    /*
                     * If we have had a heuristic decision, then attempt to
                     * make the action outcome the same. If we have a
                     * conflict, then we will abort.
                     */

                    if (heuristicDecision != TwoPhaseOutcome.HEURISTIC_COMMIT) {
                        actionStatus = ActionStatus.PREPARED;

                        return TwoPhaseOutcome.PREPARE_NOTOK;
                    } else {
                        /*
                         * Heuristic commit, which is ok since we want to
                         * commit anyway! So, ignore it (but remember the
                         * resource so we can tell it to forget later.)
                         */
                    }
                }
            }
        }

        return p;
    }

    /**
     * Walk down a record list extracting records and calling the appropriate
     * commit function. Discard or merge records as appropriate
     */

    protected int doCommit (RecordList rl, boolean reportHeuristics)
    {
        if ((rl != null) && (rl.size() > 0))
        {
            AbstractRecord rec;

            while (((rec = rl.getFront()) != null))
            {
                int outcome = doCommit(reportHeuristics, rec);

                /*
                 * Check the outcome and if we have a heuristic rollback try to
                 * rollback everything else in the list *if* we have not already
                 * committed something. That way we make the outcome for all
                 * participants the same as the first (rollback) and don't get a
                 * heuristic!
                 */

                switch (outcome)
                {
                    case TwoPhaseOutcome.FINISH_OK:
                    case TwoPhaseOutcome.HEURISTIC_COMMIT:
                        pastFirstParticipant = true;
                        break;
                    case TwoPhaseOutcome.HEURISTIC_MIXED:
                    case TwoPhaseOutcome.HEURISTIC_HAZARD:
                    default:
                        /*
                         * Do nothing and continue to commit everything else. We've
                         * got this far as errors have caused problems, but we gain
                         * nothing by now rolling back some participants. This could
                         * cause further heuristics!
                         */

                        pastFirstParticipant = true;
                        break;
                    case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
                    {
                        /*
                         * A heuristic decision of commit means that we have got
                         * past the first entry in the list. So there is no going
                         * back now!
                         */

                        if (pastFirstParticipant)
                            break;
                        else
                        {
                            /*
                             * Remember the heuristic decision so we can restore it
                             * after rolling back. Otherwise we can't return the
                             * right value from commit.
                             */

                            pastFirstParticipant = true;

                            int oldDecision = heuristicDecision;

                            phase2Abort(reportHeuristics);

                            heuristicDecision = oldDecision;
                        }
                    }
                    break;
                }
            }
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    protected int doCommit (boolean reportHeuristics, AbstractRecord record)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::doCommit ("
                    + record + ")");
        }

        /*
         * To get heuristics right, as soon as we manage to commit the first
         * record we set the heuristic to HEURISTIC_COMMIT. Then, if any other
         * heuristics are raised we can manage the final outcome correctly.
         */

        int ok = TwoPhaseOutcome.FINISH_ERROR;

        recordBeingHandled = record;

        if (recordBeingHandled != null)
        {
            if (actionType == ActionType.TOP_LEVEL)
            {
                if ((ok = recordBeingHandled.topLevelCommit()) == TwoPhaseOutcome.FINISH_OK)
                {
                    /*
                     * Record successfully committed, we can delete it now.
                     */

                    recordBeingHandled = null;

                    updateHeuristic(TwoPhaseOutcome.FINISH_OK, true); // must
                    // remember
                    // that
                    // something
                    // has
                    // committed
                }
                else
                {
                    if (tsLogger.logger.isTraceEnabled()) {
                        tsLogger.logger.trace("BasicAction.doCommit for "+get_uid()+" received "+
                                TwoPhaseOutcome.stringForm(ok)+" from "+RecordType.typeToClass(recordBeingHandled.typeIs()));
                    }

                    if ((reportHeuristics)
                            && ((ok == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
                            || (ok == TwoPhaseOutcome.HEURISTIC_COMMIT)
                            || (ok == TwoPhaseOutcome.HEURISTIC_MIXED) || (ok == TwoPhaseOutcome.HEURISTIC_HAZARD)))
                    {
                        updateHeuristic(ok, true);
                        heuristicList.insert(recordBeingHandled);
                        addDeferredThrowables(recordBeingHandled, deferredThrowables);
                    }
                    else
                    {
                        if (ok == TwoPhaseOutcome.NOT_PREPARED)
                        {
                            /*
                             * If this is the first resource then rollback,
                             * otherwise promote to HEURISTIC_HAZARD, but don't
                             * add to heuristicList.
                             */

                            updateHeuristic(TwoPhaseOutcome.HEURISTIC_HAZARD, true);
                        }
                        else
                        {
                            /*
                             * The commit failed. Add this record to the failed
                             * list to indicate this. Covers statuses like FAILED_ERROR.
                             */


                            if ((ok == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
                                    || (ok == TwoPhaseOutcome.HEURISTIC_COMMIT)
                                    || (ok == TwoPhaseOutcome.HEURISTIC_MIXED) || (ok == TwoPhaseOutcome.HEURISTIC_HAZARD))
                            {
                                updateHeuristic(ok, true);
                            }

                            failedList.insert(recordBeingHandled);
                            addDeferredThrowables(recordBeingHandled, deferredThrowables);
                        }
                    }
                }
            }
            else
            {
                /*
                 * Thankfully nested actions cannot raise heuristics!
                 */

                ok = recordBeingHandled.nestedCommit();

                if (recordBeingHandled.propagateOnCommit())
                {
                    merge(recordBeingHandled);
                }
                else
                {
                    recordBeingHandled = null;
                }
            }

            if (ok != TwoPhaseOutcome.FINISH_OK)
            {
                /* Preserve error messages */
            }

            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.tracef(
                        "BasicAction::doCommit() result for action-id (%s) on record id: (%s) is (%s) node id: (%s)",
                        get_uid(), record.order(), TwoPhaseOutcome.stringForm(ok),
                        arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier());
            }

        }

        return ok;
    }

    /*
     * Walk down a record list extracting records and calling the appropriate
     * abort function. Discard records when done.
     */

    protected int doAbort (RecordList list_toprocess, boolean reportHeuristics)
    {
        if ((list_toprocess != null) && (list_toprocess.size() > 0))
        {
            while ((recordBeingHandled = list_toprocess.getFront()) != null)
            {
                doAbort(reportHeuristics, recordBeingHandled);
            }
        }

        return TwoPhaseOutcome.FINISH_OK;
    }

    protected int doAbort (boolean reportHeuristics, AbstractRecord record)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::doAbort ("
                    + record + ")");
        }

        int ok = TwoPhaseOutcome.FINISH_OK;

        recordBeingHandled = record;

        if (recordBeingHandled != null)
        {
            if (actionType == ActionType.TOP_LEVEL)
                ok = recordBeingHandled.topLevelAbort();
            else
                ok = recordBeingHandled.nestedAbort();

            if ((actionType != ActionType.TOP_LEVEL)
                    && (recordBeingHandled.propagateOnAbort()))
            {
                merge(recordBeingHandled);
            }
            else
            {
                if (ok == TwoPhaseOutcome.FINISH_OK)
                {
                    updateHeuristic(TwoPhaseOutcome.FINISH_OK, false); // remember
                    // that
                    // something
                    // aborted
                    // ok
                }
                else
                {
                    if ((reportHeuristics)
                            && ((ok == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
                            || (ok == TwoPhaseOutcome.HEURISTIC_COMMIT)
                            || (ok == TwoPhaseOutcome.HEURISTIC_MIXED) || (ok == TwoPhaseOutcome.HEURISTIC_HAZARD))) {
                        if (actionType == ActionType.TOP_LEVEL)
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_52(get_uid(), TwoPhaseOutcome.stringForm(ok));
                        else
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_53(get_uid(), TwoPhaseOutcome.stringForm(ok));

                        updateHeuristic(ok, false);
                        heuristicList.insert(recordBeingHandled);
                        addDeferredThrowables(recordBeingHandled, deferredThrowables);
                    }
                    else
                    {
                        if (ok != TwoPhaseOutcome.FINISH_OK) {
                            if (actionType == ActionType.TOP_LEVEL)
                                tsLogger.i18NLogger.warn_coordinator_BasicAction_54(get_uid(),
                                        TwoPhaseOutcome.stringForm(ok),
                                        RecordType.typeToClass(recordBeingHandled.typeIs()).getCanonicalName());
                            else
                                tsLogger.i18NLogger.warn_coordinator_BasicAction_55(get_uid(),
                                        TwoPhaseOutcome.stringForm(ok),
                                        RecordType.typeToClass(recordBeingHandled.typeIs()).getCanonicalName());
                        }
                    }
                }

                /*
                 * Don't need a canDelete as in the C++ version since Java's
                 * garbage collection will deal with things for us.
                 */

                recordBeingHandled = null;
            }

            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.tracef(
                        "BasicAction::doAbort() result for action-id (%s) on record id: (%s) is (%s) node id: (%s)",
                        get_uid(), record.order(), TwoPhaseOutcome.stringForm(ok),
                        arjPropertyManager.getCoreEnvironmentBean().getNodeIdentifier());
            }

        }

        return ok;
    }

    protected AbstractRecord insertRecord (RecordList reclist, AbstractRecord record)
    {
        boolean lock = TxControl.asyncPrepare;

        if (lock)
        {
            synchronized (reclist) {
                if (!reclist.insert(record))
                    record = null;
            }
        }
        else
        {
            if (!reclist.insert(record))
                record = null;
        }

        return record;
    }

    /**
     * Do we want to check that a transaction can only be terminated by a thread
     * that has it as its current transaction? The base class has this check
     * enabled (i.e., we check), but some implementations may wish to override
     * this.
     *
     * @return <code>false</code> to disable checking.
     */

    protected boolean checkForCurrent ()
    {
        return false;
    }

    /*
     * If we get a single heuristic then we will always rollback during prepare.
     *
     * Getting a heuristic during commit is slightly different, since some
     * resources may have already committed, changing the type of heuristic we
     * may need to throw. However, once we get to commit we know that it will be
     * the final outcome. So, as soon as a single resource commits successfully,
     * we can take it as a HEURISTIC_COMMIT. We will forget a HEURISTIC_COMMIT
     * outcome at the end anyway.
     */

    protected final void updateHeuristic (int p, boolean commit)
    {
        /*
         * Some resource has prepared/committed ok, so we need to remember this
         * in case we get a future heuristic.
         */

        synchronizationLock.lock();

        try {
            if (p == TwoPhaseOutcome.FINISH_OK)
            {
                if (commit)
                {
                    if (heuristicDecision == TwoPhaseOutcome.PREPARE_OK)
                        p = TwoPhaseOutcome.HEURISTIC_COMMIT;

                    if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
                        heuristicDecision = TwoPhaseOutcome.HEURISTIC_MIXED;
                }
                else
                {
                    if (heuristicDecision == TwoPhaseOutcome.PREPARE_OK)
                        p = TwoPhaseOutcome.HEURISTIC_ROLLBACK;

                    if (heuristicDecision == TwoPhaseOutcome.HEURISTIC_COMMIT)
                        heuristicDecision = TwoPhaseOutcome.HEURISTIC_MIXED;
                }

                // leave HAZARD and MIXED alone
            } else {
                if (TxStats.enabled())
                    TxStats.getInstance().incrementHeuristics();
            }

            /*
             * Is this the first heuristic? Always give HEURISTIC_MIXED priority,
             * but if we have no heuristic and we get a HEURISTIC_HAZARD then go
             * with that until something better comes along!
             */

            /*
             * Have we already been given a conflicting heuristic? If so, raise the
             * decision to the next heuristic level.
             */

            switch (heuristicDecision)
            {
                case TwoPhaseOutcome.PREPARE_OK:
                    if ((p != TwoPhaseOutcome.PREPARE_OK)
                            && (p != TwoPhaseOutcome.FINISH_OK)) // first heuristic
                        // outcome.
                        heuristicDecision = p;
                    break;
                case TwoPhaseOutcome.HEURISTIC_COMMIT:
                    if ((p == TwoPhaseOutcome.HEURISTIC_ROLLBACK)
                            || (p == TwoPhaseOutcome.HEURISTIC_MIXED))
                        heuristicDecision = TwoPhaseOutcome.HEURISTIC_MIXED;
                    else
                    {
                        if (p == TwoPhaseOutcome.HEURISTIC_HAZARD)
                            heuristicDecision = TwoPhaseOutcome.HEURISTIC_HAZARD;
                    }
                    break;
                case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
                    if ((p == TwoPhaseOutcome.HEURISTIC_COMMIT)
                            || (p == TwoPhaseOutcome.HEURISTIC_MIXED))
                        heuristicDecision = TwoPhaseOutcome.HEURISTIC_MIXED;
                    else
                    {
                        if (p == TwoPhaseOutcome.HEURISTIC_HAZARD)
                            heuristicDecision = TwoPhaseOutcome.HEURISTIC_HAZARD;
                    }
                    break;
                case TwoPhaseOutcome.HEURISTIC_HAZARD:
                    if (p == TwoPhaseOutcome.HEURISTIC_MIXED)
                        heuristicDecision = TwoPhaseOutcome.HEURISTIC_MIXED;
                    break;
                case TwoPhaseOutcome.HEURISTIC_MIXED:
                    break;
                default:
                    heuristicDecision = p; // anything!
                    break;
            }
        } finally {
            synchronizationLock.unlock();
        }
    }

    protected void updateState ()
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::updateState() for action-id "
                    + get_uid());
        }

        /*
         * If the action is topLevel then prepare() will have written the
         * intention_list to the object_store. If any of the phase2Commit
         * processing failed then records will exist on the failedList. If this
         * is the case then we need to re-write the BasicAction record in the
         * object store. If the failed list is empty we can simply delete the
         * BasicAction record.
         */

        if (actionType == ActionType.TOP_LEVEL)
        {
            /*
             * make sure the object store is set up for a top-level atomic
             * action.
             */

            getStore();

            /*
             * If we have failures then rewrite the intentions list. Otherwise,
             * delete the log entry. Depending upon how we get here the intentions
             * list will either be in the preparedList or the failedList. Fortunately
             * save_state will figure out which one to use.
             */

            if (((failedList != null) && (failedList.size() > 0))
                    || ((heuristicList != null) && (heuristicList.size() > 0))
                    || ((preparedList != null) && (preparedList.size() > 0)))
            {
                /*
                 * Re-write the BasicAction record with the failed list
                 */

                Uid u = getSavingUid();
                String tn = type();
                OutputObjectState state = new OutputObjectState(u, tn);

                if (!save_state(state, ObjectType.ANDPERSISTENT)) {
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_64();

                    // what else?
                }

                if (state.notempty())
                {
                    try
                    {
                        if (!transactionStore.write_committed(u, tn, state)) {
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_65();
                        }
                    }
                    catch (ObjectStoreException e)
                    {
                        // just log a warning since the intentions list has already been written
                        tsLogger.logger.warn(e);
                    }
                }
            }
            else
            {
                try
                {
                    if (savedIntentionList)
                    {
                        if (transactionStore.remove_committed(getSavingUid(), type()))
                        {
                            savedIntentionList = false;
                        }
                    }
                }
                catch (ObjectStoreException e) {
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_70(e);
                }
            }
        }
    }

    /*
     * This is only meant as an instance cut of the children, so don't lock the
     * entire transaction. Thus, the list may change before we return.
     */

    private final void createPreparedLists ()
    {
        if (preparedList == null)
            preparedList = new RecordList();

        if (readonlyList == null)
            readonlyList = new RecordList();

        if (failedList == null)
            failedList = new RecordList();

        if (heuristicList == null)
            heuristicList = new RecordList();

        if (pendingList == null)
            pendingList = new RecordList();
    }

    /**
     * Check to see if this transaction is the one that is current for this
     * thread. If it isn't, then we mark this transaction as rollback only.
     *
     * @return <code>true</code> if the transaction is current,
     *         <code>false</code> otherwise.
     */

    private final boolean checkIsCurrent ()
    {
        boolean isCurrent = true;

        if (checkForCurrent())
        {
            BasicAction currentAct = BasicAction.Current();

            /* Ensure I am the currently active action */

            if ((currentAct != null) && (currentAct != this)) {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_56(currentAct.get_uid(), get_uid());

                isCurrent = false;

                if (currentAct.isAncestor(get_uid())) {
                    /* current action is one of my children */

                    BasicAction parentAct = parent();

                    /* prevent commit of my parents (ensures safety) */

                    while (parentAct != null) {
                        parentAct.preventCommit();
                        parentAct = parentAct.parent();
                    }
                }
            }

            currentAct = null;
        }

        return isCurrent;
    }

    // called by the reaper system via TwoPhaseCoordinator.recordStackTraces
    protected void recordStackTraces() {
        synchronizationLock.lock();

        try {
            Map<String,String> currentCapture = createStackTraces();
            StackTraceCapture stackTraceCapture = new StackTraceCapture(System.currentTimeMillis(), currentCapture);
            threadStackTraceHistoryList.add(stackTraceCapture);
        } finally {
            synchronizationLock.unlock();
        }
    }

    // called by the reaper system via TwoPhaseCoordinator.outputCapturedStackTraces
    protected void outputCapturedStackTraces() {
        synchronizationLock.lock();

        try {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
            for(StackTraceCapture stackTraceCapture : threadStackTraceHistoryList) {
                for(Map.Entry<String,String> entry : stackTraceCapture.getStackTraces().entrySet()) {
                    tsLogger.i18NLogger.info_historic_stack_trace(objectUid, entry.getKey(), dateFormat.format(new Date(stackTraceCapture.getTime())), entry.getValue());
                }
            }
        } finally {
            synchronizationLock.unlock();
        }
    }

    protected Map<String,String> createStackTraces() {
        synchronizationLock.lock();

        try {
            Map<String,String> results = new HashMap<>();
            if (_childThreads == null) return results;
            for (Thread entry : _childThreads.values()) {
                StackTraceElement[] stackTrace = entry.getStackTrace();
                StringBuilder sb = new StringBuilder();
                for (StackTraceElement element : stackTrace) {
                    sb.append(element.toString());
                    sb.append("\n");
                }
                results.put(entry.getName(), sb.toString());
            }
            return results;
        } finally {
            synchronizationLock.unlock();
        }
    }

    private final boolean checkChildren (boolean isCommit)
    {
        boolean problem = false;

        /*
         * If we have child threads then by default we just print a warning and
         * continue. The other threads will eventually find out the outcome.
         */

        if ((_childThreads != null) && (_childThreads.size() > 0))
        {
            if ((_childThreads.size() != 1)
                    || ((_childThreads.size() == 1) && (!_childThreads.contains(Thread.currentThread())))) {
                /*
                 * More than one thread or the one thread is not the current
                 * thread
                 */

                if (isCommit) {
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_57(get_uid());
                } else {
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_58(get_uid());

                    // This was added for JBTM-2476 - it could be used during commit
                    // but as the commit path needs to be optimum and getStackTrace is expensive
                    // I did not include it
                    for(Map.Entry<String,String> entry : createStackTraces().entrySet()) {
                        tsLogger.i18NLogger.warn_multiple_threads(objectUid, entry.getKey(), entry.getValue());
                    }
                }

                if (_checkedAction != null)
                    _checkedAction.check(isCommit, get_uid(), _childThreads);

                removeAllChildThreads();
            }
        }

        /* Ensure I have no child actions */

        if ((_childActions != null) && (_childActions.size() > 0))
        {
            problem = true;

            Enumeration<BasicAction> iter = _childActions.elements();
            BasicAction child = null;
            boolean printError = true;

            /*
             * We may have already aborted our children, e.g., because of an
             * out-of-sequence commit, so we check here to reduce the number of
             * error messages!
             *
             * We can't just remove the children when we are finished with them
             * because BasicAction is not responsible for action tracking.
             */

            while (iter.hasMoreElements())
            {
                child = iter.nextElement();

                if (child.status() != ActionStatus.ABORTED) {
                    if (printError) {
                        if (isCommit) {
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_59(get_uid());
                        } else {
                            tsLogger.i18NLogger.warn_coordinator_BasicAction_60(get_uid());
                        }

                        printError = false;
                    }

                    tsLogger.i18NLogger.warn_coordinator_BasicAction_61(child.get_uid());

                    child.Abort();
                    child = null;
                }
            }

            iter = null;

            if (isCommit) {
                tsLogger.i18NLogger.warn_coordinator_BasicAction_62(((child != null ? child.get_uid().toString() : "null")));
            }
        }

        return problem;
    }

    /*
     * Just in case we are deleted/terminated with threads still registered. We
     * must make sure those threads don't try to remove themselves from this
     * action later. So we unregister them ourselves now.
     *
     * This is only called by End/Abort and so all child actions will have been
     * previously terminated as well.
     */

    private final void removeAllChildThreads ()
    {
        /*
         * Do not remove the current thread as it is committing/aborting!
         */

        criticalStart();

        if ((_childThreads != null) && (_childThreads.size() != 0))
        {
            Thread currentThread = Thread.currentThread();

            /*
             * Iterate through all registered threads and tell them to ignore
             * the action pointer, i.e., they are now no longer within this
             * action.
             */

            Enumeration<Thread> iter = _childThreads.elements();
            Thread t = null;

            while (iter.hasMoreElements())
            {
                t = iter.nextElement();

                if (tsLogger.logger.isTraceEnabled()) {
                    tsLogger.logger.trace("BasicAction::removeAllChildThreads () action "+get_uid()+" removing "+t);
                }

                if (t != currentThread)
                    ThreadActionData.purgeAction(this, t);
            }
        }

        criticalEnd();
    }

    /**
     * actionInitialise determines whether the BasicAction is a nested,
     * top-level, or a top-level nested atomic action
     */

    private final void actionInitialise (BasicAction parent)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::actionInitialise() for action-id "
                    + get_uid());
        }

        criticalStart();

        if (parent != null) /* ie not top_level */
        {
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("Action "+get_uid()+" with parent status "+parent.actionStatus);
            }

            currentHierarchy = new ActionHierarchy(parent.getHierarchy());
        }
        else
        {
            currentHierarchy = new ActionHierarchy(
                    ActionHierarchy.DEFAULT_HIERARCHY_DEPTH);

            /*
             * This is a top-level atomic action so set the signal handler block
             * a number of signals.
             */
        }

        currentHierarchy.add(get_uid(), actionType);

        switch (actionType)
        {
            case ActionType.TOP_LEVEL:
                if (parent != null)
                {
                    /*
                     * do not want to print warning all the time as this is what
                     * nested top-level actions are used for.
                     */

                    if (tsLogger.logger.isTraceEnabled()) {
                        tsLogger.logger.trace("Running Top Level Action "+get_uid()+" from within " +
                                "nested action ("+parent.get_uid()+")");
                    }
                }
                break;
            case ActionType.NESTED:
                if (parent == null)
                    actionType = ActionType.TOP_LEVEL;
                break;
        }

        parentAction = parent;

        criticalEnd();
    }

    private final void doForget (RecordList list_toprocess)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::doForget ("
                    + list_toprocess + ")");
        }

        /*
         * If the user has selected to maintain all heuristic information, then
         * we never explicitly tell resources to forget. We assume that the user
         * (or some management tool) will do this, and simply save as much
         * information as we can into the action state to allow them to do so.
         *
         * However, if we had a resource that returned a heuristic outcome and
         * we managed to make the outcome of this transaction the same as that
         * outcome, we removed the heuristic. So, we need to tell the resource
         * regardless, or it'll never be able to tidy up.
         */

        boolean force = (boolean) (heuristicDecision == TwoPhaseOutcome.FINISH_OK);

        if (!TxControl.maintainHeuristics || force)
        {
            if (list_toprocess.size() > 0)
            {
                RecordList tmpList = new RecordList();

                while (((recordBeingHandled = list_toprocess.getFront())) != null)
                {
                    /*
                     * Remember for later if we cannot tell it to forget.
                     */

                    if (recordBeingHandled.forgetHeuristic())
                        recordBeingHandled = null;
                    else
                        tmpList.putFront(recordBeingHandled);
                }

                /*
                 * Now put those resources we couldn't tell to forget back on
                 * the heuristic list.
                 */

                if (tmpList.size() > 0)
                {
                    while ((recordBeingHandled = tmpList.getFront()) != null)
                        list_toprocess.putFront(recordBeingHandled);
                }
            }
        }
    }

    /*
     * Walk down a record list extracting records and calling the appropriate
     * cleanup function. Discard records when done. NOTE: We only need to do
     * cleanup at top level since cleanup at nested level would be subsumed when
     * the parent action is forced to abort
     *
     * Ignore heuristics. Who can we report them to?
     *
     * This routine is called by phase2Cleanup, which gets called only in
     * exceptional circumstances. By default we leave cleaning up the various
     * lists until the action instance goes out of scope.
     */

    private final void doCleanup (RecordList list_toprocess)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("BasicAction::doCleanup ("
                    + list_toprocess + ")");
        }

        if (list_toprocess.size() > 0)
        {
            int ok = TwoPhaseOutcome.FINISH_OK;

            while (((recordBeingHandled = list_toprocess.getFront()) != null))
            {
                if (actionType == ActionType.TOP_LEVEL)
                    ok = recordBeingHandled.topLevelCleanup();
                else
                    ok = recordBeingHandled.nestedCleanup();

                if ((actionType != ActionType.TOP_LEVEL)
                        && (recordBeingHandled.propagateOnAbort()))
                {
                    merge(recordBeingHandled);
                }
                else
                {
                    if (ok != TwoPhaseOutcome.FINISH_OK)
                    {
                        /* Preserve error messages */
                    }

                    recordBeingHandled = null;
                }
            }
        }
    }

    private final boolean doOnePhase ()
    {
        synchronizationLock.lock();

        try {
            if (TxControl.onePhase)
            {
                if(pendingList == null) {
                    return true;
                }
                if (pendingList.size() == 1) {
                    return pendingList.peekFront().isPermittedTopLevelOnePhaseCommit();
                }
            }
            return false;
        } finally {
            synchronizationLock.unlock();
        }
    }

    /*
     * Operation to merge a record into those held by the parent BasicAction.
     * This is accomplished by invoking the add operation of the parent
     * BasicAction. If the add operation does not return AR_ADDED, the record is
     * deleted
     */

    private final void merge (AbstractRecord A)
    {
        synchronizationLock.lock();

        try {
            int as;

            if ((as = parentAction.add(A)) != AddOutcome.AR_ADDED)
            {
                A = null;

                if (as == AddOutcome.AR_REJECTED)
                    tsLogger.i18NLogger.warn_coordinator_BasicAction_68();
            }
        } finally {
            synchronizationLock.unlock();
        }
    }

    /* Adds the deferred throwables of the given record to the given list of throwables. */

    private void addDeferredThrowables(AbstractRecord record, List<Throwable> throwables)
    {
        if (record instanceof ExceptionDeferrer)
            ((ExceptionDeferrer) record).getDeferredThrowables(throwables);
        else if (record.value() instanceof ExceptionDeferrer)
            ((ExceptionDeferrer) record.value()).getDeferredThrowables(throwables);
    }

    /* These (genuine) lists hold the abstract records */

    protected RecordList pendingList;
    protected RecordList preparedList;
    protected RecordList readonlyList;
    protected RecordList failedList;
    protected RecordList heuristicList;
    protected boolean savedIntentionList;

    private ActionHierarchy currentHierarchy;
    private ParticipantStore transactionStore;  // a ParticipantStore is also a TxLog

    //    private boolean savedIntentionList;

    /* Atomic action status variables */

    private volatile int actionStatus;
    private int actionType;
    private BasicAction parentAction;
    private AbstractRecord recordBeingHandled;
    private int heuristicDecision;
    private CheckedAction _checkedAction; // control what happens if threads active when terminating.
    private boolean pastFirstParticipant;  // remember where we are (were) in committing during recovery
    private boolean internalError; // is there an error internal to the TM (such as write log errors, for example)

    /*
     * We need to keep track of the number of threads associated with each
     * action. Since we can't override the basic thread methods, we have to
     * provide an explicit means of registering threads with an action.
     */

    private Hashtable<String, Thread> _childThreads;
    private Hashtable<BasicAction, BasicAction> _childActions;

    private BasicActionFinalizer finalizerObject;
    private static final boolean finalizeBasicActions = arjPropertyManager.getCoordinatorEnvironmentBean().isFinalizeBasicActions();

    //    private Mutex _lock = new Mutex(); // TODO
    private List<Throwable> deferredThrowables = new ArrayList<>();

    protected boolean subordinate;

    protected final List<StackTraceCapture> threadStackTraceHistoryList = new ArrayList<>();
}

// a point in time snapshot of N Thread stacks, see threadStackTraceHistoryList and makeStackTraces
class StackTraceCapture {
    private final long time;
    private final Map<String,String> stackTraces;

    public StackTraceCapture(long time, Map<String, String> stackTraces) {
        this.time = time;
        this.stackTraces = stackTraces;
    }

    public long getTime() {
        return time;
    }

    public Map<String, String> getStackTraces() {
        return stackTraces;
    }
}

class BasicActionFinalizer
{
    private final BasicAction basicAction;

    BasicActionFinalizer(BasicAction basicAction)
    {
        this.basicAction = basicAction;
    }

    @Override
    protected void finalize() throws Throwable
    {
        basicAction.finalizeInternal();
    }
}
