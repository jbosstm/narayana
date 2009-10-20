/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
//
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//
// $Id: AITMatrixImpl04.java,v 1.2 2003/06/26 11:44:01 rbegg Exp $
//

package org.jboss.jbossts.qa.Hammer02Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AITMatrixImpl04.java,v 1.2 2003/06/26 11:44:01 rbegg Exp $
 */

/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AITMatrixImpl04.java,v 1.2 2003/06/26 11:44:01 rbegg Exp $
 */


import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.jts.extensions.AtomicTransaction;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import org.jboss.jbossts.qa.Hammer02.*;
import org.omg.CORBA.IntHolder;
import org.omg.CosTransactions.Control;

import java.util.Hashtable;

public class AITMatrixImpl04 extends LockManager implements MatrixOperations
{
	public AITMatrixImpl04(int width, int height)
			throws InvocationException
	{
		super(ObjectType.ANDPERSISTENT);

		_width = width;
		_height = height;

		_values = new Element[_width][];
		for (int x = 0; x < _width; x++)
		{
			_values[x] = new Element[_height];
		}

		for (int x = 0; x < _width; x++)
		{
			for (int y = 0; y < _height; y++)
			{
				if (y < (_height / 2))
				{
					_values[x][y] = Element.create(0);
				}
				else
				{
					_values[x][y] = Element.create(1);
				}
			}
		}
		try
		{
			AtomicTransaction atomicTransaction = new AtomicTransaction();

			atomicTransaction.begin();

			if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
			{
				atomicTransaction.commit(true);
			}
			else
			{
				System.err.println("AITMatrixImpl04.AITMatrixImpl04: failed to get lock");
				atomicTransaction.rollback();

				throw new InvocationException(Reason.ReasonConcurrencyControl);
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITMatrixImpl04.AITMatrixImpl04: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	public AITMatrixImpl04(int width, int height, Uid uid)
			throws InvocationException
	{
		super(uid);

		_width = width;
		_height = height;

		_values = new Element[_width][];
		for (int x = 0; x < _width; x++)
		{
			_values[x] = new Element[_height];
		}
	}

	public void finalize()
			throws Throwable
	{
		try
		{
			super.terminate();
			super.finalize();
		}
		catch (Exception exception)
		{
			System.err.println("AITMatrixImpl04.finalize: " + exception);
			throw exception;
		}
	}

	public int get_width()
			throws InvocationException
	{
		return _width;
	}

	public int get_height()
			throws InvocationException
	{
		return _height;
	}

	public void get_value(int x, int y, IntHolder value, Control ctrl)
			throws InvocationException
	{
		if ((x < 0) || (x >= _width) || (y < 0) || (y >= _height))
		{
			throw new InvocationException(Reason.ReasonUnknown);
		}

		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();

			interposition.registerTransaction(ctrl);

			try
			{
				if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
				{
					_values[x][y].get_value(value);
				}
				else
				{
// Modified 15/01/2001 K Jones: Removed 'interposition.unregisterTransaction()'
					throw new InvocationException(Reason.ReasonConcurrencyControl);
				}
			}
			catch (InvocationException invocationException)
			{
				interposition.unregisterTransaction();

				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITMatrixImpl04.get_value: " + exception);
				interposition.unregisterTransaction();

				throw new InvocationException();
			}
			catch (Error error)
			{
				System.err.println("AITMatrixImpl04.get_value: " + error);
				interposition.unregisterTransaction();

				throw new InvocationException();
			}

			interposition.unregisterTransaction();
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITMatrixImpl04.get_value: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	public void set_value(int x, int y, int value, Control ctrl)
			throws InvocationException
	{
		if ((x < 0) || (x >= _width) || (y < 0) || (y >= _height))
		{
			throw new InvocationException(Reason.ReasonUnknown);
		}

		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();

			interposition.registerTransaction(ctrl);

			try
			{
				if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
				{
					_values[x][y].set_value(value);
				}
				else
				{
// Modified 15/01/2001 K Jones: Removed 'interposition.unregisterTransaction()'
					throw new InvocationException(Reason.ReasonConcurrencyControl);
				}
			}
			catch (InvocationException invocationException)
			{
				interposition.unregisterTransaction();

				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITMatrixImpl04.set_value: " + exception);
				interposition.unregisterTransaction();

				throw new InvocationException();
			}
			catch (Error error)
			{
				System.err.println("AITMatrixImpl04.set_value: " + error);
				interposition.unregisterTransaction();

				throw new InvocationException();
			}

			interposition.unregisterTransaction();
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITMatrixImpl04.set_value: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	public boolean save_state(OutputObjectState objectState, int objectType)
	{
		super.save_state(objectState, objectType);
		try
		{
			for (int x = 0; x < _width; x++)
			{
				for (int y = 0; y < _height; y++)
				{
				    UidHelper.packInto(_values[x][y].get_uid(), objectState);
				}
			}

			return true;
		}
		catch (Exception exception)
		{
			System.err.println("AITMatrixImpl04.save_state: " + exception);
			return false;
		}
	}

	public boolean restore_state(InputObjectState objectState, int objectType)
	{
		super.restore_state(objectState, objectType);
		try
		{
			for (int x = 0; x < _width; x++)
			{
				for (int y = 0; y < _height; y++)
				{
					Uid uid = UidHelper.unpackFrom(objectState);

					_values[x][y] = Element.obtain(uid);
				}
			}

			return true;
		}
		catch (Exception exception)
		{
			System.err.println("AITMatrixImpl04.restore_state: " + exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/LockManager/AITMatrixImpl04";
	}

	private static class Element extends LockManager
	{
		private Element(int value)
				throws InvocationException
		{
			super(ObjectType.ANDPERSISTENT);

			_value = value;

			try
			{
				AtomicTransaction atomicTransaction = new AtomicTransaction();

				atomicTransaction.begin();

				if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
				{
					atomicTransaction.commit(true);
				}
				else
				{
					System.err.println("Element.Element: failed to get lock");
					atomicTransaction.rollback();

					throw new InvocationException(Reason.ReasonConcurrencyControl);
				}
			}
			catch (InvocationException invocationException)
			{
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("Element.Element: " + exception);
				throw new InvocationException(Reason.ReasonUnknown);
			}
		}

		private Element(Uid uid)
				throws InvocationException
		{
			super(uid);
		}

		public void finalize()
				throws Throwable
		{
			try
			{
				super.terminate();
				super.finalize();
			}
			catch (Exception exception)
			{
				System.err.println("Element.finalize: " + exception);
				throw exception;
			}
		}

		public void get_value(IntHolder value)
				throws InvocationException
		{
			try
			{
				if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
				{
					value.value = _value;
				}
				else
				{
					throw new InvocationException(Reason.ReasonConcurrencyControl);
				}
			}
			catch (InvocationException invocationException)
			{
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("Element.get_value: " + exception);
				throw new InvocationException(Reason.ReasonUnknown);
			}
		}

		public void set_value(int value)
				throws InvocationException
		{
			try
			{
				if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
				{
					_value = value;
				}
				else
				{
					throw new InvocationException(Reason.ReasonConcurrencyControl);
				}
			}
			catch (InvocationException invocationException)
			{
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("Element.set_value: " + exception);
				throw new InvocationException(Reason.ReasonUnknown);
			}
		}

		public static Element create(int value)
		{
			Element element;

			try
			{
				element = new Element(value);

				_all.put(element.get_uid(), element);
			}
			catch (Exception exception)
			{
				element = null;
			}

			return element;
		}

		public static Element obtain(Uid uid)
		{
			Element element;

			element = (Element) _all.get(uid);

			if (element == null)
			{
				try
				{
					element = new Element(uid);
				}
				catch (Exception exception)
				{
					element = null;
				}

				_all.put(uid, element);
			}

			return element;
		}

		public boolean save_state(OutputObjectState objectState, int objectType)
		{
			super.save_state(objectState, objectType);
			try
			{
				objectState.packInt(_value);

				return true;
			}
			catch (Exception exception)
			{
				System.err.println("Element.save_state: " + exception);
				return false;
			}
		}

		public boolean restore_state(InputObjectState objectState, int objectType)
		{
			super.restore_state(objectState, objectType);
			try
			{
				_value = objectState.unpackInt();

				return true;
			}
			catch (Exception exception)
			{
				System.err.println("Element.restore_state: " + exception);
				return false;
			}
		}

		public String type()
		{
			return "/StateManager/LockManager/AITMatrixImpl04_Element";
		}

		private int _value;

		private static Hashtable _all = new Hashtable();
	}

	private int _width;
	private int _height;
	private Element[][] _values;
}
