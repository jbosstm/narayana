/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: BasicThreadedObject.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.common.*;
import java.lang.Thread;
import java.lang.Math;
import java.util.Random;

import com.hp.mwtests.ts.arjuna.exceptions.TestException;

import java.lang.InterruptedException;

public class BasicThreadedObject extends Thread
{
    
public BasicThreadedObject (boolean start)
    {
	startAction = start;
	uid = new Uid();
    }

public void run ()
    {
	if (startAction)
	{
	    BasicThreadedObject.A = new AtomicAction();

	    System.out.println("BasicThreadedObject "+uid+" created action "+BasicThreadedObject.A.get_uid());

	    BasicThreadedObject.A.begin();

	    Thread.yield();	    
	}
	else
	{
	    System.out.println("BasicThreadedObject "+uid+" adding to action "+BasicThreadedObject.A.get_uid());
	    
	    BasicThreadedObject.A.addThread();

 	    Thread.yield();	    
	}

	BasicAction act = BasicAction.Current();

	if (act != null)
	    System.out.println("BasicThreadedObject "+uid+" current action "+act.get_uid());
	else
	    System.out.println("BasicThreadedObject "+uid+" current action null");

	try
	{
	    BasicThreadedObject.O.incr(4);

	    Thread.yield();
	}
	catch (Exception e)
	{
	}
	
	if (startAction)
	{
	    System.out.println("\nBasicThreadedObject "+uid+" committing action "+act.get_uid());
	    BasicThreadedObject.A.commit();
	    System.out.println("BasicThreadedObject "+uid+" action "+act.get_uid()+" committed\n");
	}
	else
	{
	    System.out.println("\nBasicThreadedObject "+uid+" aborting action "+act.get_uid());
	    BasicThreadedObject.A.abort();
	    System.out.println("BasicThreadedObject "+uid+" action "+act.get_uid()+" aborted\n");
	}
    }

private Uid uid;
private boolean startAction;

public static AtomicAction A = null;
public static SimpleObject O = new SimpleObject();
    
};
