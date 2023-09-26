/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

@RunWith(BMUnitRunner.class)
public class XARecoveryModuleHelpersUnitTest
{
    /**
     * Test that recovery helpers that are not in use can be removed after pass 1
     * @throws Exception
     */
    @Test
    public void testTimelyXAResourceRecoveryHelperRemoval1() throws Exception {
        testTimelyXAResourceRecoveryHelperRemoval(false);
    }

    /**
     * Test that recovery helpers that are in use can only be removed after pass 2 completes
     * @throws Exception
     */
    @Test
    public void testTimelyXAResourceRecoveryHelperRemoval2() throws Exception {
        testTimelyXAResourceRecoveryHelperRemoval(true);
    }

    /**
     * Test that recovery helpers can be added and removed during recovery pass 2
     * (and not have to wait until pass 2 is complete)
     * @throws Exception
     */
    @BMScript("recovery-helper")
    @Test
    public void testTimelyXAResourceRecoveryHelperRemoval3() throws Exception {
        final XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        ExecutorService pool = Executors.newFixedThreadPool(1);

        new Thread() {
            public void run() {
                xaRecoveryModule.periodicWorkFirstPass();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                xaRecoveryModule.periodicWorkSecondPass();
            }
        }.start();

        Future<?> future = pool.submit(new Runnable() {
            @Override
            public void run() {
                final XAResourceRecoveryHelper xaResourceRecoveryHelper = new XAResourceRecoveryHelper() {
                    @Override
                    public boolean initialise(String p) throws Exception { return true; }

                    @Override
                    public XAResource[] getXAResources() throws Exception { return new XAResource[0]; }
                };

                addAndAssertTimelyXAResourceRecoveryHelperRemoval3(xaResourceRecoveryHelper, xaRecoveryModule);
            }
        });

        // waiting for the thread to be finished
        future.get();
    }

    // this method is used in the BMUnit 'recovery-helper' script
    private void addAndAssertTimelyXAResourceRecoveryHelperRemoval3(XAResourceRecoveryHelper xaResourceRecoveryHelper, XARecoveryModule xaRecoveryModule) {
        assertEquals(String.format("Before adding recovery helper '%s' to module '%s' is expected the state to be 'SECOND_PASS'",
                xaResourceRecoveryHelper, xaRecoveryModule), 3, getScanState(xaRecoveryModule));

        xaRecoveryModule.addXAResourceRecoveryHelper(xaResourceRecoveryHelper);

        assertEquals(String.format("After adding recovery helper '%s' to module '%s' is expected the state to be 'SECOND_PASS'",
                xaResourceRecoveryHelper, xaRecoveryModule),
                3, getScanState(xaRecoveryModule));

        xaRecoveryModule.removeXAResourceRecoveryHelper(xaResourceRecoveryHelper);

        assertEquals(String.format("After removing recovery helper '%s' to module '%s' is expected the state still to be 'IDLE'",
                xaResourceRecoveryHelper, xaRecoveryModule),
                0, getScanState(xaRecoveryModule));
    }

    private void testTimelyXAResourceRecoveryHelperRemoval(final boolean somethingToRecover) throws Exception {
        final XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        final long xAResourcesSleepMillis = 500;
        final long millis = System.currentTimeMillis();

        final SimpleResource testXAResource = new SimpleResource() {
            @Override
            public Xid[] recover(int i) throws XAException {
                if (!somethingToRecover)
                    return new Xid[0];

                return new Xid[] {new Xid() {
                    @Override
                    public int getFormatId() { return 0; }

                    @Override
                    public byte[] getGlobalTransactionId() { return new byte[0]; }

                    @Override
                    public byte[] getBranchQualifier() { return new byte[0]; }
                }};
            }
        };

        final XAResourceRecoveryHelper xaResourceRecoveryHelper = new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String p) throws Exception {
                return true;
            }

            @Override
            public XAResource[] getXAResources() throws Exception {
                System.out.printf("getXAResources sleep (%d)%n",System.currentTimeMillis() - millis);
                Thread.sleep(xAResourcesSleepMillis);
                System.out.printf("getXAResources return (%d)%n",System.currentTimeMillis() - millis);
                return new XAResource[] {testXAResource};
            }
        };

        final XAResourceRecoveryHelper xaResourceRecoveryHelper2 = new XAResourceRecoveryHelper() {
            @Override
            public boolean initialise(String p) throws Exception { return true; }

            @Override
            public XAResource[] getXAResources() throws Exception { return new XAResource[0]; }
        };

        /*
         * Remove helpers in the background whilst recovery is running to check that they are removed when
         * the scanner is in the correct state.
         * The sleep time param is tuned to ensure that the add/remove recovery helper operation occurs
         * during pass 1.
         */
        XAHelperRemover remover = new XAHelperRemover(
                xaResourceRecoveryHelper, xaRecoveryModule, xAResourcesSleepMillis / 2, false);
        XAHelperRemover remover2 = new XAHelperRemover(
                xaResourceRecoveryHelper2, xaRecoveryModule, xAResourcesSleepMillis / 2, true);

        xaRecoveryModule.addXAResourceRecoveryHelper(xaResourceRecoveryHelper);

        System.out.printf("Before pass 1 (%d)%n", System.currentTimeMillis() - millis);
        xaRecoveryModule.periodicWorkFirstPass();

        remover.start();
        remover2.start();

        System.out.printf("Finished pass 1 (%d)%n", System.currentTimeMillis() - millis);
        Thread.sleep(xAResourcesSleepMillis); // See https://issues.jboss.org/browse/JBTM-2717
        System.out.printf("Starting pass 2 (%d)%n", System.currentTimeMillis() - millis);
        xaRecoveryModule.periodicWorkSecondPass();

        System.out.printf("Finished pass 2 (%d)%n", System.currentTimeMillis() - millis);

        // wait for helper removal threads to finish 
        try {
            remover.join();
            remover2.join();
        } catch (InterruptedException e) {
            fail("Test was interrupted whilst waiting for xa resource helper threads: " + e.getMessage());
        }

        if (somethingToRecover) {
            assertEquals("helper removed in wrong state", 0, remover.getRemoveState());
        } else {
            // See https://issues.jboss.org/browse/JBTM-2717
            assertEquals("helper removed in wrong state", 2, remover.getRemoveState());
        }

        // the unused helper should have been removed after pass 1
        assertEquals("helper2 removed in wrong state", 2, remover2.getRemoveState());
    }

    private int getScanState(Object instance) {
        try {
            Field scanState = XARecoveryModule.class.getDeclaredField("scanState");
            scanState.setAccessible(true);

            AtomicInteger status = (AtomicInteger) scanState.get(instance);
            return status.get();
        } catch (Exception e) {
            System.out.printf("getScanState error %s%n", e.getMessage());
            return -1;
        }
    }

    private class XAHelperRemover extends Thread {
        private XAResourceRecoveryHelper helper;
        private XARecoveryModule xaRecoveryModule;
        private long xAResourcesSleepMillis;
        private int removeState = -1;
        private boolean add;

        private int getRemoveState() {
            return removeState;
        }

        private XAHelperRemover(XAResourceRecoveryHelper helper,
                                XARecoveryModule xaRecoveryModule,
                                long xAResourcesSleepMillis, boolean add) {
            this.helper = helper;
            this.xaRecoveryModule = xaRecoveryModule;
            this.xAResourcesSleepMillis = xAResourcesSleepMillis;
            this.add = add;
        }

        public void run() {
            long millis = System.currentTimeMillis();

            try {
                Thread.sleep(xAResourcesSleepMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.printf("removing helper (%d)%n", System.currentTimeMillis() - millis);

            if (add)
                xaRecoveryModule.addXAResourceRecoveryHelper(helper);

            xaRecoveryModule.removeXAResourceRecoveryHelper(helper);

            removeState = getScanState(xaRecoveryModule);

            System.out.printf("remove helper took %d millis%n", System.currentTimeMillis() - millis);
        }
    }
}