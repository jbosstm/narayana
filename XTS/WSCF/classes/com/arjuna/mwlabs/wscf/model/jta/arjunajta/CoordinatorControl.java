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
 * $Id: CoordinatorControl.java,v 1.5 2005/05/19 12:13:34 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.jta.arjunajta;

import com.arjuna.mw.wscf.model.xa.outcomes.*;
import com.arjuna.mw.wscf.model.xa.status.*;

import com.arjuna.mw.wscf.common.Qualifier;
import com.arjuna.mw.wscf.common.CoordinatorId;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;
import com.arjuna.mw.wsas.completionstatus.Success;
import com.arjuna.mw.wsas.completionstatus.Failure;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.WrongStateException;
import com.arjuna.mw.wsas.exceptions.ProtocolViolationException;

import com.arjuna.mw.wscf.exceptions.*;

import javax.transaction.*;
import javax.transaction.xa.*;

import java.util.Hashtable;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CoordinatorControl.java,v 1.5 2005/05/19 12:13:34 nmcl Exp $
 * @since 1.0.
 */

public class CoordinatorControl
{

    public CoordinatorControl ()
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
     */

    public void begin () throws SystemException
    {
	try
	{
	    _theTransactionManagerImple.begin();

	    javax.transaction.Transaction tx = _theTransactionManagerImple.getTransaction();
	    int status = _theTransactionManagerImple.getStatus();
	
	    if (status != javax.transaction.Status.STATUS_ACTIVE)
		throw new BegunFailedException("CoordinatorControl.begun: "+status);
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

    public void suspend () throws SystemException
    {
	try
	{
	    javax.transaction.Transaction tx = _theTransactionManagerImple.suspend();
	
	    _coordinators.put(currentActivity(), tx);
	}
	catch (Exception ex)
	{
	    throw new SystemException(ex.toString());
	}
    }	

    /**
     * The activity has been resumed on the current thread.
     */

    public void resume () throws SystemException
    {
	try
	{
	    javax.transaction.Transaction tx = (javax.transaction.Transaction) _coordinators.get(currentActivity());
	
	    _theTransactionManagerImple.resume(tx);
	}
	catch (Exception ex)
	{
	    throw new SystemException(ex.toString());
	}
    }	

    /**
     * The activity has completed and is no longer active on the current
     * thread.
     */

    public void completed () throws SystemException
    {
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

    public javax.transaction.TransactionManager transactionManager ()
    {
	return _theTransactionManagerImple;
    }

    public final javax.transaction.Transaction currentCoordinator () throws NoCoordinatorException, SystemException
    {
	javax.transaction.Transaction tx = (javax.transaction.Transaction) _coordinators.get(currentActivity());

	if (tx == null)
	    throw new NoCoordinatorException();
	else
	    return tx;
    }
    
    final javax.transaction.Transaction transaction (ActivityHierarchy hier) throws SystemException
    {
	return (javax.transaction.Transaction) _coordinators.get(hier);
    }

    private final ActivityHierarchy currentActivity () throws SystemException
    {
	try
	{
	    return UserActivityFactory.userActivity().currentActivity();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	    
	    throw new SystemException(ex.toString());
	}
    }

    private static Hashtable _coordinators = new Hashtable();
    private static javax.transaction.TransactionManager _theTransactionManagerImple = null;
    
    private static final String _defaultClassName = com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple.class.getName();

}
