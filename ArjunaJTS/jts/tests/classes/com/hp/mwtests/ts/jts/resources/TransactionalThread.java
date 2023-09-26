/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.resources;

import org.omg.CosTransactions.Control;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

public class TransactionalThread extends Thread
{

    public TransactionalThread ()
    {
	done = false;
	control = null;
    }
    
    public TransactionalThread (Control currentTran)
    {
	done = false;
	control = currentTran;
    }

    public void run ()
    {
	boolean shouldWork = false;
	
	CurrentImple current = OTSImpleManager.current();

	if (control != null)
	{
	    System.out.println("New thread resuming transaction.");

	    try
	    {
		current.resume(control);
	    }
	    catch (Exception e)
	    {
		System.err.println("Caught unexpected exception: "+e);
		System.exit(1);
	    }
	}
	
	try
	{
	    System.out.print("Non-creating thread trying to commit transaction. ");
	    
	    if (control == null)
		System.out.println("Should fail - no transaction associated with thread!");
	    else
		System.out.println("Should succeed.");
		
	    current.commit(true);

	    System.out.print("Non-creating thread committed transaction. ");

	    if (control == null)
	    {
		System.out.println("Error.");
		System.exit(1);
	    }
	    else
		System.out.println();
	}
	catch (Exception e)
	{
	    System.err.println("Caught unexpected exception: "+e);
	}

	done = true;
    }

    public synchronized boolean finished ()
    {
	return done;
    }

    private boolean done;
    private Control control;
    
}