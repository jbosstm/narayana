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
// $Id: AITServiceImpl01.java,v 1.2 2003/06/26 11:43:44 rbegg Exp $
//

package org.jboss.jbossts.qa.CrashRecovery09Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AITServiceImpl01.java,v 1.2 2003/06/26 11:43:44 rbegg Exp $
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
 * $Id: AITServiceImpl01.java,v 1.2 2003/06/26 11:43:44 rbegg Exp $
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
