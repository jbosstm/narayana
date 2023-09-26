/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.recovery;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Assert;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.tools.RecoveryMonitor;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;


/**
 * <p>
 * Test of {@link RecoveryMonitor} which were causing deadlock when a listener was active
 * while the periodic recovery was about to suspend.
 * <p>
 * We have observed this for WildFly<br/>
 * The suspend call is coming from the WildFly <code>RecoverySuspendController</code> which puts <code>PeriodicRecovery</code>
 * in the <code>SUSPENDED</code> state.
 * The shutdown request waits for the recovery listeners to finish but the recovery listeners are waiting for the periodic recovery
 * <code>wakeUp</code> call to complete which never happens because periodic recovery is suspended.
 */
public class RecoveryMonitorDeadlockTest {
    private static Logger log = Logger.getLogger(RecoveryMonitorDeadlockTest.class.getName());

    // timeout for countdown latches to await, when the proper call is not reached till that time
    private static final int AWAIT_TIMEOUT_S = 10;

    @Test
    public void testRecoveryMonitorWithSuccess() throws Exception {
        // countdown announcing the XAResource recovery was started
        final CountDownLatch coundDownXARecoveryStarted = new CountDownLatch(1);
        // countdown on which the XAResource recovery waits, this recovery causes the recover call to await
        final CountDownLatch coundDownDelayXARecovery = new CountDownLatch(1);

        // XAResource which suspends the recover call to be able to run suspend on recovery manager meanwhile
        final XAResource sleepingXA = new XATestResource(XATestResource.OK_JNDI_NAME, false) {
            @Override
            public Xid[] recover(int i) throws XAException {
                coundDownXARecoveryStarted.countDown();
                try {
                    log.info(this + ": Recovering to be delayed before coundown is 'released'");
                    boolean timeoutNotElapsed = coundDownDelayXARecovery.await(10, TimeUnit.SECONDS);
                    Assert.assertTrue("Timeout " + AWAIT_TIMEOUT_S + " seconds elapsed while waiting"
                        + " for the recover() handler being released", timeoutNotElapsed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Assert.fail("Awaiting for the recovery release was interrupted: " + e);
                }
                log.info(this + ": Recovered.");
                return new Xid[0];
            }
        };
        // resource helper to be registered to recovery manager for it to process the XAResource
        final XAResourceRecoveryHelper xaResourceRecoveryHelper = new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String p) throws Exception {
                return true;
            }
            @Override
            public XAResource[] getXAResources() throws Exception {
                return new XAResource[] {sleepingXA};
            }
        };

        RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();
        recoveryEnvironmentBean.setRecoveryBackoffPeriod(1); // use a short interval between passes
        recoveryEnvironmentBean.setRecoveryListener(true); // configure the RecoveryMonitor

        RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
        XARecoveryModule recoveryModule = new XARecoveryModule();
        recoveryModule.addXAResourceRecoveryHelper(xaResourceRecoveryHelper);
        manager.addModule(recoveryModule); // we only need to test the XARecoveryModule
        manager.startRecoveryManagerThread(); // start periodic recovery

        String host = recoveryEnvironmentBean.getRecoveryAddress(); // the recovery listener host
        String rcPort = String.valueOf(recoveryEnvironmentBean.getRecoveryPort()); // the recovery listener port

        // running the recovery scan which will be delayed by sleepingXAResource
        manager.scan(null);
        boolean timeoutNotElapsed = coundDownXARecoveryStarted.await(AWAIT_TIMEOUT_S, TimeUnit.SECONDS);
        Assert.assertTrue("Time to get started XAResource.recover() function elapsed", timeoutNotElapsed);

        // sending client SCAN request while there is still running the recovery process
        // the timeout on socket is defined so small that the call immediately returns here
        // but SCAN invocation is already happening inside of the Narayana
        RecoveryMonitor.main(new String[] {"-verbose", "-port", rcPort, "-host", host, "-timeout", "100"});

        // creating new thread for being able to check if threads are in deadlock or all passes fine
        Callable<String> task = () -> {
            log.fine("Going to suspend recovery manager");
            manager.suspend(false);
            manager.terminate();
            System.out.println("Hey I'm quine nice!");
            return "OK";
        };
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<String> future = executor.submit(task);

        // recovery monitor was launched and socket timeout was received, leaving the recover to finish
        coundDownDelayXARecovery.countDown();

        try {
            Assert.assertEquals("OK", future.get(AWAIT_TIMEOUT_S, TimeUnit.SECONDS));
        } catch (TimeoutException te) {
            Assert.fail("Not possible to suspend recovery manager when a client SCAN request is active."
                + " There is a deadlock. Recommented to generate a threaddump. Cause: " + te + " " + te.getMessage());
        }
    }
}