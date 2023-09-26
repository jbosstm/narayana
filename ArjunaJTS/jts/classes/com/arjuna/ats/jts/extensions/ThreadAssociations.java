/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.extensions;

import java.util.Hashtable;
import java.util.Vector;

import org.omg.CORBA.SystemException;

import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * Instances of TxAssociation can be added on
 * a per thread basis, or for all threads.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: ThreadAssociations.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 *
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

		if (v.isEmpty())
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
                    jtsLogger.i18NLogger.warn_extensions_threadasserror("ThreadAssociations.updateAssociations", e);
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
                jtsLogger.i18NLogger.warn_extensions_threadasserror("ThreadAssociations.updateAssociations", e);
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