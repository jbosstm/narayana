/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.Hammer01Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.jts.extensions.AtomicTransaction;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import org.jboss.jbossts.qa.Hammer01.*;
import org.omg.CORBA.IntHolder;

public class AITMatrixImpl02 extends LockManager implements MatrixOperations
{
	public AITMatrixImpl02(int width, int height)
			throws InvocationException
	{
		super(ObjectType.ANDPERSISTENT);

		_width = width;
		_height = height;

		_values = new int[_width][];
		for (int x = 0; x < _width; x++)
		{
			_values[x] = new int[_height];
		}

		for (int x = 0; x < _width; x++)
		{
			for (int y = 0; y < _height; y++)
			{
				if (y < (_height / 2))
				{
					_values[x][y] = 0;
				}
				else
				{
					_values[x][y] = 1;
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
				System.err.println("AITMatrixImpl02.AITMatrixImpl02: failed to get lock");
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
			System.err.println("AITMatrixImpl02.AITMatrixImpl02: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	public AITMatrixImpl02(int width, int height, Uid uid)
			throws InvocationException
	{
		super(uid);

		_width = width;
		_height = height;

		_values = new int[_width][];
		for (int x = 0; x < _width; x++)
		{
			_values[x] = new int[_height];
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
			System.err.println("AITMatrixImpl02.finalize: " + exception);
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

	public void get_value(int x, int y, IntHolder value)
			throws InvocationException
	{
		if ((x < 0) || (x >= _width) || (y < 0) || (y >= _height))
		{
			throw new InvocationException(Reason.ReasonUnknown);
		}

		try
		{
			if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
			{
				value.value = _values[x][y];
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
			System.err.println("AITMatrixImpl02.get_value: " + exception);
			throw new InvocationException(Reason.ReasonUnknown);
		}
	}

	public void set_value(int x, int y, int value)
			throws InvocationException
	{
		if ((x < 0) || (x >= _width) || (y < 0) || (y >= _height))
		{
			throw new InvocationException(Reason.ReasonUnknown);
		}

		try
		{
			if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
			{
				_values[x][y] = value;
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
			System.err.println("AITMatrixImpl02.set_value: " + exception);
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
					objectState.packInt(_values[x][y]);
				}
			}

			return true;
		}
		catch (Exception exception)
		{
			System.err.println("AITMatrixImpl02.save_state: " + exception);
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
					_values[x][y] = objectState.unpackInt();
				}
			}

			return true;
		}
		catch (Exception exception)
		{
			System.err.println("AITMatrixImpl02.restore_state: " + exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/LockManager/AITMatrixImpl02";
	}

	private int _width;
	private int _height;
	private int[][] _values = null;
}