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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CheckedTransactions.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.local.checked;

import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.arjuna.common.Uid;

import com.hp.mwtests.ts.jts.resources.*;
import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CORBA.INVALID_TRANSACTION;

import java.util.*;

class MyCheckedAction extends CheckedAction
{
    
    public synchronized void check (boolean isCommit, Uid actUid, Hashtable list)
    {
	// don't do anything so that no warning message is printed!
    }
    
}

class TXThread extends Thread
{

    public TXThread (Control c)
    {
        cont = c;
    }

    public void run ()
    {
	try
	{
	    System.out.println("Thread "+Thread.currentThread()+" attempting to rollback transaction.");
	    
	    cont.get_terminator().rollback();

	    System.out.println("Transaction rolled back. Checked transactions disabled.");
	}
	catch (Exception e)
	{
	    System.out.println("Caught exception: "+e);
	    System.out.println("Checked transactions enabled!");
	}
    }
    
    private Control cont;
    
};

public class CheckedTransactions
{
    
    public static void main (String[] args)
    {
	ORB myORB = null;
	RootOA myOA = null;

	try
	{
	    myORB = ORB.getInstance("test");
	    myOA = OA.getRootOA(myORB);
	    
	    myORB.initORB(args, null);
	    myOA.initOA();

	    ORBManager.setORB(myORB);
	    ORBManager.setPOA(myOA);

	    for (int i = 0; i < args.length; i++)
	    {
		if (args[i].compareTo("-check") == 0)
		{
		    Properties p = System.getProperties();

		    p.put(com.arjuna.ats.jts.common.Environment.CHECKED_TRANSACTIONS, "YES");
		    
		    System.setProperties(p);
		}
		if (args[i].compareTo("-help") == 0)
		{
		    System.out.println("Usage: CheckedTransactions [-check] [-help]");
		    System.exit(0);
		}
	    }
	    
	    Control tx = null;

	    System.out.println("Thread "+Thread.currentThread()+" starting transaction.");

	    OTSImpleManager.current().setCheckedAction(new MyCheckedAction());
	    
	    OTSImpleManager.current().begin();

	    tx = OTSImpleManager.current().get_control();

	    TXThread txThread = new TXThread(tx);

	    txThread.start();
	    txThread.join();

	    System.out.println("Thread "+Thread.currentThread()+" committing transaction.");

	    OTSImpleManager.current().commit(false);

	    System.out.println("Transaction committed. Checked transactions enabled.");
	}
	catch (Exception e)
	{
	    System.out.println("Caught exception: "+e);
	    System.out.println("Checked transactions disabled!");
	}

	myOA.destroy();
	myORB.shutdown();
    }

}

