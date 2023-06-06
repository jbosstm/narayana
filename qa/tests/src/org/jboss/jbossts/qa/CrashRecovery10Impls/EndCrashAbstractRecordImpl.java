/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery10Impls;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

public class EndCrashAbstractRecordImpl extends AbstractRecord
{
	public static final int NO_CRASH = 0;
	public static final int CRASH_IN_PREPARE = 1;
	public static final int CRASH_IN_COMMIT = 2;
	public static final int CRASH_IN_ABORT = 3;

	public EndCrashAbstractRecordImpl(int crashBehavior)
	{
		//
		// to get the appropriate ordering it is necessary to
		// fabricate a suitable objectUid
		//
		super(new Uid("7FFFFFFF:7FFFFFFF:0:0:0"), "EndCrashAbstractRecord", ObjectType.NEITHER);

		_crashBehavior = crashBehavior;
	}

	public int typeIs()
	{
		return RecordType.USER_DEF_LAST9;
	}

	public Object value()
	{
		return null;
	}

	public void setValue(Object object)
	{
	}

	public int nestedAbort()
	{
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedCommit()
	{
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedPrepare()
	{
		return TwoPhaseOutcome.PREPARE_OK;
	}

	public int topLevelAbort()
	{
		if (_crashBehavior == CRASH_IN_ABORT)
		{
			System.out.println("Passed");
			System.exit(0);
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelCommit()
	{
		if (_crashBehavior == CRASH_IN_COMMIT)
		{
			System.out.println("Passed");
			System.exit(0);
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelPrepare()
	{
		if (_crashBehavior == CRASH_IN_PREPARE)
		{
			System.out.println("Passed");
			System.exit(0);
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

	private int _crashBehavior = NO_CRASH;
}