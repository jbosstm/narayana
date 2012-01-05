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
package org.jboss.jbossts.qa.ArjunaCore.LockManager.impl;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

/**
 * Simple record used to test AtomicAction
 */
public class BasicLockRecord extends LockManager
{
	public BasicLockRecord()
	{
		super(ObjectType.ANDPERSISTENT);
	}

	public BasicLockRecord(Uid oldId)
	{
		super(oldId, ObjectType.ANDPERSISTENT);
	}

	protected BasicLockRecord(Uid storeUid, int ot)
	{
		super(storeUid, ot);
	}

	public BasicLockRecord(int id)
	{
		super(ObjectType.ANDPERSISTENT);
		mId = id;
	}

	/**
	 * My methods to test abstract record is being processed correctly by the transaction
	 * manager.
	 */
	public int increase()
	{
		return increase(0);
	}

	public int increase(int retry)
	{
		return increase(retry, 0);
	}

	public int increase(int retry, int wait_time)
	{
		int returnValue = 0;
		int locking_result = LockResult.REFUSED;
		int locking_attempt_count = 0;

		do
		{
			locking_result = setlock(new Lock(LockMode.WRITE), retry, wait_time);

			if (locking_result == LockResult.GRANTED)
			{
				mValue++;
			}
			else
			{
				locking_attempt_count++;
			}
		}
		while ((locking_result != LockResult.GRANTED) && (locking_attempt_count < mLimit));

		if (locking_result != LockResult.GRANTED)
		{
			qautil.qadebug("trying to get lock for " + mLimit + "th time");
		}
		else
		{
			returnValue = 1;
		}

		return returnValue;
	}

	public int getValue()
	{
		return getValue(5);
	}

	public int getValue(int retry)
	{
		return getValue(retry, 250);
	}

	public int getValue(int retry, int wait_time)
	{
		int return_value = 0;
		int locking_result = LockResult.REFUSED;
		int locking_attempt_count = 0;

		do
		{
			locking_result = setlock(new Lock(LockMode.READ), retry, wait_time);

			if (locking_result == LockResult.GRANTED)
			{
				return_value = mValue;
			}
			else
			{
				locking_attempt_count++;
			}
		}
		while ((locking_result != LockResult.GRANTED) && (locking_attempt_count < mLimit));

		if (locking_result != LockResult.GRANTED)
		{
			qautil.qadebug("trying to get lock for " + mLimit + "th time");
		}

		return return_value;
	}

	public boolean save_state(OutputObjectState objectState, int objectType)
	{
		super.save_state(objectState, objectType);
		try
		{
			objectState.packInt(mValue);
			return true;
		}
		catch (Exception exception)
		{
			qautil.debug("BasicLockRecord.save_state: ", exception);
			return false;
		}
	}

	public boolean restore_state(InputObjectState objectState, int objectType)
	{
		super.restore_state(objectState, objectType);
		try
		{
			mValue = objectState.unpackInt();
			return true;
		}
		catch (Exception exception)
		{
			qautil.debug("BasicLockRecord.restore_state: ", exception);
			return false;
		}
	}

	public String type()
	{
		return "/StateManager/LockManager/BasicLockRecord";
	}

	private int mValue = 0;
	private int mLimit = 1000;
	private int mId = 0;
}

