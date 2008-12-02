/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
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

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ACCoordinator;

/**
 * This class represents a specific coordination instance. It is essentially an
 * ArjunaCore TwoPhaseCoordinator, which gives us access to two-phase with
 * synchronization support but without thread management. This is the
 * subordinate coordinator implementation which we use when doing
 * interposition.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SubordinateCoordinator.java,v 1.1 2005/05/19 12:13:39 nmcl Exp $
 * @since 2.0.
 */

public class SubordinateCoordinator extends ACCoordinator
{
	
	public SubordinateCoordinator ()
	{
		super();
	}

	public SubordinateCoordinator (Uid recovery)
	{
		super(recovery);
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
        return super.prepare(true);
	}

    /**
     * this is driven by a volatile participant registered on behalf of the coordinator
     */
    public void commitVolatile()
    {
        super.afterCompletion(finalStatus);
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
	}

    /**
     * this is driven by a volatile participant registered on behalf of the coordinator
     */
    public void rollbackVolatile()
    {
        super.afterCompletion(finalStatus);
    }

    /**
     * this is driven by a durable participant registered on behalf of the coordinator and does a
     * normal commit minus the after completion processing which will be driven by a volatile
     * participant also registerd for this coordinator..
     */
	public void rollback ()
	{
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
     * return a uid for the volatile participant registered on behalf of this corodinator
     */
    public String getVolatile2PhaseId()
    {
        return get_uid().stringForm() + "_V";
    }

    /**
     * return a uid for the durable participant registered on behalf of this corodinator
     */
    public String getDurable2PhaseId()
    {
        return get_uid().stringForm() + "_D";
    }


    /**
     * this saves the status after the subtransaction commit or rollback so it can be referred to during
     * afterCompletion processing.
     */
    private int finalStatus = ActionStatus.CREATED;
}
