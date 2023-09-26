/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.AITResources02Impls;



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
import org.jboss.jbossts.qa.AITResources02.*;
import org.jboss.jbossts.qa.Utils.JVMStats;
import org.omg.CORBA.IntHolder;
import org.omg.CosTransactions.Control;

public class AITCounterImpl03 extends LockManager implements CounterOperations
{
	public AITCounterImpl03()
			throws InvocationException
	{
		super(ObjectType.ANDPERSISTENT);

		_value = 0;

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
				System.err.println("AITCounterImpl03.AITCounterImpl03: failed to get lock");
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
			System.err.println("AITCounterImpl03.AITCounterImpl03: " + exception);
			throw new InvocationException();
		}
	}

	public AITCounterImpl03(Uid uid)
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
			System.err.println("AITCounterImpl03.finalize: " + exception);
			throw exception;
		}
	}

	public void get(IntHolder value, Control ctrl)
			throws InvocationException
	{
		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();

			interposition.registerTransaction(ctrl);

			try
			{
				if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED)
				{
					value.value = _value;
				}
				else
				{
					//   Modified 15/01/2001 K Jones:  'interposition.unregisterTransaction()' removed
					throw new InvocationException();
				}
			}
			catch (InvocationException invocationException)
			{
				interposition.unregisterTransaction();
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITCounterImpl03.get: " + exception);
				interposition.unregisterTransaction();
				throw new InvocationException();
			}
			catch (Error error)
			{
				System.err.println("AITCounterImpl03.get: " + error);
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
			System.err.println("AITCounterImpl03.get: " + exception);
			throw new InvocationException();
		}
	}

	public void set(int value, Control ctrl)
			throws InvocationException
	{
		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();

			interposition.registerTransaction(ctrl);

			try
			{
				if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
				{
					_value = value;
				}
				else
				{
//   Modified 15/01/2001 K Jones:  'interposition.unregisterTransaction()' removed
					throw new InvocationException();
				}
			}
			catch (InvocationException invocationException)
			{
				interposition.unregisterTransaction();
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITCounterImpl03.set: " + exception);
				interposition.unregisterTransaction();
				throw new InvocationException();
			}
			catch (Error error)
			{
				System.err.println("AITCounterImpl03.set: " + error);
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
			System.err.println("AITCounterImpl03.set: " + exception);
			throw new InvocationException();
		}
	}

	public void increase(Control ctrl)
			throws InvocationException
	{
		try
		{
			com.arjuna.ats.jts.ExplicitInterposition interposition = new com.arjuna.ats.jts.ExplicitInterposition();

			interposition.registerTransaction(ctrl);
			try
			{
				if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)
				{
					_value++;
				}
				else
				{
//   Modified 15/01/2001 K Jones:  'interposition.unregisterTransaction()' removed
					throw new InvocationException();
				}
			}
			catch (InvocationException invocationException)
			{
				interposition.unregisterTransaction();
				throw invocationException;
			}
			catch (Exception exception)
			{
				System.err.println("AITCounterImpl03.increase: " + exception);
				interposition.unregisterTransaction();
				throw new InvocationException();
			}
			catch (Error error)
			{
				System.err.println("AITCounterImpl03.increase: " + error);
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
			System.err.println("AITCounterImpl03.increase: " + exception);
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
			System.err.println("AITCounterImpl03.save_state: " + exception);
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
			System.err.println("AITCounterImpl03.restore_state: " + exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/LockManager/AITCounterImpl03";
	}

	private int _value;
}