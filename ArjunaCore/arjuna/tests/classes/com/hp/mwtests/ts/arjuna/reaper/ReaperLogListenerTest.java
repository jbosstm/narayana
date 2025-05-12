/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.listener.ReaperMonitor;
import com.arjuna.ats.arjuna.logging.ArjunaLoggerInterceptor;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReaperLogListenerTest
{
    private LoggerInterceptor logListener;
    private final String ARJUNA_LOGGER_PROJECT_CODE = "ARJUNA"; // see arjunaI18NLogger
    private final String REAPER_MESSAGE_ID = "ARJUNA012418"; // TransactionReaper Installing logger redirect filter

    public class DummyMonitor implements ReaperMonitor
    {
        public synchronized void rolledBack (Uid txId)
        {
            success = true;
            notify();
            notified = true;
        }
        
        public synchronized void markedRollbackOnly (Uid txId)
        {
            success = false;
            notify();
            notified = true;
        }

        public boolean success = false;
        public boolean notified = false;

        public synchronized boolean checkSucceeded(int msecsTimeout)
        {
            if (!notified) {
                try {
                    wait(msecsTimeout);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            return success;
        }
    }

    @Before
    public void setup() {
        // set the logger interceptor filter (TransactionReaper will use it if it is valid)
        arjPropertyManager.getCoordinatorEnvironmentBean().setLoggerInterceptor(LoggerInterceptor.class.getCanonicalName());
        // get the interceptor (the CoordinatorEnvironmentBean will have instantiated it)
        ArjunaLoggerInterceptor listener = arjPropertyManager.getCoordinatorEnvironmentBean().getLoggerInterceptor();

        assertTrue(listener instanceof ArjunaLoggerInterceptor);

        logListener = (LoggerInterceptor) listener;
    }

    @Test
    public void test()
    {
        // ensure that the reaper generates some log messages at level WARN:
        TransactionReaper reaper = TransactionReaper.transactionReaper();
        DummyMonitor reaperMonitor = new DummyMonitor();
       
        reaper.addListener(reaperMonitor);
        
        AtomicAction A = new AtomicAction();

        A.begin();

        reaper.insert(A, 1);

        assertTrue(reaperMonitor.checkSucceeded(30 * 1000));

        assertTrue(reaper.removeListener(reaperMonitor));

        // insert a new transaction with a longer timeout and check that we can find the remaining time

        A = new AtomicAction();

        reaper.insert(A, 1000);

        long remaining = reaper.getRemainingTimeoutMills(A);

        assertTrue(remaining != 0);

        // ok now remove it

        reaper.remove(A);

        // verify that the log message interceptor intercepted the warn and info messages
        // (a test enhancement could be to verify that they do not appear in the logs - see ReaperLogFilter)
        assertFalse(logListener.getInfoMessages().isEmpty());
        assertFalse(logListener.getWarnMessages().isEmpty());
        assertTrue(logListener.getErrorMessages().isEmpty());

        // get the first warning message
        Optional<LoggerInterceptor.LogMessage> infoMsg = logListener.getInfoMessages().stream().findFirst();

        assertFalse(infoMsg.isEmpty());

        // and verify that it contains the correct message code
        assertTrue(logListener.getMessageId(infoMsg.get().message()).startsWith(ARJUNA_LOGGER_PROJECT_CODE));
        assertNotNull(infoMsg.get().instant());
        assertEquals(REAPER_MESSAGE_ID, logListener.getMessageId(infoMsg.get().message()));
    }
}