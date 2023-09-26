/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.Hammer02Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
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
import org.omg.CosTransactions.Status;

import java.util.Hashtable;

public class AITMatrixImpl03 extends LockManager implements MatrixOperations
{
	public AITMatrixImpl03(int width, int height)
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
				System.err.println("AITMatrixImpl03.AITMatrixImpl03: failed to get lock");
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
			System.err.println("AITMatrixImpl03.AITMatrixImpl03: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	public AITMatrixImpl03(int width, int height, Uid uid)
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
			System.err.println("AITMatrixImpl03.finalize: " + exception);
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

			AtomicTransaction atomicTransaction = new AtomicTransaction();

			try
			{
				atomicTransaction.begin();

				if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
				{
					try
					{
						_values[x][y].get_value(value);

						atomicTransaction.commit(true);
					}
					catch (InvocationException invocationException)
					{
						atomicTransaction.rollback();

						throw invocationException;
					}
				}
				else
				{
					atomicTransaction.rollback();

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
				System.err.println("AITMatrixImpl03.get_value: " + exception);
				if (atomicTransaction.get_status() == Status.StatusActive)
				{
					atomicTransaction.rollback();
				}

				interposition.unregisterTransaction();

				throw new InvocationException(Reason.ReasonUnknown);
			}
			catch (Error error)
			{
				System.err.println("AITMatrixImpl03.get_value: " + error);
				if (atomicTransaction.get_status() == Status.StatusActive)
				{
					atomicTransaction.rollback();
				}

				interposition.unregisterTransaction();

				throw new InvocationException(Reason.ReasonUnknown);
			}

			interposition.unregisterTransaction();
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITMatrixImpl03.get_value: " + exception);
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

			AtomicTransaction atomicTransaction = new AtomicTransaction();

			try
			{
				atomicTransaction.begin();

				if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
				{
					try
					{
						_values[x][y].set_value(value);

						atomicTransaction.commit(true);
					}
					catch (InvocationException invocationException)
					{
						atomicTransaction.rollback();

						throw invocationException;
					}
				}
				else
				{
					atomicTransaction.rollback();

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
				System.err.println("AITMatrixImpl03.set_value: " + exception);
				if (atomicTransaction.get_status() == Status.StatusActive)
				{
					atomicTransaction.rollback();
				}

				interposition.unregisterTransaction();

				throw new InvocationException(Reason.ReasonUnknown);
			}
			catch (Error error)
			{
				System.err.println("AITMatrixImpl03.set_value: " + error);
				if (atomicTransaction.get_status() == Status.StatusActive)
				{
					atomicTransaction.rollback();
				}

				interposition.unregisterTransaction();

				throw new InvocationException(Reason.ReasonUnknown);
			}

			interposition.unregisterTransaction();
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITMatrixImpl03.set_value: " + exception);
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
			System.err.println("AITMatrixImpl03.save_state: " + exception);
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
			System.err.println("AITMatrixImpl03.restore_state: " + exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/LockManager/AITMatrixImpl03";
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
				AtomicTransaction atomicTransaction = new AtomicTransaction();

				try
				{
					atomicTransaction.begin();

					if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
					{
						value.value = _value;
						atomicTransaction.commit(true);
					}
					else
					{
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
					System.err.println("Element.get_value: " + exception);
					if (atomicTransaction.get_status() == Status.StatusActive)
					{
						atomicTransaction.rollback();
					}

					throw new InvocationException(Reason.ReasonUnknown);
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
				AtomicTransaction atomicTransaction = new AtomicTransaction();

				try
				{
					atomicTransaction.begin();

					if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
					{
						_value = value;
						atomicTransaction.commit(true);
					}
					else
					{
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
					System.err.println("Element.set_value: " + exception);
					if (atomicTransaction.get_status() == Status.StatusActive)
					{
						atomicTransaction.rollback();
					}

					throw new InvocationException(Reason.ReasonUnknown);
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
			return "/StateManager/LockManager/AITMatrixImpl03_Element";
		}

		private int _value;

		private static Hashtable _all = new Hashtable();
	}

	private int _width;
	private int _height;
	private Element[][] _values;
}