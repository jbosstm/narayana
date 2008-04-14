/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * $Id: CompletionCoordinatorImple.java,v 1.6.24.1 2005/11/22 10:36:22 kconner Exp $
 */

package com.arjuna.mwlabs.wst.at.participants;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;
import com.arjuna.webservices.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: CompletionCoordinatorImple.java,v 1.6.24.1 2005/11/22 10:36:22 kconner Exp $
 * @since 1.0.
 */

public class CompletionCoordinatorImple implements com.arjuna.wst.CompletionCoordinatorParticipant
{

    public CompletionCoordinatorImple (CoordinatorManager cm, ActivityHierarchy hier, final boolean deactivate)
    {
	_cm = cm;
	_hier = hier;
    this.deactivate = deactivate ;
    }
    
    public void commit () throws TransactionRolledBackException, UnknownTransactionException, SystemException
    {
	try
	{
	    if (_hier != null)
		_cm.resume(_hier);

	    _cm.confirm();
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
	    //	    throw new HeuristicHazardException();
	    
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.exceptions.NoCoordinatorException ex)
	{
	    throw new UnknownTransactionException();
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.CoordinatorCancelledException ex)
	{
	    throw new TransactionRolledBackException();
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicMixedException ex)
	{
	    //	    throw new HeuristicMixedException();

	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicHazardException ex)
	{
	    //	    throw new HeuristicHazardException();

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
	finally
	{
	    if (deactivate)
	        CompletionCoordinatorProcessor.getProcessor().deactivateParticipant(this);
	}
    }
    
    public void rollback () throws UnknownTransactionException, SystemException
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
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.CoordinatorConfirmedException ex)
	{
	    throw new SystemException();
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicMixedException ex)
	{
	    throw new SystemException(ex.toString());
	}
	catch (com.arjuna.mw.wscf.model.twophase.exceptions.HeuristicHazardException ex)
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
	finally
	{
        if (deactivate)
            CompletionCoordinatorProcessor.getProcessor().deactivateParticipant(this);
	}
    }

    private CoordinatorManager   _cm;
    private ActivityHierarchy    _hier;
    private final boolean deactivate ;
}
