/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.recovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.orbspecific.recovery.recoverycoordinators.GenericRecoveryCoordinator;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class RecoveryCoordinatorUnitTest extends TestBase
{
    @Test
    public void testGeneric () throws Exception
    {
        GenericRecoveryCoordinator rec = new GenericRecoveryCoordinator(new Uid(), new Uid(), new Uid(), false);
        
        rec.replay_completion(null);
        
        assertTrue(GenericRecoveryCoordinator.makeId(new Uid(), new Uid(), new Uid(), true) != null);
        assertEquals(GenericRecoveryCoordinator.reconstruct("foobar"), null);
    }
}