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
 * $Id: JTAHLS.java,v 1.7 2005/05/19 12:13:33 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.as.coordinator.jta;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mwlabs.wscf.model.as.coordinator.jta.context.soap.JTAContextImple;

import com.arjuna.mw.wscf.model.as.coordinator.xa.outcomes.*;
import com.arjuna.mw.wscf.model.as.coordinator.xa.status.*;

import com.arjuna.mw.wscf.common.Qualifier;
import com.arjuna.mw.wscf.common.CoordinatorId;

import com.arjuna.mw.wscf.api.UserCoordinatorService;

import com.arjuna.mw.wscf.model.as.coordinator.CoordinatorManagerService;

import com.arjuna.mw.wscf.model.as.coordinator.Participant;
import com.arjuna.mw.wscf.model.as.coordinator.Coordinator;
import com.arjuna.mw.wscf.model.as.coordinator.Message;

import com.arjuna.mw.wsas.context.Context;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.activity.Outcome;
import com.arjuna.mw.wsas.activity.HLS;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;
import com.arjuna.mw.wsas.completionstatus.Success;
import com.arjuna.mw.wsas.completionstatus.Failure;

import com.arjuna.mwlabs.wsas.activity.ActivityHandleImple;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import com.arjuna.mw.wscf.exceptions.*;

import javax.transaction.*;
import javax.transaction.xa.*;

import java.util.Hashtable;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: JTAHLS.java,v 1.7 2005/05/19 12:13:33 nmcl Exp $
 * @since 1.0.
 */

public class JTAHLS implements HLS, CoordinatorManagerService, UserCoordinatorService
{

    public JTAHLS ()
    {
	synchronized (_coordinators)
	{
	    if (_theTransactionManagerImple == null)
	    {
		try
		{
		    _theTransactionManagerImple = (javax.transaction.TransactionManager) Class.forName(_defaultClassName).newInstance();
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}
	    }
	}
    }
    
    /**
     * An activity has begun and is active on the current thread.
     *
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.jta.JTAHLS_1 [com.arjuna.mwlabs.wscf.model.as.coordinator.jta.JTAHLS_1] - JTAHLS.begun: 
     */

    public void begun () throws SystemException
    {
	try
	{
	    _theTransactionManagerImple.begin();

	    javax.transaction.Transaction tx = _theTransactionManagerImple.getTransaction();
	    int status = _theTransactionManagerImple.getStatus();
	
	    if (status != javax.transaction.Status.STATUS_ACTIVE)
		throw new BegunFailedException(wscfLogger.log_mesg.getString("com.arjuna.mwlabs.wscf.model.as.coordinator.jta.JTAHLS_1")+status);
	    else
	    {
		_coordinators.put(currentActivity(), tx);
	    }
	}
	catch (SystemException ex)
	{
	    throw ex;
	}
	catch (Exception ex)
	{
	    throw new UnexpectedException(ex.toString());
	}
    }

    /**
     * The current activity is completing with the specified completion status.
     *
     * @param CompletionStatus cs The completion status to use.
     *
     * @return The result of terminating the relationship of this HLS and
     * the current activity.
     */

    public Outcome complete (CompletionStatus cs) throws SystemException
    {
	Transaction current = currentCoordinator();
	XAException xae = null;
	
	if ((cs != null) && (cs instanceof Success))
	{
	    // commit

	    try
	    {
		current.commit();
	    }
	    catch (javax.transaction.RollbackException ex)
	    {
		cs = Failure.instance();
	    }
	    catch (javax.transaction.SystemException ex)
	    {
		cs = Failure.instance();
	    }
	    catch (javax.transaction.HeuristicRollbackException ex)
	    {
		cs = Failure.instance();

		xae = new XAException(XAException.XA_HEURRB);
	    }
	    catch (javax.transaction.HeuristicMixedException ex)
	    {
		cs = Failure.instance();

		xae = new XAException(XAException.XA_HEURMIX);
	    }
	}
	else
	{
	    // abort

	    try
	    {
		current.rollback();
	    }
	    catch (javax.transaction.SystemException ex)
	    {
		cs = Failure.instance();
	    }
	}

	_coordinators.remove(currentActivity());

	try
	{
	    if (_theTransactionManagerImple.getTransaction().equals(current))
	    {
		_theTransactionManagerImple.suspend();
	    }
	}
	catch (javax.transaction.SystemException ex)
	{
	    ex.printStackTrace();
	}

	return new XAOutcome(cs);
    }	

    /**
     * The activity has been suspended.
     */

    public void suspended () throws SystemException
    {
    }	

    /**
     * The activity has been resumed on the current thread.
     */

    public void resumed () throws SystemException
    {
    }	

    /**
     * The activity has completed and is no longer active on the current
     * thread.
     */

    public void completed () throws SystemException
    {
    }

    /**
     * The HLS name.
     */

    public String identity () throws SystemException
    {
	return "JTAHLS";
    }

    /**
     * The activity service maintains a priority ordered list of HLS
     * implementations. If an HLS wishes to be ordered based on priority
     * then it can return a non-negative value: the higher the value,
     * the higher the priority and hence the earlier in the list of HLSes
     * it will appear (and be used in).
     *
     * @return a positive value for the priority for this HLS, or zero/negative
     * if the order is not important.
     */

    public int priority () throws SystemException
    {
	return 0;
    }

    /**
     * Return the context augmentation for this HLS, if any on the current
     * activity.
     *
     * @param ActivityHierarchy current The handle on the current activity
     * hierarchy. The HLS may use this when determining what information to
     * place in its context data.
     *
     * @return a context object or null if no augmentation is necessary.
     */

    public Context context () throws SystemException
    {
	return new JTAContextImple(currentCoordinator());
    }

    /**
     * If the application requires and if the coordination protocol supports
     * it, then this method can be used to execute a coordination protocol on
     * the currently enlisted participants at any time prior to the termination
     * of the coordination scope.
     *
     * This implementation only supports coordination at the end of the
     * activity.
     *
     * @param CompletionStatus cs The completion status to use when determining
     * how to execute the protocol.
     *
     * @exception WrongStateException Thrown if the coordinator is in a state
     * the does not allow coordination to occur.
     * @exception ProtocolViolationException Thrown if the protocol is violated
     * in some manner during execution.
     * @exception SystemException Thrown if any other error occurs.
     *
     * @return The result of executing the protocol, or null.
     */

    public Outcome coordinate (CompletionStatus cs) throws WrongStateException, ProtocolViolationException, NoCoordinatorException, SystemException
    {
	return null;
    }

    /**
     * Enrol the specified participant with the coordinator associated with
     * the current thread. If the coordinator supports a priority ordering
     * of participants, then that ordering can also be specified. Any
     * qualifiers that are to be associated with the participant are also
     * provided.
     *
     * @param Participant act The participant.
     * @param int priority The priority to associate with the participant in
     * the coordinator's list.
     * @param Qualifier[] quals Any qualifiers to be associated with the
     * participant.
     *
     * @exception WrongStateException Thrown if the coordinator is not in a
     * state that allows participants to be enrolled.
     * @exception DuplicateParticipantException Thrown if the participant has
     * already been enrolled and the coordination protocol does not support
     * multiple entries.
     * @exception InvalidParticipantException Thrown if the participant is invalid.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void addParticipant (Participant act, int priority, Qualifier[] quals) throws WrongStateException, DuplicateParticipantException, NoCoordinatorException, InvalidParticipantException, SystemException
    {
	try
	{
	    currentCoordinator().enlistResource(new XAResourceImple(act, quals));
	}
	catch (javax.transaction.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (javax.transaction.RollbackException ex)
	{
	    throw new WrongStateException(ex.toString());
	}
    }

    /**
     * Remove the specified participant from the coordinator's list.
     * This operation may not be supported by all coordination protocols.
     *
     * @exception InvalidParticipantException Thrown if the participant is not known
     * of by the coordinator.
     * @exception WrongStateException Thrown if the state of the coordinator
     * does not allow the participant to be removed (e.g., in a two-phase
     * protocol the coordinator is committing.)
     * @exception SystemException Thrown if any other error occurs.
     */
    
    public void removeParticipant (Participant act) throws InvalidParticipantException, NoCoordinatorException, WrongStateException, SystemException
    {
    }

    /**
     * Some coordination protocol messages may have asynchronous responses
     * or it may be possible for participants to autonomously generate
     * responses to messages that have not yet been producted by the
     * coordinator. As such, this method allows a response from a participant
     * to be passed to the coordinator. In order to ensure that the protocol
     * remains valid, it is necessary for the participant to specify what
     * message produced the response: if the response was autonomously
     * generated by the participant on the assumption it would receive this
     * message from the coordinator and the coordinator subsequently decides
     * not to produce such a message, then the action taken by the participant
     * is invalid and hence so is the response.
     *
     * @param String id the unique participant identification.
     * @param Message notification the message the participant got/assumed
     * when producing the response.
     * @param Outcome response the actual response.
     * @param Qualifier[] quals any qualifiers associated with the response.
     *
     * @exception InvalidParticipantException Thrown if the coordinator has no
     * knowledge of the participant.
     * @exception WrongStateException Thrown if the coordinator is in a state
     * that does not allow it to accept responses at all or this specific
     * type of response.
     * @exception SystemException Thrown if any other error occurs.
     */

    public void setResponse (String id, Message notification, Outcome response, Qualifier[] quals) throws InvalidParticipantException, NoCoordinatorException, WrongStateException, SystemException
    {
	// TODO
    }

    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return a reference to the current coordinators' parent if it is nested,
     * null otherwise.
     */
    
    public Coordinator getParentCoordinator () throws NoCoordinatorException, SystemException
    {
	return null;  // XA does not support nesting!
    }

    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the status of the current coordinator. If there is no
     * activity associated with the thread then NoActivity
     * will be returned.
     *
     * @see com.arjuna.mw.wsas.status.Status
     */

    public com.arjuna.mw.wsas.status.Status status () throws SystemException
    {
	// TODO return separate class for each type

	try
	{
	    return new XAStatus(currentCoordinator().getStatus());
	}
	catch (javax.transaction.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}
    }

    /**
     * Not supported by basic JTA.
     *
     * @exception SystemException Thrown if any error occurs.
     *
     * @return the complete list of qualifiers that have been registered with
     * the current coordinator.
     */
    
    public Qualifier[] qualifiers () throws NoCoordinatorException, SystemException
    {
	return null;
    }

    /**
     * @exception SystemException Thrown if any error occurs.
     *
     * @return The unique identity of the current coordinator.
     */

    public CoordinatorId identifier () throws NoCoordinatorException, SystemException
    {
	return new CoordinatorIdImple(currentCoordinator().toString());
    }

    public static String className ()
    {
	return JTAHLS.class.getName();
    }

    private final ActivityHandleImple currentActivity () throws SystemException
    {
	try
	{
	    ActivityHierarchy hier = UserActivityFactory.userActivity().currentActivity();
	
	    if (hier.size() > 0)
		return (ActivityHandleImple) hier.activity(hier.size() -1);
	    else
		return null;
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	    
	    throw new SystemException(ex.toString());
	}
    }

    private final javax.transaction.Transaction currentCoordinator () throws NoCoordinatorException, SystemException
    {
	javax.transaction.Transaction tx = (javax.transaction.Transaction) _coordinators.get(currentActivity());

	if (tx == null)
	    throw new NoCoordinatorException();
	else
	    return tx;
    }
    
    private static Hashtable _coordinators = new Hashtable();
    private static javax.transaction.TransactionManager _theTransactionManagerImple = null;
    
    private static final String _defaultClassName = com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple.class.getName();

}
