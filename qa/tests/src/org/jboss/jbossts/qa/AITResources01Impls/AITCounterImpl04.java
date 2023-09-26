/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//
/*
*
* This remote object has been created to test jiterbug issue 264
*
* The class is a copy of impl01 but the setlock() method has been altered
* so that the issue can be tested. This remote object is only used in test 59
* at this time
*						# Author P.Craddock
*						# 09/08/01
*
*/
package org.jboss.jbossts.qa.AITResources01Impls;



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
import org.jboss.jbossts.qa.AITResources01.*;
import org.jboss.jbossts.qa.Utils.JVMStats;
import org.omg.CORBA.IntHolder;
import org.omg.CosTransactions.Status;

public class AITCounterImpl04 extends LockManager implements CounterOperations
{
	public AITCounterImpl04()
			throws InvocationException
	{
		super(ObjectType.ANDPERSISTENT);

		_value = 0;

		try
		{
			AtomicTransaction atomicTransaction = new AtomicTransaction();

			atomicTransaction.begin();

			if (setlock(new Lock(LockMode.WRITE), super.waitTotalTimeout) == LockResult.GRANTED)
			{
				atomicTransaction.commit(true);
			}
			else
			{
				System.err.println("AITCounterImpl04.AITCounterImpl04: failed to get lock");
				atomicTransaction.rollback();

				throw new InvocationException();
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITCounterImpl04.AITCounterImpl04: " + exception);
			throw new InvocationException();
		}
	}

	public AITCounterImpl04(Uid uid)
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
			System.err.println("AITCounterImpl04.finalize: " + exception);
			throw exception;
		}
	}

	public void get(IntHolder value)
			throws InvocationException
	{
		try
		{
			AtomicTransaction atomicTransaction = new AtomicTransaction();

			try
			{
				atomicTransaction.begin();

				if (setlock(new Lock(LockMode.READ), super.waitTotalTimeout) == LockResult.GRANTED)
				{
					value.value = _value;
					atomicTransaction.commit(true);
				}
				else
				{
					atomicTransaction.rollback();

					throw new InvocationException();
				}
			}
			catch (InvocationException invocationException)
			{
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITCounterImpl04.get: " + exception);
				if (atomicTransaction.get_status() == Status.StatusActive)
				{
					atomicTransaction.rollback();
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
			System.err.println("AITCounterImpl04.get: " + exception);
			throw new InvocationException();
		}
	}

	public void set(int value)
			throws InvocationException
	{
		try
		{
			AtomicTransaction atomicTransaction = new AtomicTransaction();

			try
			{
				atomicTransaction.begin();

				if (setlock(new Lock(LockMode.WRITE), super.waitTotalTimeout) == LockResult.GRANTED)
				{
					_value = value;
					atomicTransaction.commit(true);
				}
				else
				{
					atomicTransaction.rollback();

					throw new InvocationException();
				}
			}
			catch (InvocationException invocationException)
			{
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITCounterImpl04.set: " + exception);
				if (atomicTransaction.get_status() == Status.StatusActive)
				{
					atomicTransaction.rollback();
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
			System.err.println("AITCounterImpl04.set: " + exception);
			throw new InvocationException();
		}
	}

	public void increase()
			throws InvocationException
	{
		try
		{
			AtomicTransaction atomicTransaction = new AtomicTransaction();

			try
			{
				atomicTransaction.begin();

				if (setlock(new Lock(LockMode.WRITE), super.waitTotalTimeout) == LockResult.GRANTED)
				{
					_value++;
					atomicTransaction.commit(true);
				}
				else
				{
					atomicTransaction.rollback();

					throw new InvocationException();
				}
			}
			catch (InvocationException invocationException)
			{
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITCounterImpl04.increase: " + exception);
				if (atomicTransaction.get_status() == Status.StatusActive)
				{
					atomicTransaction.rollback();
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
			System.err.println("AITCounterImpl04.increase: " + exception);
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
			System.err.println("AITCounterImpl04.save_state: " + exception);
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
			System.err.println("AITCounterImpl04.restore_state: " + exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/LockManager/AITCounterImpl04";
	}

	private int _value;
}