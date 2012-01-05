/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyritypeght.txt in the distribution for a full listing
 * of individual contributors.
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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SubordinateCoordinator.java,v 1.1 2005/05/19 12:13:39 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.sagas.arjunacore.subordinate;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.*;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.BACoordinator;

import java.util.HashMap;
import java.util.Collection;

/**
 * This class represents a specific coordination instance. It inherits from
 * ArjunaCore TwoPhaseCoordinator via the BACoordinator. This is the
 * subordinate coordinator implementation which we use when doing
 * interposition.
 *
 * This implementation registers itself as a coordinator completion participant in its parent
 * activity. It has to use the coordinator completion protocol because it relies upon dispatch
 * of a complete request from the parent to drive logging of i) the subordinate transaction state
 * then ii) the state of its registered proxy participant. These operations must both happen in that
 * order and only when the parent transaction is preparing to close. Note that this does not
 * imply that the transaction is closed, merely that it goes through phase 1 of the transaction
 * termination protocol (PREPARE) leaving a transaction log record on the disk (albeit in state
 * COMMITTING). It must still be possible to leave phase 2 open for either close (COMMIT)
 * or compensate (ROLLBACK).
 *
 * Why? Well, if the participant logs its state first either at parent complete or, if participant
 * completion is in use, before parent complete, then a crash risks losing details of the subordinate
 * transaction -- including potentialy completed participants which may require closing or compensating.
 * Note that this danger bypasses any question of whether or when subsequently the subordinate transaction
 * state might be logged -- the participant cannot log its state and guarantee to close or compensate
 * correctly without having first ensured that it can guarantee to close or compensate subordinate
 * participants.
 *
 * Contrariwise, if the subordinate transaction logs its state first and a crash occurs before the proxy
 * participant state can be saved then it is still possible during recovery to correlate the presence of
 * the logged subordinate transaction with the absence of its proxy participant and automatically
 * compensate the subordinate transaction's completed participants. This will match the action of the parent
 * since absence of the proxy participant requires it also to compensate. It is not possible to commit
 * any earlier than parent complete because it is still legitimate for participants to register before
 * that event.
 *
 * So, the implication of this is that when the proxy notifies complete to this coordinator it must
 * perform the usual complete processing and also drive phase 1 of the commit process. Later, when a
 * close request is dispatched, it can drive phase 2 of the commit process.
 *
 * If there is a failure during phase 1 of the commit process then the subordinate transaction will
 * not be logged. In this case the proxy participant must notify a fail to the parent transaction and
 * avoid logging its own state. This is safe because a crash will automatically cause a subsequent
 * resend of the complete request to return fail because the proxy participant is unknown.
 *
 * If phase 1 completes successfully then the proxy participant must log its state and notify completed
 * to the parent trasaction. If a crash occurs between logging the subordinate transaction state and
 * logging the proxy state then the absence of the proxy participant can be reconciled during recovery
 * and the subordinate transaction can be automatically compensated. If a crash occurs between logging the
 * proxy participant and notifying completed to the parent transaction then the participant will be recreated
 * during recovery and a subsequent resend of complete can be answered with completed. Note that this means
 * complete requests for unknown participants must only be answered after the first pass of the BA participant
 * recovery module has completed.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SubordinateCoordinator.java,v 1.1 2005/05/19 12:13:39 nmcl Exp $
 * @since 2.0.
 */

public class SubordinateBACoordinator extends BACoordinator
{
    // TODO - modify to use above protocol!

    /**
     * normal constructor
     */
	public SubordinateBACoordinator()
	{
		super();
        activated = true;
	}

    /**
     * constructor for recovered coordinator
     * @param recovery
     */
	public SubordinateBACoordinator(Uid recovery)
	{
		super(recovery);
        activated = false;
	}

    /**
	 * If the application requires and if the coordination protocol supports it,
	 * then this method can be used to execute a coordination protocol on the
	 * currently enlisted participants at any time prior to the termination of
	 * the coordination scope.
	 *
	 * This implementation only supports coordination at the end of the
	 * activity.
	 *
	 * @param     cs The completion status to use when determining how to
	 *            execute the protocol.
	 *
	 * @exception com.arjuna.mw.wsas.exceptions.WrongStateException
	 *                Thrown if the coordinator is in a state the does not allow
	 *                coordination to occur.
	 * @exception com.arjuna.mw.wsas.exceptions.ProtocolViolationException
	 *                Thrown if the protocol is violated in some manner during
	 *                execution.
	 * @exception com.arjuna.mw.wsas.exceptions.SystemException
	 *                Thrown if any other error occurs.
	 *
	 * @return The result of executing the protocol, or null.
	 */

	public Outcome coordinate (CompletionStatus cs) throws WrongStateException,
			ProtocolViolationException, SystemException
	{
		throw new ProtocolViolationException();
	}

	public int end (boolean reportHeuristics)
	{
		return ActionStatus.INVALID;
	}

    /**
     * this is driven by a coordinator-completion participant registered on behalf of the coordinator
     * and is required to propagate the complete to all registered coordinator-completion participants.
     * @return the result of preparing the transaction
     */

	public void complete ()  throws WrongStateException, SystemException
	{
        // if this goes wrong here then we will throw an exception

        super.complete();

        // now we need to run phase one of commit
        // TODO -- need to do completion processing here?

        int outcome = super.prepare(true);
        // if we have prepared ok (or are read only) then status will be COMMITTING

        if (outcome == TwoPhaseOutcome.PREPARE_NOTOK) {
            // phase 1 failed so we need to run phase 2 abort
            // this will set status to ABORTED

            phase2Abort(true);
        }

        // no need to return anything as the caller can just check the status
	}

    /**
     * this is driven by a coordinator-completion participant registered on behalf of the coordinator
     * and is required to propagate the close to all registered participants.
     */

	public int close () throws SystemException
	{
        int status = status();
        int result;

        if (status == ActionStatus.COMMITTING) {
            // TODO -- need to do completion processing here?
            // we already completed and ran phase 1 so do a phase 2 commit
            phase2Commit(true);
            result = status();
        } else {
            // we have not yet completed so we can rely upon the parent implementation to do
            // everything we need
            result = super.close();
        }

        // if we have completed then remove the coordinator from the recovered coordinators table
        if (status() != ActionStatus.COMMITTING) {
            SubordinateBACoordinator.removeRecoveredCoordinator(this);
        }

        // run any callback associated with this transaction
        runCallback(get_uid().stringForm());

        return result;
	}

    /**
     * this is driven by a coordinator-completion participant registered on behalf of the coordinator
     * and is required to propagate the cancel to all registered participants.
     */
	public int cancel ()
	{
        int status = status();
        int result;

        // TODO -- check if there is a window here where status could change to COMMITTING
        if (status == ActionStatus.COMMITTING) {
            phase2Abort(true);
            result = status();
        } else {
            result = super.cancel();
        }

        SubordinateBACoordinator.removeRecoveredCoordinator(this);

        // run any callback associated with this transaction
        runCallback(get_uid().stringForm());

        return result;
	}

    /**
     * called by the durable participant during recovery processing
     * TODO clarify when and why this gets called and what to do about it
     */
    public void unknown()
    {
    }

    /**
     * called by the durable participant during recovery processing
     * TODO clarify when and why this gets called and what to do about it
     */
    public void error()
    {
    }

    public String type ()
    {
        return "/StateManager/BasicAction/AtomicAction/Sagas/SubordinateCoordinator";
    }

    /**
     * unique string used as prefix for participant ids to ensure they can be identified at recovery
     */
    public static String PARTICIPANT_PREFIX = "org.jboss.jbossts.xts.ba.subordinate.participant.";

    /**
     * return a uid for the corodinator completion participant registered on behalf of this coordinator
     */
    public String getCoordinatorCompletionParticipantid()
    {
        return PARTICIPANT_PREFIX + get_uid().stringForm() + "_CCP";
    }

    protected static synchronized void addRecoveredCoordinator(SubordinateBACoordinator coordinator)
    {
        recoveredCoordinators.put(coordinator.get_uid().stringForm(), coordinator);
    }

    protected static synchronized void removeRecoveredCoordinator(SubordinateBACoordinator coordinator)
    {
        recoveredCoordinators.remove(coordinator.get_uid().stringForm());
    }

    public static synchronized void addActiveProxy(String id)
    {
        activeProxies.put(id, Boolean.TRUE);
    }

    public static synchronized void removeActiveProxy(String id)
    {
        activeProxies.remove(id);
    }

    protected void setActivated()
    {
        activated = true;
    }

    public boolean isActivated()
    {
        return activated;
    }

    /**
     * test whether a transaction has been restored without its proxy participant. this indicates that
     * we crashed between preparing the suborindate TX and logging the proxy participant.
     * @return
     */
    public boolean isOrphaned()
    {
        String id = get_uid().stringForm();
        if (isActiveProxy(id)) {
            return false;
        }

        // the proxy may have been removed because this tx has been resolved while we were checking

        if (getRecoveredCoordinator(id) == null) {
            return false;
        }

        // ok we have a tx but no proxy so this is really an orphan
        
        return true;
    }

    private static synchronized boolean isActiveProxy(String proxyId)
    {
        return activeProxies.get(proxyId) == Boolean.TRUE;
    }

    public static synchronized SubordinateBACoordinator getRecoveredCoordinator(String coordinatorId)
    {
        return recoveredCoordinators.get(coordinatorId);
    }

    public static synchronized SubordinateBACoordinator[] listRecoveredCoordinators()
    {
        Collection<SubordinateBACoordinator> values = recoveredCoordinators.values();
        int length = values.size();
        return values.toArray(new SubordinateBACoordinator[length]);
    }

    /**
     * flag identifying whether this coordinator is active, set true for normal transactions and false
     * for recovered transactions until they are activated
     */
    private boolean activated;

    private static final HashMap<String, SubordinateBACoordinator> recoveredCoordinators = new HashMap<String, SubordinateBACoordinator>();

    private static final HashMap<String, Boolean> activeProxies = new HashMap<String, Boolean>();

    /**
     * we need to remove the association between parent and subordinate context at completion
     * of commit or rollback -- we use a callback mechanism keyed by transaction id to achieve this
     */

    private static final HashMap<String, SubordinateCallback> callbacks = new HashMap<String, SubordinateCallback>();

    /**
     * class implemented by any code which wishes to register a callabck
     */
    public static abstract class SubordinateCallback
    {
        private SubordinateCallback next; // in case multiple callbacks are registered

        public abstract void run();
    }

    /**
     * register a callback to be called when a subordinate transaction with a specific key executes
     * a commit or rollback. the callback will not be called in the case of a crash
     * @param key
     * @param callback
     */
    public static void addCallback(String key, SubordinateCallback callback)
    {
        SubordinateCallback old = callbacks.put(key, callback);
        // chian any existign callback so we ensure to call them all
        callback.next = old;
    }

    private void runCallback(String key)
    {
        SubordinateCallback callback = callbacks.get(key);
        while (callback != null) {
            callback.run();
            callback = callback.next;
        }
    }
}