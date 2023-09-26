/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.recovery;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.internal.txoj.recovery.RecoveredTransactionalObject;

public class MyRecoveredTO extends RecoveredTransactionalObject
{
    public MyRecoveredTO(Uid objectUid, String originalType,
            ParticipantStore participantStore)
    {
        super(objectUid, originalType, participantStore);
    }
    
    public void replay ()
    {
        super.replayPhase2();
    }
}