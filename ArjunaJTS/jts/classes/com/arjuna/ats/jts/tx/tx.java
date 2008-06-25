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
 * $Id: tx.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.tx;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import com.arjuna.ats.internal.arjuna.template.*;

import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.jts.*;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import java.util.Hashtable;

import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

/*
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: tx.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class tx
{
    /**
     * These values are pretty arbitrary since we have
     * no way of knowing what they should be. As long as
     * the CPP names are used we should be ok though.
     */

    public static final int TX_OK = 0;
    public static final int TX_COMMIT_COMPLETED = 1;
    public static final int TX_COMMIT_DESICION_LOGGED = 2;
    public static final int TX_CHAINED = 3;
    public static final int TX_UNCHAINED = 4;
    public static final int TX_ERROR = -1;
    public static final int TX_FAIL = -2;
    public static final int TX_PROTOCOL_ERROR = -3;
    public static final int TX_OUTSIDE = -4;
    public static final int TX_NO_BEGIN = -5;
    public static final int TX_HAZARD = -6;
    public static final int TX_HAZARD_NO_BEGIN = -7;
    public static final int TX_NOT_SUPPORTED = -8;
    public static final int TX_EINVAL = -9;
    public static final int TX_ROLLBACK = -10;
    public static final int TX_ROLLBACK_NO_BEGIN = -11;
    
    public static final synchronized int tx_open ()
    {
	int toReturn = tx.TX_ERROR;  // what to return?
    
	if (!__tx_open)
	{
	    __tx_open = true;
	    toReturn = tx.TX_OK;
	}

	return toReturn;
    }

    /**
     * The X/Open spec. says to raise TX_PROTOCOL_ERROR if called
     * from within a transaction. However, the OTS spec. implies there
     * is no mapping for tx_close. So, do nothing.
     */

    public static final synchronized int tx_close ()
    {
	int toReturn = tx.TX_ERROR;
    
	if (__tx_open)
	{
	    __tx_open = false;
	    toReturn = tx.TX_OK;
	}

	return toReturn;
    }

    public static final synchronized int tx_allow_nesting ()
    {
	int toReturn = tx.TX_PROTOCOL_ERROR;

	if (!__tx_open)
	{
	    __tx_allow_nesting = true;
	    toReturn = tx.TX_OK;
	}

	return toReturn;
    }

    public static final synchronized int tx_begin ()
    {
	int toReturn = tx.TX_OK;
	CurrentImple current = OTSImpleManager.current();

	if (!__tx_allow_nesting)
	{
	    /*
	     * Already have a transaction?
	     */

	    try
	    {
		Control control = current.get_control();

		if (control != null)
		{
		    /*
		     * Have a transaction already, and not allowed to
		     * create nested transactions!
		     */

		    toReturn = tx.TX_PROTOCOL_ERROR;
		    control = null;
		}
	    }
	    catch (Exception e)
	    {
		// something went wrong!
		
		toReturn = tx.TX_FAIL;
	    }
	}

	if (toReturn == tx.TX_OK)
	{
	    try
	    {
		current.begin();
	    }
	    catch (Exception e)
	    {
		toReturn = tx.TX_FAIL;
	    }
	}
    
	return toReturn;
    }

    /**
     * This needs to implement checked transactions such that only the
     * transaction initiator (thread) can terminate it.
     */

    public static final synchronized int tx_rollback ()
    {
	int toReturn = tx.TX_OK;
	CurrentImple current = OTSImpleManager.current();

	try
	{
	    current.rollback();
	}
	catch (NoTransaction e1)
	{
	    toReturn = tx.TX_NO_BEGIN;
	}
	catch (Exception e2)
	{
	    toReturn = tx.TX_FAIL;
	}

	return toReturn;
    }

    public static final synchronized int tx_set_commit_return (int when_return)
    {
	int toReturn = tx.TX_OK;
	boolean b = ((when_return == 0) ? false : true);
	
	if ((when_return == tx.TX_COMMIT_COMPLETED) ||
	    (when_return == tx.TX_COMMIT_DESICION_LOGGED))
	{
	    __tx_report_heuristics.put(Thread.currentThread(), new Boolean(b));
	}
	else
	    toReturn = tx.TX_PROTOCOL_ERROR;

	return toReturn;
    }
    
    public static final synchronized int tx_commit ()
    {
	int toReturn = tx.TX_OK;
	CurrentImple current = OTSImpleManager.current();
	Boolean report_heuristics = (Boolean) __tx_report_heuristics.get(Thread.currentThread());

	if (report_heuristics == null)
	    report_heuristics = new Boolean(true);  // default TRUE
	    
	try
	{
	    boolean when_return = report_heuristics.booleanValue();
		
	    current.commit(when_return);
	}
	catch (NoTransaction e1)
	{
	    toReturn = tx.TX_NO_BEGIN;
	}
	catch (HeuristicMixed e2)
	{
	    toReturn = tx.TX_HAZARD;
	}
	catch (HeuristicHazard e3)
	{
	    toReturn = tx.TX_HAZARD;
	}
	catch (TRANSACTION_ROLLEDBACK e4)
	{
	    toReturn = tx.TX_ROLLBACK;
	}
	catch (Exception e5)
	{
	    toReturn = tx.TX_FAIL;
	}

	return toReturn;
    }

    public static final synchronized int tx_set_transaction_control (int control)
    {
	return tx.TX_FAIL;
    }

    public static final synchronized int tx_set_transaction_timeout (int timeout)
    {
	int toReturn = tx.TX_OK;
	CurrentImple current = OTSImpleManager.current();

	try
	{
	    current.set_timeout(timeout);
	}
	catch (Exception e)
	{
	    toReturn = tx.TX_FAIL;
	}

	return toReturn;
    }
    
    private static boolean   __tx_open = false;
    private static boolean   __tx_allow_nesting = false;
    private static Hashtable __tx_report_heuristics = new Hashtable();
 
}
