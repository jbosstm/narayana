/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery09Impls;



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
import org.jboss.jbossts.qa.CrashRecovery09.*;
import org.omg.CORBA.IntHolder;

public class AITServiceImpl01 extends LockManager implements ServiceOperations
{
	public AITServiceImpl01()
			throws InvocationException
	{
		super(ObjectType.ANDPERSISTENT);

		_value = 0;
	}

	public AITServiceImpl01(Uid uid)
			throws InvocationException
	{
		super(uid);

		_value = 0;
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
			System.err.println("AITServiceImpl01.finalize: " + exception);
			throw exception;
		}
	}

	public void get(IntHolder value)
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
				throw new InvocationException();
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITServiceImpl01.get: " + exception);
			throw new InvocationException();
		}
	}

	public void set(int value)
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
				throw new InvocationException();
			}
		}
		catch (InvocationException invocationException)
		{
			throw invocationException;
		}
		catch (Exception exception)
		{
			System.err.println("AITServiceImpl01.set: " + exception);
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
			System.err.println("AITServiceImpl01.save_state: " + exception);
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
			System.err.println("AITServiceImpl01.restore_state: " + exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/LockManager/AITServiceImpl01";
	}

	private int _value;
}