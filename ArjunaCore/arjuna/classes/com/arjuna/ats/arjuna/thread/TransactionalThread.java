/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: TransactionalThread.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.thread;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.utils.ThreadUtil;

import java.util.Hashtable;
import java.lang.Thread;

import java.lang.IllegalArgumentException;

/**
 * A transactional thread is automatically registered with the transaction
 * that is in scope when it is created (i.e., the transaction that is
 * associated with the creating thread). The transactional thread is also
 * automatically unregistered from the transaction when the thread
 * terminates.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TransactionalThread.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class TransactionalThread extends Thread
{

public void finalize ()
    {
	if (action != null)
	{
	    action.removeChildThread(ThreadUtil.getThreadId(this));
	    action = null;
	}
    }

public static void create (Thread thread) throws IllegalArgumentException
    {
	/*
	 * New thread should not be running yet, so this
	 * should work!
	 */

    final String threadId = ThreadUtil.getThreadId(thread) ;
	if (actions.get(threadId) == null)
	{
	    BasicAction currentAction = BasicAction.Current();

	    if (currentAction != null)
	    {
		currentAction.addChildThread(thread);
		actions.put(threadId, currentAction);
		
		currentAction = null;
	    }
	}
	else
	    throw new IllegalArgumentException();
    }

public static void destroy (Thread thread) throws IllegalArgumentException
    {
    final String threadId = ThreadUtil.getThreadId(thread) ;
	BasicAction currentAction = (BasicAction) actions.remove(threadId);

	if (currentAction != null)
	{
	    if (currentAction != null)
	    {
		currentAction.removeChildThread(threadId);
		currentAction = null;
	    }
	}
	else
	    throw new IllegalArgumentException();
    }

protected TransactionalThread ()
    {
	/*
	 * New thread should not be running yet, so this
	 * should work!
	 */
	
	action = BasicAction.Current();

	if (action != null)
	{
	    action.addChildThread(this);
	}
    }

private BasicAction action = null;

private static Hashtable actions = new Hashtable();
    
}
