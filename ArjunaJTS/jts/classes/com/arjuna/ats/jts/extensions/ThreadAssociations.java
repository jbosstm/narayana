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
 * Copyright (C) 2000, 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ThreadAssociations.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.extensions;

import com.arjuna.ats.jts.logging.*;

import org.omg.CosTransactions.*;

import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import com.arjuna.ats.internal.jts.ControlWrapper;

import com.arjuna.common.util.logging.*;

import java.util.*;

import org.omg.CORBA.SystemException;

/**
 * Instances of TxAssociation can be added on
 * a per thread basis, or for all threads.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: ThreadAssociations.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 *
 * @message com.arjuna.ats.jts.extensions.threadasserror {0} caught exception {1}
 */

public class ThreadAssociations
{

    public final static boolean add (TxAssociation tx)
    {
	if (tx == null)
	    return false;
	
	Vector v;
	Thread ct = Thread.currentThread();
	    
	synchronized (txAssociations)
	    {
		v = (Vector) txAssociations.get(tx);

		if (v == null)
		{
		    v = new Vector();
		    txAssociations.put(ct, v);
		}
	    }

	v.addElement(tx);

	return true;
    }

    public final static boolean addGlobal (TxAssociation tx)
    {
	if (tx == null)
	    return false;

	synchronized (globalTxAssociations)
	    {
		globalTxAssociations.addElement(tx);
	    }

	return true;
    }

    public final static boolean remove (TxAssociation tx)
    {
	if (tx == null)
	    return false;

	synchronized (txAssociations)
	    {
		Thread ct = Thread.currentThread();
		Vector v = (Vector) txAssociations.get(ct);
		
		v.removeElement(tx);

		if (v.size() == 0)
		    txAssociations.remove(ct);
	    }

	return true;
    }

    public final static boolean removeGlobal (TxAssociation tx)
    {
	if (tx == null)
	    return false;

	synchronized (globalTxAssociations)
	    {
		globalTxAssociations.removeElement(tx);
	    }

	return true;
    }

    public final static boolean removeAll (boolean global)
    {
	if (global)
	{
	    synchronized (globalTxAssociations)
		{
		    globalTxAssociations.removeAllElements();
		    globalTxAssociations = null;
		}
	}
	else
	{
	    synchronized (txAssociations)
		{
		    txAssociations.clear();
		    txAssociations = null;
		}
	}

	return true;
    }
	    
    final static void updateAssociation (ControlWrapper tx, int reason)
    {
	
	/*
	 * Do thread specific first.
	 */
	
	try
	{
	    synchronized (txAssociations)
		{
		    Vector v = (Vector) txAssociations.get(Thread.currentThread());

		    if (v != null)
		    {
			for (int i = 0; i < v.size(); i++)
			{
			    TxAssociation ta = (TxAssociation) v.elementAt(i);

			    try
			    {
				if (ta != null)
				    update(ta, tx, reason);
			    }
			    catch (SystemException e)
			    {
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
				    jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.threadasserror",
							      new Object[] {"ThreadAssociations.updateAssociations", e} );
				}
			    }
			}
		    }
		}
	    
	    /*
	     * Now do globals.
	     */

	    synchronized (globalTxAssociations)
		{
		    for (int i = 0; i < globalTxAssociations.size(); i++)
		    {
			TxAssociation ta = (TxAssociation) globalTxAssociations.elementAt(i);

			try
			{
			    if (ta != null)
				update(ta, tx, reason);
			}
			catch (SystemException e)
			{
			    if (jtsLogger.loggerI18N.isWarnEnabled())
			    {
				jtsLogger.loggerI18N.warn("com.arjuna.ats.jts.extensions.threadasserror",
							  new Object[] {"ThreadAssociations.updateAssociations", e} );
			    }
			}
		    }
		}
	}
	catch (Throwable t)
	{
	    // ignore any exceptions or errors!
	}
    }
 
    private static void update (TxAssociation ta, ControlWrapper tx,
				int reason) throws SystemException
    {
	switch (reason)
	{
	case CurrentImple.TX_BEGUN:
	    {
		ta.begin(tx);
	    }
	    break;
	case CurrentImple.TX_COMMITTED:
	    {
		ta.commit(tx);
	    }
	    break;
	case CurrentImple.TX_ABORTED:
	    {
		ta.rollback(tx);
	    }
	    break;
	case CurrentImple.TX_SUSPENDED:
	    {
		ta.suspend(tx);
	    }
	    break;
	case CurrentImple.TX_RESUMED:
	    {
		ta.resume(tx);
	    }
	    break;
	default:
	    break;
	}
    }

    private static Hashtable txAssociations = new Hashtable();
    private static Vector    globalTxAssociations = new Vector();

}
