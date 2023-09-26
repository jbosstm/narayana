/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.PerfProfile01Impls;



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
import org.jboss.jbossts.qa.PerfProfile01.*;
import org.omg.CosTransactions.Status;

public class AITImplicitObjectImpl01 extends LockManager implements ImplicitObjectOperations
{
	public AITImplicitObjectImpl01()
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
				System.err.println("AITImplicitObjectImpl01.AITImplicitObjectImpl01: failed to get lock");
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
			System.err.println("AITImplicitObjectImpl01.AITImplicitObjectImpl01: " + exception);
			throw new InvocationException();
		}
	}

	public AITImplicitObjectImpl01(Uid uid)
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
			System.err.println("AITImplicitObjectImpl01.finalize: " + exception);
			throw exception;
		}
	}

	public void no_tran_nulloper()
			throws InvocationException
	{
	}

	public void no_tran_readlock()
			throws InvocationException
	{
		try
		{
			if (setlock(new Lock(LockMode.READ), 0) != LockResult.GRANTED)
			{
				System.err.println("AITImplicitObjectImpl01.no_tran_readlock: failed to get lock");

				throw new InvocationException();
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITImplicitObjectImpl01.no_tran_readlock: " + exception);
			throw new InvocationException();
		}
	}

	public void no_tran_writelock()
			throws InvocationException
	{
		try
		{
			if (setlock(new Lock(LockMode.WRITE), 0) != LockResult.GRANTED)
			{
				System.err.println("AITImplicitObjectImpl01.no_tran_writelock: failed to get lock");

				throw new InvocationException();
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITImplicitObjectImpl01.no_tran_writelock: " + exception);
			throw new InvocationException();
		}
	}

	public void tran_commit_nulloper()
			throws InvocationException
	{
		try
		{
			AtomicTransaction atomicTransaction = new AtomicTransaction();

			try
			{
				atomicTransaction.begin();

				atomicTransaction.commit(true);
			}
			catch (Exception exception)
			{
				System.err.println("AITImplicitObjectImpl01.tran_commit_nulloper: " + exception);
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
			System.err.println("AITImplicitObjectImpl01.tran_commit_nulloper: " + exception);
			throw new InvocationException();
		}
	}

	public void tran_commit_readlock()
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
				System.err.println("AITImplicitObjectImpl01.tran_commit_readlock: " + exception);
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
			System.err.println("AITImplicitObjectImpl01.tran_commit_readlock: " + exception);
			throw new InvocationException();
		}
	}

	public void tran_commit_writelock()
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
				System.err.println("AITImplicitObjectImpl01.tran_commit_writelock: " + exception);
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
			System.err.println("AITImplicitObjectImpl01.tran_commit_writelock: " + exception);
			throw new InvocationException();
		}
	}

	public void tran_rollback_nulloper()
			throws InvocationException
	{
		try
		{
			AtomicTransaction atomicTransaction = new AtomicTransaction();

			try
			{
				atomicTransaction.begin();

				atomicTransaction.rollback();
			}
			catch (Exception exception)
			{
				System.err.println("AITImplicitObjectImpl01.tran_rollback_nulloper: " + exception);
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
			System.err.println("AITImplicitObjectImpl01.tran_rollback_nulloper: " + exception);
			throw new InvocationException();
		}
	}

	public void tran_rollback_readlock()
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
					atomicTransaction.rollback();
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
				System.err.println("AITImplicitObjectImpl01.tran_rollback_readlock: " + exception);
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
			System.err.println("AITImplicitObjectImpl01.tran_rollback_readlock: " + exception);
			throw new InvocationException();
		}
	}

	public void tran_rollback_writelock()
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
					atomicTransaction.rollback();
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
				System.err.println("AITImplicitObjectImpl01.tran_rollback_writelock: " + exception);
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
			System.err.println("AITImplicitObjectImpl01.tran_rollback_writelock: " + exception);
			throw new InvocationException();
		}
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
			System.err.println("AITImplicitObjectImpl01.save_state: " + exception);
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
			System.err.println("AITImplicitObjectImpl01.restore_state: " + exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/LockManager/AITImplicitObjectImpl01";
	}

	private int _value;
}