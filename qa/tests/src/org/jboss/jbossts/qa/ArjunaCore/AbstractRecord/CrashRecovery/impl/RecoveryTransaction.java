/*
 * SPDX short identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.BasicAction;

public class RecoveryTransaction extends BasicAction
{

	public RecoveryTransaction(Uid txId)
	{
		super(txId);

		activate();
	}

	public void doAbort()
	{
		super.phase2Abort(true);
	}

	public void doCommit()
	{
		super.phase2Commit(true);
	}

	public String type()
	{
		return "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction";
	}

}