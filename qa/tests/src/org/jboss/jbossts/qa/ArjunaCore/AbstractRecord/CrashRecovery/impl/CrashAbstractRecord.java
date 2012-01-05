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
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

/**
 * Simple record used to test AtomicAction
 */
public class CrashAbstractRecord extends AbstractRecord
{
	public CrashAbstractRecord()
	{
		this(1, 0);
	}

	/**
	 * Crashpoint will be used to set the point at which the crash will occur  the type of crash
	 * will be determined by crashtype(0 = system.exit(), 1 = Fail )
	 */
	public CrashAbstractRecord(int crashpoint, int crashtype)
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

	/**
	 * The default action of this record is to crash on commit.
	 */
	public int topLevelCommit()
	{
		if (mCrashPoint == 1)
		{
			qautil.qadebug("Abstract record is crashing on top level commit");
			if (mCrashType == 0)
			{
				System.out.println("Passed");
				System.exit(0);
			}
			else
			{
				return TwoPhaseOutcome.FINISH_ERROR;
			}
		}
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelAbort()
	{
		if (mCrashPoint == 2)
		{
			qautil.qadebug("Abstract record is crashing on top level commit");
			if (mCrashType == 0)
			{
				System.out.println("Passed");
				System.exit(0);
			}
			else
			{
				return TwoPhaseOutcome.FINISH_ERROR;
			}
		}
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelPrepare()
	{
		if (mCrashPoint == 3)
		{
			qautil.qadebug("Abstract record is crashing on top level prepare");
			if (mCrashType == 0)
			{
				System.out.println("Passed");
				System.exit(0);
			}
			else
			{
				return TwoPhaseOutcome.FINISH_ERROR;
			}
		}
		return TwoPhaseOutcome.PREPARE_OK;
	}

	public int nestedCommit()
	{
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedAbort()
	{
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedPrepare()
	{
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

	private int mCrashPoint = 0;
	private int mCrashType = 0; //default is 0(exit vm) 1(return fail)
}

