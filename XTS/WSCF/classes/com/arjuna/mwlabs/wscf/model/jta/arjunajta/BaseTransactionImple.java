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
 * $Id: BaseTransactionImple.java,v 1.5 2005/05/19 12:13:34 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.jta.arjunajta;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.mw.wsas.*;
import com.arjuna.mw.wsas.activity.*;
import com.arjuna.mw.wsas.completionstatus.*;
import com.arjuna.mw.wsas.exceptions.*;

import com.arjuna.mw.wscf.model.xa.outcomes.XAOutcome;
import com.arjuna.mw.wscf.model.xa.status.XAStatus;

import javax.transaction.xa.*;

import javax.transaction.SystemException;
import java.lang.IllegalStateException;

/**
 * @message com.arjuna.mwlabs.wscf.model.jta.arjunajta.BaseTransactionImple_1 [com.arjuna.mwlabs.wscf.model.jta.arjunajts.BaseTransactionImple_1] - ArjunaCoreHLS.begun: 
 */

public class BaseTransactionImple
{
    
    public BaseTransactionImple ()
    {
    }

    public void begin () throws javax.transaction.NotSupportedException, javax.transaction.SystemException
    {
	try
	{
	    ActivityHierarchy hier = UserActivityFactory.userActivity().currentActivity();

	    /*
	     * Nested transactions are not supported by JTA/XA.
	     */
	    
	    if (hier != null)
		throw new javax.transaction.NotSupportedException();
	    
	    UserActivityFactory.userActivity().start();
	}
	catch (Exception ex)
	{
	    throw new javax.transaction.SystemException(ex.toString());
	}
    }

    public void commit () throws javax.transaction.RollbackException, javax.transaction.HeuristicMixedException, javax.transaction.HeuristicRollbackException, java.lang.SecurityException, java.lang.IllegalStateException, javax.transaction.SystemException
    {	
	try
	{
	    Outcome res = UserActivityFactory.userActivity().end(Success.instance());
	    
	    if (res != null)
	    {
		if (res instanceof XAOutcome)
		{
		    XAOutcome co = (XAOutcome) res;
		    XAException exp = (XAException) co.data();
		    
		    if (exp == null)
		    {
			if (co.completedStatus().equals(Failure.instance()))
			    throw new javax.transaction.RollbackException();
		    }
		    else
		    {
			switch (exp.errorCode)
			{
			case XAException.XA_HEURRB:
			    throw new javax.transaction.HeuristicRollbackException();
			case XAException.XA_HEURMIX:
			default:
			    throw new javax.transaction.HeuristicMixedException();
			}
		    }
		}
		else
		    throw new SystemException(wscfLogger.log_mesg.getString("com.arjuna.mwlabs.wscf.model.jta.arjunajta.BaseTransactionImple_1"));
	    }
	}
	catch (NoPermissionException ex)
	{
	    throw new java.lang.SecurityException();
	}
	catch (ProtocolViolationException ex)
	{
	    throw new javax.transaction.HeuristicMixedException();
	}
	catch (WrongStateException ex)
	{
	    throw new IllegalStateException();
	}
	catch (InvalidActivityException ex)
	{
	    throw new IllegalStateException();
	}
	catch (NoActivityException ex)
	{
	    throw new java.lang.IllegalStateException();
	}
	catch (ActiveChildException ex)
	{
	    // ?? assume the coordination protocol will cancel children anyway.
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new javax.transaction.SystemException(ex.toString());
	}
    }

    public void rollback () throws java.lang.IllegalStateException, java.lang.SecurityException, javax.transaction.SystemException
    {
	try
	{
	    Outcome res = UserActivityFactory.userActivity().end(Failure.instance());
	    
	    if (res != null)
	    {
		if (res instanceof XAOutcome)
		{
		    XAOutcome co = (XAOutcome) res;
		    XAException exp = (XAException) co.data();
		    
		    if (exp != null)
			throw new SystemException(exp.toString());
		}
		else
		    throw new SystemException(wscfLogger.log_mesg.getString("com.arjuna.mwlabs.wscf.model.jta.arjunajta.BaseTransactionImple_1"));
	    }
	}
	catch (NoPermissionException ex)
	{
	    throw new java.lang.SecurityException();
	}
	catch (ProtocolViolationException ex)
	{
	    throw new javax.transaction.SystemException(ex.toString());
	}
	catch (WrongStateException ex)
	{
	    throw new IllegalStateException();
	}
	catch (InvalidActivityException ex)
	{
	    throw new IllegalStateException();
	}
	catch (NoActivityException ex)
	{
	    throw new java.lang.IllegalStateException();
	}
	catch (ActiveChildException ex)
	{
	    // ?? assume the coordination protocol will cancel children anyway.
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new javax.transaction.SystemException(ex.toString());
	}
    }

    public void setRollbackOnly () throws java.lang.IllegalStateException, javax.transaction.SystemException
    {
	try
	{
	    UserActivityFactory.userActivity().setCompletionStatus(FailureOnly.instance());
	}
	catch (NoActivityException ex)
	{
	    throw new IllegalStateException();
	}
	catch (WrongStateException ex)
	{
	    throw new IllegalStateException();
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new javax.transaction.SystemException(ex.toString());
	}
    }

    public int getStatus () throws javax.transaction.SystemException
    {
	try
	{
	    com.arjuna.mw.wsas.status.Status s = UserActivityFactory.userActivity().status();
	    
	    if (s instanceof XAStatus)
	    {
		XAStatus xas = (XAStatus) s;
		
		return xas.getStatus();
	    }
	    else
		return javax.transaction.Status.STATUS_UNKNOWN;
	}
	catch (Exception ex)
	{
	    throw new SystemException(ex.toString());
	}
    }

    public void setTransactionTimeout (int seconds) throws javax.transaction.SystemException
    {
	try
	{
	    UserActivityFactory.userActivity().setTimeout(seconds);
	}
	catch (Exception ex)
	{
	    throw new javax.transaction.SystemException(ex.toString());
	}
    }

    public int getTimeout () throws javax.transaction.SystemException
    {
	try
	{
	    return UserActivityFactory.userActivity().getTimeout();
	}
	catch (Exception ex)
	{
	    throw new javax.transaction.SystemException(ex.toString());
	}
    }
    
}
