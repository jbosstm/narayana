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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionalThread.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.resources;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import org.omg.CosTransactions.*;

import java.util.Random;
import java.lang.InterruptedException;

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
