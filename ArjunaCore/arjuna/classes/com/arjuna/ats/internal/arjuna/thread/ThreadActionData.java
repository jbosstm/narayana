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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.NoSuchElementException;

import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.utils.ThreadUtil;

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
		Deque<BasicAction> txs = _threadList.get();

		if (txs != null && !txs.isEmpty())
		{
			return txs.peekFirst();
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
		Deque<BasicAction> txs = _threadList.get();

		if (txs == null)
		{
			txs = new ArrayDeque<BasicAction>();
			txs.push(a);

			_threadList.set(txs);
		}
		else
			txs.push(a);

		if (register)
                    a.addChildThread(Thread.currentThread());
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

			Deque<BasicAction> s = new ArrayDeque<BasicAction>();
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
				while (!s.isEmpty())
				{
					pushAction(s.pop());
				}
			}
			catch (Exception ex)
			{
			}
		}
	}

	public static BasicAction popAction () throws NoSuchElementException
	{
		return popAction(ThreadUtil.getThreadId(), true);
	}

	public static BasicAction popAction (boolean unregister)
			throws NoSuchElementException
	{
		return popAction(ThreadUtil.getThreadId(), unregister);
	}

	public static BasicAction popAction (String threadId)
			throws NoSuchElementException
	{
		return popAction(threadId, true);
	}

	/**
	 * By setting the unregister flag accordingly, information about the thread
	 * is not removed from the action.
	 */

	public static BasicAction popAction (String threadId, boolean unregister)
			throws NoSuchElementException
	{
		Deque<BasicAction> txs = _threadList.get();

		if (txs != null)
		{
			BasicAction a = txs.pop();

			if (a != null && unregister)
			{
				a.removeChildThread(threadId);
			}

			if (txs.isEmpty())
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
		if (act != null && unregister)
        {
			act.removeChildThread(ThreadUtil.getThreadId(t));
        }

		Deque<BasicAction> txs = _threadList.get();

		if (txs != null)
		{
			txs.remove(act);

			if (txs.isEmpty())
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
		Deque<BasicAction> txs = _threadList.get();

		_threadList.set(null);

		if (txs != null)
		{
			if (unregister)
			{
				while (!txs.isEmpty())
				{
					BasicAction act = txs.pop();

					if (act != null)
                    {
						act.removeChildThread(ThreadUtil.getThreadId(t));
                    }
				}
			}
		}
	}

	/**
	 * Add a per thread setup object to the global list. This should only
	 * happen before the transaction service really begins, or you risk having
	 * some threads see one view of the list that is different to other threads.
	 * 
	 * @param s the setup to add.
	 */
	
	public static void addSetup (ThreadSetup s)
	{
		synchronized (_threadSetups)
		{
			_threadSetups.add(s);
		}
	}

	/**
	 * Remove a per thread setup object to the global list. This should only
	 * happen after the transaction service really ends, or you risk having
	 * some threads see one view of the list that is different to other threads.
	 * 
	 * @param s the setup to add.
	 */
	
	public static boolean removeSetup (ThreadSetup s)
	{
		synchronized (_threadSetups)
		{
			return _threadSetups.remove(s);
		}
	}

	private static void setup ()
	{
		for (int i = 0; i < _threadSetups.size(); i++)
		{
			ThreadSetup s = _threadSetups.get(i);

			if (s != null)
				s.setup();
		}
	}

	private static ThreadLocal<Deque<BasicAction>> _threadList = new ThreadLocal<Deque<BasicAction>>();

	private static ArrayList<ThreadSetup> _threadSetups = new ArrayList<ThreadSetup>();

}
