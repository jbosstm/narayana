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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: FactoryList.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.interposition;


import com.arjuna.ats.jts.extensions.Arjuna;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.arjuna.common.BasicMutex;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.InterpositionCreator;
import com.arjuna.ats.internal.jts.interposition.resources.strict.StrictInterpositionCreator;
import com.arjuna.ats.internal.jts.interposition.resources.restricted.RestrictedInterpositionCreator;
import com.arjuna.ats.internal.jts.interposition.resources.osi.OSIInterpositionCreator;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;

import org.omg.CosTransactions.*;

import org.omg.CORBA.SystemException;

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

	public FactoryList ()
	{
		FactoryList.add(new InterpositionCreator(), Arjuna.XID());
		FactoryList.add(new StrictInterpositionCreator(), Arjuna.strictXID());
		FactoryList.add(new RestrictedInterpositionCreator(), Arjuna.restrictedXID());
		FactoryList.add(new OSIInterpositionCreator(), 0); // 0 is OSI TP!
		FactoryList.addDefault(new OSIInterpositionCreator(), 0); // 0 is OSI
																	// TP!
	}

	public static ControlImple recreateLocal (PropagationContext ctx, int formatID)
			throws SystemException
	{
		ControlImple toReturn = null;

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
	 * 
	 * @message com.arjuna.ats.internal.jts.interposition.fldefault {0} -
	 *          default already set!
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
		else
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.interposition.fldefault", new Object[]
				{ "FactoryList.addDefault" });
			}
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
	private static FactoryElement _default = null; // used if no formatID
													// values match.
	private static BasicMutex _lock = new BasicMutex();

}
