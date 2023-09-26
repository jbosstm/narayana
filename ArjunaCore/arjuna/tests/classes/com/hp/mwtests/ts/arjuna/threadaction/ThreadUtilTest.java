/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.threadaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.CoordinatorEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.arjuna.coordinator.CheckedActionFactory;
import com.arjuna.ats.arjuna.utils.ThreadUtil;

public class ThreadUtilTest {

    private boolean allowCheckedActionFactoryOverride;

    @Before
    public void setup() {
        CoordinatorEnvironmentBean coordinatorEnvironmentBean = arjPropertyManager.getCoordinatorEnvironmentBean();
        allowCheckedActionFactoryOverride = coordinatorEnvironmentBean.isAllowCheckedActionFactoryOverride();
    }

    @After
    public void tearDown() {
        CoordinatorEnvironmentBean coordinatorEnvironmentBean = arjPropertyManager.getCoordinatorEnvironmentBean();
        coordinatorEnvironmentBean.setAllowCheckedActionFactoryOverride(allowCheckedActionFactoryOverride);
    }

    @Test
    public void testDisassociateFromDifferentThread() throws InterruptedException {
        Thread thread = Thread.currentThread();

        AtomicBoolean called = new AtomicBoolean(false);

        CoordinatorEnvironmentBean coordinatorEnvironmentBean = arjPropertyManager.getCoordinatorEnvironmentBean();
        coordinatorEnvironmentBean.setAllowCheckedActionFactoryOverride(true);
        coordinatorEnvironmentBean.setCheckedActionFactory(new CheckedActionFactory() {

            @Override
            public CheckedAction getCheckedAction(Uid txId, String actionType) {
                return new CheckedAction() {
                    public void check(boolean isCommit, Uid actUid, Hashtable list) {
                        called.set(true);
                    }
                };
            }
        });
        AtomicAction tx = new AtomicAction();
        assertFalse(tx.removeChildThread(ThreadUtil.getThreadId(thread)));
        tx.begin();

        synchronized (tx) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    assertTrue(tx.removeChildThread(ThreadUtil.getThreadId(thread)));
                    assertTrue(tx.end(true) == ActionStatus.COMMITTED);
                    synchronized (tx) {
                        tx.notify();
                    }
                }
            }).start();
            tx.wait();
        }

        assertTrue(called.get() == false);
    }
}