/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.Utils;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;

/**
 * Utility class to hold delay functions for crash recovery tests.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-09
 */
public class CrashRecoveryDelays
{
    public static void awaitRecoveryCR07(int num_clients) throws InterruptedException {
        awaitRecovery(1, num_clients);
    }

    public static void awaitRecoveryCR08(int num_clients) throws InterruptedException {
        // due to the way CachedRecoveredTransaction/StatusChecker handle TRANSIENT some of these tests need two passes
        awaitRecovery(2, num_clients);
    }

    public static void awaitRecoveryCR09() throws InterruptedException {
        awaitRecovery(1, 1);
    }

    public static void awaitRecoveryCR10() throws InterruptedException {
        awaitRecovery(1, 1);
    }

    private static void awaitRecovery(int num_cycles, int num_clients) throws InterruptedException
    {
        // Note: this assumes the client is running with the same config as the rec mgr process.
        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();
        int recoveryCycleTime = recoveryEnvironmentBean.getPeriodicRecoveryPeriod()+recoveryEnvironmentBean.getRecoveryBackoffPeriod();
        // this timing may be a little tight on some older/busy systems
        // consider increasing the per-client fudge factor a bit...
        int delay = ((num_cycles*recoveryCycleTime)+(num_clients*10))*1000;
        System.out.println("Sleeping for " + delay + " ms.");
        Thread.sleep(delay);
    }

    /////////////////

    public static void awaitReplayCompletionCR01() throws InterruptedException {
        awaitReplayCompletion(5); // was 10
    }

    public static void awaitReplayCompletionCR04() throws InterruptedException {
        awaitReplayCompletion(5); // was 10
    }

    public static void awaitReplayCompletionCR02() throws InterruptedException {
        awaitReplayCompletion(5); // was 60
    }

    public static void awaitReplayCompletionCR05() throws InterruptedException {
        awaitReplayCompletion(5); // was 60
    }

    private static void awaitReplayCompletion(int seconds) throws InterruptedException
    {
        Thread.sleep(seconds * 1000);
    }
}
