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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ThreadActionData.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.thread;

import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.utils.ThreadUtil;

import java.lang.Thread;
import java.util.Stack;
import java.util.Vector;

import java.util.NoSuchElementException;
import java.util.EmptyStackException;

/**
 * This class maintains a mapping between a thread and its notion of the current
 * transaction. Essentially this is a stack of transactions.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ThreadActionData.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ThreadActionData
{

	public static BasicAction currentAction ()
	{
		ThreadActionData.setup();
		Stack txs = (Stack) _threadList.get();

		if (txs != null)
		{
			try
			{
				return (BasicAction) txs.peek();
			}
			catch (EmptyStackException e)
			{
			}
		}

		return null;
	}

	public static void pushAction (BasicAction a)
	{
		pushAction(a, true);
	}

	/**
	 * By setting the register flag accordingly, information about the thread
	 * may not be propagated to the action, i.e., only the thread's notion of
	 * current changes.
	 */

	public static void pushAction (BasicAction a, boolean register)
	{
		Thread t = Thread.currentThread();
		Stack txs = (Stack) _threadList.get();

		if (txs == null)
		{
			txs = new Stack();
			txs.push(a);

			_threadList.set(txs);
		}
		else
			txs.push(a);

		if (register)
			a.addChildThread(t);
	}

	/**
	 * Put back the entire hierarchy, removing whatever is already there.
	 */

	public static void restoreActions (BasicAction act)
	{
		purgeActions();

		if (act != null)
		{
			/*
			 * First get the hierarchy from the bottom up.
			 */

			java.util.Stack s = new java.util.Stack();
			BasicAction nextLevel = act.parent();

			s.push(act);

			while (nextLevel != null)
			{
				s.push(nextLevel);

				nextLevel = nextLevel.parent();
			}

			/*
			 * Now push the hierarchy onto the thread stack.
			 */

			try
			{
				while (!s.empty())
				{
					pushAction((BasicAction) s.pop());
				}
			}
			catch (Exception ex)
			{
			}
		}
	}

	public static BasicAction popAction () throws EmptyStackException
	{
		return popAction(ThreadUtil.getThreadId(), true);
	}

	public static BasicAction popAction (boolean unregister)
			throws EmptyStackException
	{
		return popAction(ThreadUtil.getThreadId(), unregister);
	}

	public static BasicAction popAction (String threadId)
			throws EmptyStackException
	{
		return popAction(threadId, true);
	}

	/**
	 * By setting the unregister flag accordingly, information about the thread
	 * is not removed from the action.
	 */

	public static BasicAction popAction (String threadId, boolean unregister)
			throws EmptyStackException
	{
		Stack txs = (Stack) _threadList.get();

		if (txs != null)
		{
			BasicAction a = (BasicAction) txs.pop();

			if ((a != null) && (unregister))
			{
				a.removeChildThread(threadId);
			}

			if (txs.size() == 0)
			{
				_threadList.set(null);
			}

			return a;
		}

		return null;
	}

	public static void purgeAction (BasicAction act)
			throws NoSuchElementException
	{
		ThreadActionData.purgeAction(act, Thread.currentThread(), true);
	}

	public static void purgeAction (BasicAction act, Thread t)
			throws NoSuchElementException
	{
		ThreadActionData.purgeAction(act, t, true);
	}

	public static void purgeAction (BasicAction act, Thread t, boolean unregister)
			throws NoSuchElementException
	{
		if ((act != null) && (unregister))
        {
			act.removeChildThread(ThreadUtil.getThreadId(t));
        }

		Stack txs = (Stack) _threadList.get();

		if (txs != null)
		{
			txs.removeElement(act);

			if (txs.size() == 0)
			{
				_threadList.set(null);
			}
		}
	}

	public static void purgeActions ()
	{
		purgeActions(Thread.currentThread(), true);
	}

	public static void purgeActions (Thread t)
	{
		purgeActions(t, true);
	}

	public static void purgeActions (Thread t, boolean unregister)
	{
		Stack txs = (Stack) _threadList.get();

		_threadList.set(null);

		if (txs != null)
		{
			if (unregister)
			{
				while (!txs.empty())
				{
					BasicAction act = (BasicAction) txs.pop();

					if (act != null)
                    {
						act.removeChildThread(ThreadUtil.getThreadId(t));
                    }
				}
			}
		}
	}

	public static void addSetup (ThreadSetup s)
	{
		_threadSetups.addElement(s);
	}

	public static boolean removeSetup (ThreadSetup s)
	{
		return _threadSetups.removeElement(s);
	}

	private static void setup ()
	{
		for (int i = 0; i < _threadSetups.size(); i++)
		{
			ThreadSetup s = (ThreadSetup) _threadSetups.elementAt(i);

			if (s != null)
				s.setup();
		}
	}

	private static ThreadLocal _threadList = new ThreadLocal();

	private static Vector _threadSetups = new Vector();

}
