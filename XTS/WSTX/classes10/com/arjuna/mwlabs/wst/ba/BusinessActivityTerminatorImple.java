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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BusinessActivityTerminatorImple.java,v 1.3.18.1 2005/11/22 10:36:14 kconner Exp $
 */

package com.arjuna.mwlabs.wst.ba;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf.model.sagas.CoordinatorManagerFactory;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.webservices.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.wst.BusinessActivityTerminator;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

public class BusinessActivityTerminatorImple implements BusinessActivityTerminator
{
    public BusinessActivityTerminatorImple ()
        throws SystemException
    {
        try
        {
            _coordManager = CoordinatorManagerFactory.coordinatorManager();
            _hier = _coordManager.currentActivity();
        }
        catch (ProtocolNotRegisteredException pnre)
        {
            throw new SystemException(pnre.toString());
        }
        catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
        {
            throw new SystemException(ex.toString());
        }
    }
    
    public void close () throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
        try
        {
            if (_hier == null)
            throw new UnknownTransactionException();

            _coordManager.resume(_hier);
        
            _coordManager.close();
        }
        catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
        {
            throw new UnknownTransactionException();
        }
        catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
        {
            throw new SystemException(ex.toString());
        }
        catch (com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorCancelledException ex)
        {
            throw new TransactionRolledBackException();
        }
        catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
        {
            throw new UnknownTransactionException();
        }
        catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
        {
            throw new SystemException(ex.toString());
        }
        catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
        {
            throw new SystemException(ex.toString());
        }
        catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
        {
            throw new SystemException(ex.toString());
        }
        catch (UnknownTransactionException ex)
        {
            throw ex;
        }
        finally
        {
            TerminationCoordinatorProcessor.getProcessor().deactivateParticipant(this) ;
        }
    }

    public void cancel () throws UnknownTransactionException, SystemException
    {
	try
	{
	    if (_hier == null)
		throw new UnknownTransactionException();

	    _coordManager.resume(_hier);
	
	    _coordManager.cancel();
	}
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorConfirmedException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException();
	}
	catch (UnknownTransactionException ex)
	{
	    throw ex;
	}
	finally
	{
        TerminationCoordinatorProcessor.getProcessor().deactivateParticipant(this) ;
	}
    }

    /**
     * Complete doesn't mean go away, it just means that all work you need to
     * accomplish the commit/rollback has been received.
     */

    public void complete () throws UnknownTransactionException, SystemException
    {
	try
	{
	    if (_hier == null)
		throw new UnknownTransactionException();

	    _coordManager.resume(_hier);
	
	    _coordManager.complete();
	}
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException();
	}
	catch (UnknownTransactionException ex)
	{
	    throw ex;
	}
    }

    private CoordinatorManager                   _coordManager = null;
    private ActivityHierarchy                    _hier = null;
    
}
