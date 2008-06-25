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
 * $Id: TransactionManagerImple.java,v 1.2 2005/05/19 12:13:34 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.jta.arjunajta;

import javax.transaction.*;

import com.arjuna.mw.wsas.UserActivityFactory;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;

import java.util.Hashtable;

import com.arjuna.mw.wsas.exceptions.InvalidActivityException;

public class TransactionManagerImple extends BaseTransactionImple implements javax.transaction.TransactionManager
{

    public TransactionManagerImple ()
    {
	_coordControl = new CoordinatorControl();
	_transactions = new Hashtable();
    }
    
    public Transaction getTransaction () throws javax.transaction.SystemException
    {
	return _coordControl.transactionManager().getTransaction();
    }
 
    /**
     * @return the suspended transaction.
     */

    public Transaction suspend () throws javax.transaction.SystemException
    {
	try
	{
	    ActivityHierarchy hier = UserActivityFactory.userActivity().suspend();
	    Transaction tx = _coordControl.transaction(hier);

	    _transactions.put(tx, hier);

	    return tx;
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new javax.transaction.SystemException(ex.toString());
	}
    }

    public void resume (Transaction which) throws InvalidTransactionException, java.lang.IllegalStateException, javax.transaction.SystemException
    {
	try
	{
	    ActivityHierarchy hier = (ActivityHierarchy) _transactions.get(which);

	    if ((which != null) && (hier == null))
		throw new InvalidTransactionException();
	    
	    UserActivityFactory.userActivity().resume(hier);
	}
	catch (InvalidActivityException ex)
	{
	    throw new InvalidTransactionException();
	}
	catch (com.arjuna.mw.wsas.exceptions.SystemException ex)
	{
	    throw new javax.transaction.SystemException(ex.toString());
	}
    }

    private CoordinatorControl _coordControl;
    private Hashtable          _transactions;
    
}
