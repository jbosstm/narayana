/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.recovery;

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