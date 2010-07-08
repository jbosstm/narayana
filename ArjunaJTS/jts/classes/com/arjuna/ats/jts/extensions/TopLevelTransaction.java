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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: TopLevelTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.extensions;

import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.Unavailable;
import org.omg.CosTransactions.InvalidControl;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.INVALID_TRANSACTION;

/**
 * Creates a nested top-level transaction.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: TopLevelTransaction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class TopLevelTransaction extends AtomicTransaction
{

    public TopLevelTransaction ()
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("TopLevelTransaction::TopLevelTransaction ()");
    }

	_originalTransaction = null;
    }

    public void finalize ()
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("TopLevelTransaction.finalize ()");
    }

	if (_originalTransaction != null)
	{
	    String name = null;
	    Coordinator coord = null;
	
	    try
	    {
		coord = _originalTransaction.get_coordinator();

		if (coord != null)
		{
		    name = coord.get_transaction_name();
		}
	    }
	    catch (Exception e)
	    {
	    }

	    coord = null;

        jtsLogger.i18NLogger.warn_extensions_tltnestedscope(((name != null) ? name : "UNKNOWN"));

	    name = null;
	    _originalTransaction = null;
	}

	super.finalize();
    }

    /**
     * If nested top-level transaction, save current context for resumption
     * later.
     */

    public synchronized void begin () throws SystemException, SubtransactionsUnavailable
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("TopLevelTransaction::begin ()");
    }

	// already begun?

	if (_originalTransaction != null)
	{
	    throw new INVALID_TRANSACTION();
	}

	CurrentImple current = OTSImpleManager.current();

	_originalTransaction = current.suspend();

	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("TopLevelTransaction::begin - suspend transaction " + _originalTransaction);
    }

	super.begin();
    }

    public synchronized void commit (boolean report_heuristics) throws SystemException, NoTransaction, HeuristicMixed, HeuristicHazard, WrongTransaction
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("TopLevelTransaction::commit ( " + report_heuristics + " ) called for " + _originalTransaction);
    }

	if (validTransaction())
	{
	    try
	    {
		super.commit(report_heuristics);		
	    }
	    catch (WrongTransaction e1)
	    {
		resumeTransaction();

		throw e1;
	    }
	    catch (SystemException e2)
	    {
		resumeTransaction();

		throw e2;
	    }

	    resumeTransaction();
	}
	else
	    throw new WrongTransaction();
    }

    public synchronized void rollback () throws SystemException, NoTransaction, WrongTransaction
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("TopLevelTransaction::rollback () called for " + _originalTransaction);
    }

	if (validTransaction())
	{
	    try
	    {
		super.rollback();
	    }
	    catch (WrongTransaction e1)
	    {
		resumeTransaction();

		throw e1;
	    }
	    catch (SystemException e2)
	    {
		resumeTransaction();

		throw e2;
	    }

	    resumeTransaction();
	}
	else
	    throw new WrongTransaction();
    }

    private final void resumeTransaction ()
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("TopLevelTransaction::resumeTransaction for " + _originalTransaction);
    }

	try
	{
	    if (_originalTransaction != null)
	    {
		CurrentImple current = OTSImpleManager.current();

		current.resume(_originalTransaction);

		_originalTransaction = null;
	    }
	}
	catch (Exception e)
	{
	}
    }

    private Control _originalTransaction;
 
}
