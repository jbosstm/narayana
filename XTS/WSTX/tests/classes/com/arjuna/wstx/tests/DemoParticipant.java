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
 * $Id: DemoParticipant.java,v 1.4 2004/03/15 13:25:26 nmcl Exp $
 */

package com.arjuna.wstx.tests;

import com.arjuna.mw.wstx.resource.Participant;

import com.arjuna.mw.wscf.common.Qualifier;

import com.arjuna.mw.wstx.common.*;

import com.arjuna.mw.wstx.status.*;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.status.*;

import com.arjuna.mw.wscf.model.twophase.status.*;

import com.arjuna.ats.arjuna.state.*;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;

import com.arjuna.mw.wstx.exceptions.HeuristicHazardException;
import com.arjuna.mw.wstx.exceptions.HeuristicMixedException;
import com.arjuna.mw.wstx.exceptions.HeuristicCommitException;
import com.arjuna.mw.wstx.exceptions.HeuristicRollbackException;
import com.arjuna.mw.wstx.exceptions.InvalidParticipantException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: DemoParticipant.java,v 1.4 2004/03/15 13:25:26 nmcl Exp $
 * @since 1.0.
 */

public class DemoParticipant implements Participant
{

    public DemoParticipant (TxId id)
    {
	_status = Active.instance();
	_passed = false;
	_id = id;
    }

    public final boolean passed ()
    {
	return _passed;
    }
    
    /**
     * Prepare the participant. This is to allow
     * an implementation to put a manager/factory entity in the loop
     * to (possibly) improve performance, for example. So, the entity
     * that implements this interface may be wrapping multiple
     * implementation objects.
     *
     * @param Qualifier[] qualifiers Any additional information.
     * @exception InvalidInferiorException Thrown if the inferior identity is invalid
     *            (e.g., refers to an unknown inferior.)
     * @exception WrongStateException Thrown if the state of the inferior is such that
     *            it cannot prepare.
     * @exception HeuristicHazardException Thrown if upon preparing, the inferior finds that
     *            some of its enlisted inferiors have return statuses which
     *            mean it cannot determine what the result of issuing prepare
     *            to them has been.
     * @exception HeuristicMixedException Thrown if upon preparing, the inferior finds that
     *            some of its enlisted inferiors have return statuses which
     *            mean some of them cancelled and some of them confirmed.
     * @exception SystemException Thrown if some other error occurred.
     * @return the vote.
     */
    
    public Vote prepare (Qualifier[] qualifiers) throws SystemException, InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException
    {
	System.out.println("DemoParticipant.prepare ( "+_id+" )");

	if (_status.equals(Active.instance()))
	{
	    _status = Prepared.instance();
	    
	    return new VoteCommit();
	}
	else
	    throw new WrongStateException();
    }

    /**
     * Confirm the inferior specified.
     *
     * @param Qualifier[] qualifiers Any additional information.
     * @exception InvalidInferiorException Thrown if the inferior identity is invalid
     *            (e.g., refers to an unknown inferior.)
     * @exception WrongStateException Thrown if the state of the inferior is such that
     *            it cannot confirm.
     * @exception HeuristicHazardException Thrown if upon preparing, the inferior finds that
     *            some of its enlisted inferiors have return statuses which
     *            mean it cannot determine what the result of issuing confirm
     *            to them has been.
     * @exception HeuristicMixedException Thrown if upon preparing, the inferior finds that
     *            some of its enlisted inferiors have return statuses which
     *            mean some of them cancelled and some of them confirmed.
     * @exception InferiorCancelledException Thrown if the inferior cancels
     * rather than confirms.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void commit (Qualifier[] qualifiers) throws SystemException, InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicRollbackException
    {
	System.out.println("DemoParticipant.commit ( "+_id+" )");

	if (!_status.equals(Prepared.instance()))
	    throw new WrongStateException();
	else
	{
	    _status = Committed.instance();

	    _passed = true;
	}
    }

    public void commitOnePhase (Qualifier[] qualifiers) throws SystemException, InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicRollbackException
    {
	System.out.println("DemoParticipant.commitOnePhase ( "+_id+" )");

	if (_status.equals(Prepared.instance()))
	    throw new WrongStateException();
	else
	{
	    _status = Committed.instance();

	    _passed = true;
	}
    }
    
    /**
     * Cancel the inferior specified.
     *
     * @param Qualifier[] qualifiers Any additional information.
     * @exception InvalidInferiorException Thrown if the inferior identity is invalid
     *            (e.g., refers to an unknown inferior.)
     * @exception WrongStateException Thrown if the state of the inferior is such that
     *            it cannot cancel.
     * @exception HeuristicHazardException Thrown if upon preparing, the inferior finds that
     *            some of its enlisted inferiors have return statuses which
     *            mean it cannot determine what the result of issuing cancel
     *            to them has been.
     * @exception HeuristicMixedException Thrown if upon preparing, the inferior finds that
     *            some of its enlisted inferiors have return statuses which
     *            mean some of them cancelled and some of them confirmed.
     * @exception InferiorConfirmedException Thrown if the inferior confirms
     * rather than cancels.
     * @exception SystemException Thrown if some other error occurred.
     */

    public void rollback (Qualifier[] qualifiers) throws SystemException, InvalidParticipantException, WrongStateException, HeuristicHazardException, HeuristicMixedException, HeuristicCommitException
    {
	System.out.println("DemoParticipant.rollback ( "+_id+" )");

	if (!_status.equals(Prepared.instance()) || !_status.equals(Active.instance()))
	{
	    throw new WrongStateException();
	}
	else
	    _status = RolledBack.instance();
    }

    /**
     * Inform the specified inferior that it has generated a contradiction.
     *
     * @param Qualifier[] qualifiers Any additional qualifiers that may affect
     *                    the operation.
     * @exception InvalidInferiorException Thrown if the inferior identity is invalid.
     * @exception WrongStateException Thrown if the inferior is in an invalid state.
     * @exception SystemException Thrown in the event of a general fault.
     */

    public void forget (Qualifier[] qualifiers) throws SystemException, InvalidParticipantException, WrongStateException
    {
	System.out.println("DemoParticipant.forget ( "+_id+" )");
    }

    /**
     * @param Qualifier[] qualifiers Any additional qualifiers that may affect
     *                    the operation.
     * @exception InvalidInferiorException Thrown if the inferior identity is invalid.
     * @exception SystemException Thrown in the event of a general fault.
     * @return the current status of the specified inferior.
     */

    public Status status (Qualifier[] qualifiers) throws SystemException, InvalidParticipantException
    {
	System.out.println("DemoParticipant.status ( "+_id+" )");

	return _status;
    }

    /**
     * @return the name of this inferior.
     */

    public String name ()
    {
	return "DemoParticipant";
    }
    
    /**
     * These methods are required so that the coordinator can serialise and
     * de-serialise information about the inferior during completion and
     * recovery.
     */

    public boolean packState (OutputObjectState os)
    {
	return true;
    }

    public boolean unpackState (InputObjectState os)
    {
	return true;
    }

    private Status  _status;
    private boolean _passed;
    private TxId    _id;
    
}

