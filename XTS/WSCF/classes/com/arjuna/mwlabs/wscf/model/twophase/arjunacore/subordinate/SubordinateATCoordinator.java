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

package com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.*;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Collection;

/**
 * This class represents a specific coordination instance. It is essentially an
 * ArjunaCore TwoPhaseCoordinator, which gives us access to two-phase with
 * synchronization support but without thread management. This is the
 * subordinate coordinator implementation which we use when doing
 * interposition.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SubordinateATCoordinator.java,v 1.1 2005/05/19 12:13:39 nmcl Exp $
 * @since 2.0.
 */

public class SubordinateATCoordinator extends ATCoordinator
{
    /**
     * normal constructor
     */
	public SubordinateATCoordinator()
	{
		super();
		subordinate = true;
        activated = true;
        isReadonly = false;
	}

    /**
     * bridge wrapper constructor
     */
	public SubordinateATCoordinator(String subordinateType)
	{
		super();
        subordinate = true;
        activated = true;
        isReadonly = false;
        this.subordinateType = subordinateType;
	}

    /**
     * constructor for recovered coordinator
     * @param recovery
     */
	public SubordinateATCoordinator(Uid recovery)
	{
		super(recovery);
        subordinate = true;
        activated = false;
        isReadonly = false;
        subordinateType = null;
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
	 * @exception WrongStateException
	 *                Thrown if the coordinator is in a state the does not allow
	 *                coordination to occur.
	 * @exception ProtocolViolationException
	 *                Thrown if the protocol is violated in some manner during
	 *                execution.
	 * @exception SystemException
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
	
	public int cancel ()
	{
		return ActionStatus.INVALID;
	}

    /**
     * this is driven by a volatile participant registered on behalf of the coordinator
     *
     * @return true if the beforeCompletion succeeds otherwise false.
     */
    public boolean prepareVolatile()
    {
        return super.beforeCompletion();
    }

    /**
     * this is driven by a durable participant registered on behalf of the coordinator and does a
     * normal prepare mninus the before completion processing which has already been performed
     * @return the result of preparing the transaction
     */

	public int prepare ()
	{
        int status = super.prepare(true);
        isReadonly = (status == TwoPhaseOutcome.PREPARE_READONLY);
        return status;
	}

    /**
     * this is driven by a volatile participant registered on behalf of the coordinator
     */
    public void commitVolatile()
    {
        if (isReadonly) {
            super.afterCompletion(ActionStatus.COMMITTED);
        } else {
            super.afterCompletion(finalStatus);
        }
    }

    /**
     * this is driven by a durable participant registered on behalf of the coordinator and does a
     * normal commit minus the after completion processing which will be driven by a volatile
     * participant also registerd for this coordinator..
     */

	public void commit ()
	{
		super.phase2Commit(true);

		int status;
		
		switch (super.getHeuristicDecision())
		{
		case TwoPhaseOutcome.PREPARE_OK:
		case TwoPhaseOutcome.FINISH_OK:
			status = super.status();		
			break;
		case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
			status = ActionStatus.H_ROLLBACK;
			break;
		case TwoPhaseOutcome.HEURISTIC_COMMIT:
			status = ActionStatus.H_COMMIT;
			break;
		case TwoPhaseOutcome.HEURISTIC_MIXED:
			status = ActionStatus.H_MIXED;
			break;
		case TwoPhaseOutcome.HEURISTIC_HAZARD:
		default:
			status = ActionStatus.H_HAZARD;
			break;
		}

        this.finalStatus = status;

        // if we have completed then remove the coordinator from the recovered coordinatros table
        if (status != ActionStatus.COMMITTING) {
            SubordinateATCoordinator.removeRecoveredCoordinator(this);
        }
        
        // run any callback associated with this transaction

        runCallback(get_uid().stringForm());

	}

    /**
     * this is driven by a volatile participant registered on behalf of the coordinator
     */
    public void rollbackVolatile()
    {
        if (isReadonly) {
            super.afterCompletion(ActionStatus.ABORTED);
        } else {
            super.afterCompletion(finalStatus);
        }
    }

    /**
     * this is driven by a durable participant registered on behalf of the coordinator and does a
     * normal commit minus the after completion processing which will be driven by a volatile
     * participant also registerd for this coordinator..
     */
	public void rollback ()
	{
		// as this coordinator could be called as normal XAResource by topLeveRecord
		// this rollback call could be part of the prevent commit when beforeCompletion fails
		// in such case the heuristicList was not created by prepare call and may throw NPE
		if(heuristicList == null)
		    heuristicList = new RecordList();

		super.phase2Abort(true);
		
		int status;
		
		switch (super.getHeuristicDecision())
		{
		case TwoPhaseOutcome.PREPARE_OK:
		case TwoPhaseOutcome.FINISH_OK:
			status = super.status();
			break;
		case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
			status = ActionStatus.H_ROLLBACK;
			break;
		case TwoPhaseOutcome.HEURISTIC_COMMIT:
			status = ActionStatus.H_COMMIT;
			break;
		case TwoPhaseOutcome.HEURISTIC_MIXED:
			status = ActionStatus.H_MIXED;
			break;
		case TwoPhaseOutcome.HEURISTIC_HAZARD:
		default:
			status = ActionStatus.H_HAZARD;
			break;
		}

        // iemove the coordinator from the recovered coordinatros table
        SubordinateATCoordinator.removeRecoveredCoordinator(this);

        // run any callback associated with this transaction

        runCallback(get_uid().stringForm());
        
        this.finalStatus = status;
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

    /**
     * type string used to locate TX log records in the tx object store hierarchy
     */
    public final static String TRANSACTION_TYPE = "/StateManager/BasicAction/AtomicAction/TwoPhaseCoordinator/TwoPhase/SubordinateATCoordinator";

    public String type ()
    {
        return TRANSACTION_TYPE;
    }

    /**
     * unique string used as prefix for participant ids to ensure they can be identified at recovery
     */
    public static String PARTICIPANT_PREFIX = "org.jboss.jbossts.xts.at.subordinate.participant.";

    /**
     * return a uid for the volatile participant registered on behalf of this corodinator
     */
    public String getVolatile2PhaseId()
    {
        return PARTICIPANT_PREFIX + get_uid().stringForm() + "_V";
    }

    /**
     * return a uid for the durable participant registered on behalf of this corodinator
     */
    public String getDurable2PhaseId()
    {
        return PARTICIPANT_PREFIX + get_uid().stringForm() + "_D";
    }


    protected static synchronized void addRecoveredCoordinator(SubordinateATCoordinator coordinator)
    {
        recoveredCoordinators.put(coordinator.get_uid().stringForm(), coordinator);
    }

    protected static synchronized void removeRecoveredCoordinator(SubordinateATCoordinator coordinator)
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
     * @return whether the at is orphaned
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

    public static synchronized SubordinateATCoordinator getRecoveredCoordinator(String coordinatorId)
    {
        return recoveredCoordinators.get(coordinatorId);
    }

    public static synchronized SubordinateATCoordinator[] listRecoveredCoordinators()
    {
        Collection<SubordinateATCoordinator> values = recoveredCoordinators.values();
        int length = values.size();
        return values.toArray(new SubordinateATCoordinator[length]);
    }

    /**
     * standard AT subordinate tx type for an AT subordinate created below another AT transaction
     */
    public static final String SUBORDINATE_TX_TYPE_AT_AT = "org.jboss.jbossts.xts.at.at.subordinate";

    public String getSubordinateType()
    {
        return subordinateType;
    }
    
    @Override
    public boolean save_state(OutputObjectState os, int ot) {
        // also need to save the subordinate type
        if (super.save_state(os, ot)) {
            try {
                os.packString(subordinateType);
                return true;
            } catch (IOException ioe) {
            }
        }

        return false;
    }

    @Override
    public boolean restore_state(InputObjectState os, int ot) {
        // also need to restore the subordinate type
        if (super.restore_state(os, ot)) {
            try {
                subordinateType = os.unpackString();
                return true;
            } catch (IOException ioe) {
            }
        }
        
        return false;
    }

    /**
     * this saves the status after the subtransaction commit or rollback so it can be referred to during
     * afterCompletion processing.
     */
    private int finalStatus = ActionStatus.CREATED;

    /**
     * flag identifying whether this coordinator is active, set true for normal transactions and false
     * for recovered transactions until they are activated
     */
    private boolean activated;

    /**
     * flag identifying whether prepare returned READ_ONLY and hence whether special case handling of
     * commitVolatile and rollbackVolatile calls is required
     */
    private boolean isReadonly;

    /**
     * string identifying which type of subordinate transaction this is. the standard subordinate type is
     * XTSATRecoveryManager.SUBORDINATE_TX_TYPE_AT_AT which identifies a subordinate of another AT transaction.
     * Alternative types can occur as a result of transaction bridging e.g. the AT transaction may be a
     * subordinate of an XA transaction. different types of subordinate can be scanned and rolled back
     * independently from other subordinate types.
     */

    private String subordinateType;

    private static final HashMap<String, SubordinateATCoordinator> recoveredCoordinators = new HashMap<String, SubordinateATCoordinator>();

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
