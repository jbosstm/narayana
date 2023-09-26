/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import org.junit.BeforeClass;
import org.junit.Test;

public class AtomicActionAsyncHeuristicTest extends AtomicActionTestBase
{
    @BeforeClass
    public static void init() {
        AtomicActionTestBase.init(true);
        // heuristic tests rely on synchronous commits
        arjPropertyManager.getCoordinatorEnvironmentBean().setAsyncCommit(false);
    }

    /**
     * Verifies that action participants are notified of heuristic outcomes.
     * This test runs with synchronous commits. For other heuristic test configurations:
     * @see com.hp.mwtests.ts.arjuna.atomicaction.AtomicActionSyncTest#testHeuristicNotification1()
     * @see com.hp.mwtests.ts.arjuna.atomicaction.AtomicActionSyncTest#testHeuristicNotification2()
     * @see AtomicActionAsyncTest#testHeuristicNotification()
     */
    @Test
    public void testHeuristicNotification() throws Exception {
        super.testHeuristicNotification(false);
    }
}