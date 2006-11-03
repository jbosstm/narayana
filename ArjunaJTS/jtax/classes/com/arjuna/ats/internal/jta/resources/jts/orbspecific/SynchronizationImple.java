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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SynchronizationImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.resources.jts.orbspecific;

import com.arjuna.ats.jta.common.*;
import com.arjuna.ats.jta.xa.*;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.logging.*;

import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.utils.jts.StatusConverter;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;

import javax.transaction.*;
import javax.transaction.xa.*;
import java.io.*;

import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicRollback;
import java.io.IOException;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosTransactions.WrongTransaction;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

/**
 * Whenever a synchronization is registered, an instance of this class
 * is used to wrap it.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: SynchronizationImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class SynchronizationImple extends org.omg.CosTransactions.SynchronizationPOA 
{
    
    public SynchronizationImple (javax.transaction.Synchronization ptr)
    {
	_theSynch = ptr;
	_theReference = null;
    }

    public void finalize ()
    {
	_theSynch = null;

	try
	{
	    super.finalize();
	}
	catch (Throwable e)
	{
	}
    }

    public final org.omg.CosTransactions.Synchronization getSynchronization ()
    {
	if (_theReference == null)
	{
	    ORBManager.getPOA().objectIsReady(this);
	    
	    _theReference = org.omg.CosTransactions.SynchronizationHelper.narrow(ORBManager.getPOA().corbaReference(this));
	}
	
	return _theReference;
    }
 
    public void before_completion () throws org.omg.CORBA.SystemException
    {
	if (jtaLogger.logger.isDebugEnabled())
	{
	    jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				   com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "SynchronizationImple.before_completion");
	}

	if (_theSynch != null)
	{
	    try
	    {
		_theSynch.beforeCompletion();
	    }
	    catch (Exception e)
	    {
		throw new UNKNOWN();
	    }
	}
	else
	    throw new UNKNOWN();
    }

    public void after_completion (org.omg.CosTransactions.Status status) throws org.omg.CORBA.SystemException
    {
	if (jtaLogger.logger.isDebugEnabled())
	{
	    jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				   com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "SynchronizationImple.after_completion");
	}

	if (_theSynch != null)
	{
	    int s = StatusConverter.convert(status);

	    try
	    {
		_theSynch.afterCompletion(s);

		if (_theReference != null)
		    ORBManager.getPOA().shutdownObject(this);
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
		
		if (_theReference != null)
		    ORBManager.getPOA().shutdownObject(this);

		throw new UNKNOWN(); // should not cause any affect!
	    }
	}
	else
	    throw new UNKNOWN(); // should not cause any affect!
    }

    private javax.transaction.Synchronization       _theSynch;
    private org.omg.CosTransactions.Synchronization _theReference;

}
