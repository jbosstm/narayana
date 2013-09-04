/*
 *  JBoss, Home of Professional Open Source.
 *  Copyright 2013, Red Hat, Inc., and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
