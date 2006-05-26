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
 * $Id: TerminationCoordinatorImple.java,v 1.2.20.1 2005/11/22 10:36:19 kconner Exp $
 */

package com.arjuna.mwlabs.wst.ba.participants;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TerminationCoordinatorImple.java,v 1.2.20.1 2005/11/22 10:36:19 kconner Exp $
 * @since 1.0.
 */

// Why is this here in participants package?

public class TerminationCoordinatorImple// implements com.arjuna.wst.TerminationCoordinator
{

    public TerminationCoordinatorImple (CoordinatorManager cm, ActivityHierarchy hier)
    {
	_cm = cm;
	_hier = hier;
    }
    
    public void close () throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
	try
	{
	    if (_hier != null)
		_cm.resume(_hier);

	    _cm.close();
	}
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
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
	catch (com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorCancelledException ex)
	{
	    throw new TransactionRolledBackException();
	}
	catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}
	finally
	{
	    //	    if (_ccd != null)
	    // _ccd.deactivateObject(this);
	}
    }
    
    public void cancel () throws UnknownTransactionException, SystemException
    {
	try
	{
	    if (_hier != null)
		_cm.resume(_hier);
	    
	    _cm.cancel();
	}
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
	{
	    throw new SystemException();
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorConfirmedException ex)
	{
	    throw new SystemException();
	}
	catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}
	finally
	{
	    //	    if (_ccd != null)
	    //		_ccd.deactivateObject(this);
	}
    }

    public void complete () throws UnknownTransactionException, SystemException
    {
	try
	{
	    if (_hier != null)
		_cm.resume(_hier);
	    
	    _cm.complete();
	}
	catch (com.arjuna.mw.wsas.exceptions.InvalidActivityException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.WrongStateException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.ProtocolViolationException ex)
	{
	    throw new SystemException();
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorConfirmedException ex)
	{
	    throw new SystemException();
	}
	catch (com.arjuna.mw.wsas.exceptions.NoPermissionException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new SystemException(ex.toString());
	}
	finally
	{
	    //	    if (_ccd != null)
	    //		_ccd.deactivateObject(this);
	}
    }
    
    private CoordinatorManager   _cm;
    private ActivityHierarchy    _hier;
    
}
