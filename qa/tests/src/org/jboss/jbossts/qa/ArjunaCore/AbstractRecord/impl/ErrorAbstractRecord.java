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
/*
 * Created by IntelliJ IDEA.
 * User: peter craddock
 * Date: 12-Mar-02
 * Time: 14:36:10
 */
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class ErrorAbstractRecord extends AbstractRecord
{
	/**
	 * default constructor will call main constructor setting crash point and type
	 * so that no crash will occur.
	 */
	public ErrorAbstractRecord()
	{
		this(0, 0);
	}

	public ErrorAbstractRecord(int crashpoint, int crashtype)
	{
		super(new Uid());
		mCrashPoint = crashpoint;
		mCrashType = crashtype;
	}

	/**
	 * Typeis is over-riden to force TransactionManager to process this record first.
	 */
	public int typeIs()
	{
		return RecordType.USER_DEF_FIRST0;
	}

	public Object value()
	{
		return null;
	}

	public void setValue(Object object)
	{
	}

	public int topLevelOnePhaseCommit()
	{
		qautil.qadebug("top level one phase commit has been called : " + order());
		if (mCrashPoint == 1)
		{
			qautil.qadebug("Changing return value on top level commit");
			return outcome();
		}
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelCommit()
	{
		qautil.qadebug("top level commit has been called : " + order());
		if (mCrashPoint == 1)
		{
			qautil.qadebug("Changing return value on top level commit");
			return outcome();
		}
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelAbort()
	{
		qautil.qadebug("top level abort has been called : " + order());
		if (mCrashPoint == 2)
		{
			qautil.qadebug("Changing return value on top level abort");
			return outcome();
		}
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelPrepare()
	{
		qautil.qadebug("prep has been called : " + order());
		if (mCrashPoint == 3)
		{
			qautil.qadebug("Changing return value on top level prepare");
			return outcome();
		}
		return TwoPhaseOutcome.PREPARE_OK;
	}

	public int nestedOnePhaseCommit()
	{

		qautil.qadebug("nested one phase comit has been called : " + order());
		if (mCrashPoint == 4)
		{
			qautil.qadebug("Changing return value on nested one phase commit");
			return outcome();
		}
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedCommit()
	{

		qautil.qadebug("nested comit has been called : " + order());
		if (mCrashPoint == 4)
		{
			qautil.qadebug("Changing return value on nested commit");
			return outcome();
		}
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedAbort()
	{
		qautil.qadebug("nested abort has been called : " + order());
		if (mCrashPoint == 5)
		{
			qautil.qadebug("Changing return value on nested abort");
			return outcome();
		}
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedPrepare()
	{
		if (mCrashPoint == 6)
		{
			qautil.qadebug("Changing return value on nested prepare");
			return outcome();
		}
		return TwoPhaseOutcome.PREPARE_OK;
	}

	public void alter(AbstractRecord abstractRecord)
	{
	}

	public void merge(AbstractRecord abstractRecord)
	{
	}

	public boolean shouldAdd(AbstractRecord abstractRecord)
	{
		return false;
	}

	public boolean shouldAlter(AbstractRecord abstractRecord)
	{
		return false;
	}

	public boolean shouldMerge(AbstractRecord abstractRecord)
	{
		return false;
	}

	public boolean shouldReplace(AbstractRecord abstractRecord)
	{
		return false;
	}

	private int outcome()
	{
		int value = TwoPhaseOutcome.FINISH_OK;
		;//default

		if (mCrashType == 0)
		{
			value = TwoPhaseOutcome.PREPARE_OK;
		}
		else if (mCrashType == 1)
		{
			value = TwoPhaseOutcome.PREPARE_NOTOK;
		}
		else if (mCrashType == 2)
		{
			value = TwoPhaseOutcome.PREPARE_READONLY;
		}
		else if (mCrashType == 3)
		{
			value = TwoPhaseOutcome.HEURISTIC_ROLLBACK;
		}
		else if (mCrashType == 4)
		{
			value = TwoPhaseOutcome.HEURISTIC_COMMIT;
		}
		else if (mCrashType == 5)
		{
			value = TwoPhaseOutcome.HEURISTIC_MIXED;
		}
		else if (mCrashType == 6)
		{
			value = TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
		else if (mCrashType == 7)
		{
			value = TwoPhaseOutcome.FINISH_OK;
		}
		else if (mCrashType == 8)
		{
			value = TwoPhaseOutcome.FINISH_ERROR;
		}
		else if (mCrashType == 9)
		{
			value = TwoPhaseOutcome.NOT_PREPARED;
		}
		else if (mCrashType == 10)
		{
			value = TwoPhaseOutcome.ONE_PHASE_ERROR;
		}
		else if (mCrashType == 11)
		{
			value = TwoPhaseOutcome.INVALID_TRANSACTION;
		}
//        else if (mCrashType == 12)
//            return TwoPhaseOutcome.IGNORE_PHASE;
		qautil.qadebug("return value = " + value + " " + TwoPhaseOutcome.stringForm(value));
		return value;
	}

	public int getCrashPoint()
	{
		return mCrashPoint;
	}

	public void setCrashPoint(int mCrashPoint)
	{
		this.mCrashPoint = mCrashPoint;
	}

	private int mCrashPoint = 0;

	public int getCrashType()
	{
		return mCrashType;
	}

	public void setCrashType(int mCrashType)
	{
		this.mCrashType = mCrashType;
	}

	private int mCrashType = 7; //default is 7 these are the return values of TwoPhaseOutcome
}
