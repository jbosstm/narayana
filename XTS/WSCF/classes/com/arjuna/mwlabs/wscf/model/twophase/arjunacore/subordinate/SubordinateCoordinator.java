/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
	 * @param CompletionStatus
	 *            cs The completion status to use when determining how to
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
	
	public int doPrepare ()
	{
		if (super.beforeCompletion())
			return super.prepare(true);
		else
		{
			super.phase2Abort(true);
			
			return TwoPhaseOutcome.PREPARE_NOTOK;
		}
	}

	public int doCommit ()
	{
		super.phase2Commit(true);

		int toReturn;
		
		switch (super.getHeuristicDecision())
		{
		case TwoPhaseOutcome.PREPARE_OK:
		case TwoPhaseOutcome.FINISH_OK:
			toReturn = super.status();		
			break;
		case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
			toReturn = ActionStatus.H_ROLLBACK;
			break;
		case TwoPhaseOutcome.HEURISTIC_COMMIT:
			toReturn = ActionStatus.H_COMMIT;
			break;
		case TwoPhaseOutcome.HEURISTIC_MIXED:
			toReturn = ActionStatus.H_MIXED;
			break;
		case TwoPhaseOutcome.HEURISTIC_HAZARD:
		default:
			toReturn = ActionStatus.H_HAZARD;
			break;
		}
		
		super.afterCompletion(toReturn);
		
		return toReturn;
	}

	public int doRollback ()
	{
		super.phase2Abort(true);
		
		int toReturn;
		
		switch (super.getHeuristicDecision())
		{
		case TwoPhaseOutcome.PREPARE_OK:
		case TwoPhaseOutcome.FINISH_OK:
			toReturn = super.status();
			break;
		case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
			toReturn = ActionStatus.H_ROLLBACK;
			break;
		case TwoPhaseOutcome.HEURISTIC_COMMIT:
			toReturn = ActionStatus.H_COMMIT;
			break;
		case TwoPhaseOutcome.HEURISTIC_MIXED:
			toReturn = ActionStatus.H_MIXED;
			break;
		case TwoPhaseOutcome.HEURISTIC_HAZARD:
		default:
			toReturn = ActionStatus.H_HAZARD;
			break;
		}
		
		super.afterCompletion(toReturn);
		
		return toReturn;
	}

	public int doOnePhaseCommit ()
	{	
		return super.end(true);
	}
	
}
