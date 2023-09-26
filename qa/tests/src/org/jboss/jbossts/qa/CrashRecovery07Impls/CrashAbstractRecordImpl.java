/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


//

package org.jboss.jbossts.qa.CrashRecovery07Impls;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;

public class CrashAbstractRecordImpl extends AbstractRecord
{
	public CrashAbstractRecordImpl()
	{
	}

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
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelCommit()
	{
		System.out.println("Passed");
		System.exit(0);

		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelPrepare()
	{
		return TwoPhaseOutcome.PREPARE_OK;
	}

	public void alter(AbstractRecord abstractRecord)
	{
	}

	public void merge(AbstractRecord abstractRecord)
	{
	}

	/*
	 public boolean equals(AbstractRecord abstractRecord)
	 {
	 return false;
	 }

	 public boolean lessThan(AbstractRecord abstractRecord)
	 {
	 return true;
	 }

	 public boolean greaterThan(AbstractRecord abstractRecord)
	 {
	 return false;
	 }
 */
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
}