/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.AITResources01Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import org.jboss.jbossts.qa.AITResources01.*;
import org.jboss.jbossts.qa.Utils.JVMStats;
import org.jboss.jbossts.qa.Utils.OTS;
import org.omg.CORBA.IntHolder;
import org.omg.CosTransactions.Status;

public class AITCounterImpl02 extends LockManager implements CounterOperations
{
	public AITCounterImpl02()
			throws InvocationException
	{
		super(ObjectType.ANDPERSISTENT);

		_value = 0;

		try
		{
			org.omg.CosTransactions.Current current = OTS.current();

			current.begin();

			if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
			{
				current.commit(true);
			}
			else
			{
				System.err.println("AITCounterImpl02.AITCounterImpl02: failed to get lock");
				current.rollback();

				throw new InvocationException();
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITCounterImpl02.AITCounterImpl02: " + exception);
			throw new InvocationException();
		}
	}

	public AITCounterImpl02(Uid uid)
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
			System.err.println("AITCounterImpl02.finalize: " + exception);
			throw exception;
		}
	}

	public void get(IntHolder value)
			throws InvocationException
	{
		try
		{
			org.omg.CosTransactions.Current current = OTS.current();

			try
			{
				current.begin();

				if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
				{
					value.value = _value;
					current.commit(true);
				}
				else
				{
					current.rollback();

					throw new InvocationException();
				}
			}
			catch (InvocationException invocationException)
			{
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITCounterImpl02.get: " + exception);
				if (current.get_status() == Status.StatusActive)
				{
					current.rollback();
				}

				throw new InvocationException();
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITCounterImpl02.get: " + exception);
			throw new InvocationException();
		}
	}

	public void set(int value)
			throws InvocationException
	{
		try
		{
			org.omg.CosTransactions.Current current = OTS.current();

			try
			{
				current.begin();

				if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
				{
					_value = value;
					current.commit(true);
				}
				else
				{
					current.rollback();

					throw new InvocationException();
				}
			}
			catch (InvocationException invocationException)
			{
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITCounterImpl02.set: " + exception);
				if (current.get_status() == Status.StatusActive)
				{
					current.rollback();
				}

				throw new InvocationException();
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITCounterImpl02.set: " + exception);
			throw new InvocationException();
		}
	}

	public void increase()
			throws InvocationException
	{
		try
		{
			org.omg.CosTransactions.Current current = OTS.current();

			try
			{
				current.begin();

				if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
				{
					_value++;
					current.commit(true);
				}
				else
				{
					current.rollback();

					throw new InvocationException();
				}
			}
			catch (InvocationException invocationException)
			{
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITCounterImpl02.increase: " + exception);
				if (current.get_status() == Status.StatusActive)
				{
					current.rollback();
				}

				throw new InvocationException();
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITCounterImpl02.increase: " + exception);
			throw new InvocationException();
		}
	}

	public int getMemory()
	{
		return (int) JVMStats.getMemory();
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
			System.err.println("AITCounterImpl02.save_state: " + exception);
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
			System.err.println("AITCounterImpl02.restore_state: " + exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/LockManager/AITCounterImpl02";
	}

	private int _value;
}