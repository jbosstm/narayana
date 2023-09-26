/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.interposition;


import java.util.concurrent.locks.ReentrantLock;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;

import com.arjuna.ats.internal.jts.interposition.resources.arjuna.InterpositionCreator;
import com.arjuna.ats.internal.jts.interposition.resources.osi.OSIInterpositionCreator;
import com.arjuna.ats.internal.jts.interposition.resources.restricted.RestrictedInterpositionCreator;
import com.arjuna.ats.internal.jts.interposition.resources.strict.StrictInterpositionCreator;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.jts.extensions.Arjuna;
import com.arjuna.ats.jts.logging.jtsLogger;

/*
 * Default visibility.
 */

class FactoryElement
{
	public FactoryElement (FactoryCreator create, int formatID)
	{
		_create = create;
		_formatID = formatID;
		_next = null;
	}

	public ControlImple recreateLocal (PropagationContext ctx)
			throws SystemException
	{
		return _create.recreateLocal(ctx);
	}

	public Control recreate (PropagationContext ctx) throws SystemException
	{
		return recreateLocal(ctx).getControl();
	}

	public int _formatID;

	public FactoryElement _next;

	private FactoryCreator _create;
}

/*
 * Add the Arjuna OTS transaction recreator as a minimum, i.e., no special
 * action is required to know about JBoss transactions.
 */

/**
 * Maintains the list of known transaction interposition factories.
 */

public class FactoryList
{
    public static final int DEFAULT_ID = 0;
    
	public FactoryList ()
	{
		FactoryList.add(new InterpositionCreator(), Arjuna.XID());
		FactoryList.add(new StrictInterpositionCreator(), Arjuna.strictXID());
		FactoryList.add(new RestrictedInterpositionCreator(), Arjuna.restrictedXID());
		FactoryList.add(new OSIInterpositionCreator(), 0); // 0 is OSI TP!
		FactoryList.addDefault(new OSIInterpositionCreator(), DEFAULT_ID); // 0 is OSI TP!
	}

	public static ControlImple recreateLocal (PropagationContext ctx, int formatID)
			throws SystemException
	{
		ControlImple toReturn = null;

		if (ctx == null)
                    throw new INVALID_TRANSACTION();
		
		FactoryElement ptr = find(formatID);

		if (ptr != null)
		{
			toReturn = ptr.recreateLocal(ctx);
		}

		return toReturn;
	}

	public static Control recreate (PropagationContext ctx, int formatID)
			throws SystemException
	{
		Control toReturn = null;
		
		if (ctx == null)
		    throw new INVALID_TRANSACTION();

		FactoryElement ptr = find(formatID);

		if (ptr != null)
		{
			toReturn = ptr.recreate(ctx);
		}

		return toReturn;
	}

	public static void add (FactoryCreator create, int formatID)
	{
		FactoryElement ptr = find(formatID);

		_lock.lock();

		if (ptr == null) // assume that the create and id always match
		{
			ptr = new FactoryElement(create, formatID);
			ptr._next = _list;
			_list = ptr;
		}

		_lock.unlock();
	}

	public static void remove (int formatID)
	{
		_lock.lock();

		FactoryElement ptr = _list;
		FactoryElement trail = null;
		boolean found = false;

		while ((ptr != null) && (!found))
		{
			if (ptr._formatID == formatID)
				found = true;
			else
			{
				trail = ptr;
				ptr = ptr._next;
			}
		}

		if (found)
		{
			if (_list == ptr)
				_list = ptr._next;
			else
			{
				if (trail != null)
					trail._next = ptr._next;
			}

			ptr._next = null;

			ptr = null;
		}

		_lock.unlock();
	}

	/**
	 * Only allow a default to be added once!
	 */

	public static boolean addDefault (FactoryCreator create, int formatID)
	{
		boolean res = false;

		_lock.lock();

		if (FactoryList._default == null)
		{
			FactoryList._default = new FactoryElement(create, formatID);
			res = true;
		}
		else {
            jtsLogger.i18NLogger.warn_interposition_fldefault("FactoryList.addDefault");
        }

		_lock.unlock();

		return res;
	}

	public static boolean removeDefault ()
	{
		boolean found = false;

		_lock.lock();

		if (FactoryList._default != null)
		{
			FactoryList._default = null;
			found = true;
		}

		_lock.unlock();

		return found;
	}

	protected static FactoryElement find (int formatID)
	{
		FactoryElement ptr = _list;
		FactoryElement toReturn = null;

		_lock.lock();

		while ((ptr != null) && (toReturn == null))
		{
			if (ptr._formatID == formatID)
				toReturn = ptr;
			else
				ptr = ptr._next;
		}

		if (toReturn == null)
		{
			/*
			 * No ID matches, so use default.
			 */

			toReturn = FactoryList._default;
		}

		_lock.unlock();

		return toReturn;
	}

	private static FactoryElement _list = null;
	private static FactoryElement _default = null; // used if no formatID values match.
	private static ReentrantLock _lock = new ReentrantLock();
}