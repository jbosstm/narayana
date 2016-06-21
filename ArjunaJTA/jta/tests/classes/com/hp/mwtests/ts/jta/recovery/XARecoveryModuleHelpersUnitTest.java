/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * @author Red Hat, Inc
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
import java.util.concurrent.Callable;
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
    @org.junit.Ignore("j9 TODO ")
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

        Future<String> future = pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final XAResourceRecoveryHelper xaResourceRecoveryHelper = new XAResourceRecoveryHelper() {
                    @Override
                    public boolean initialise(String p) throws Exception { return true; }

                    @Override
                    public XAResource[] getXAResources() throws Exception { return new XAResource[0]; }
                };

                return addHelper(xaRecoveryModule, xaResourceRecoveryHelper, 3);
            }
        });

        String errMsg = future.get();
        assertNull(errMsg, errMsg);
    }

    private String addHelper(XARecoveryModule xaRecoveryModule, XAResourceRecoveryHelper xaResourceRecoveryHelper, int expectedState) {
        if (getScanState(xaRecoveryModule) != expectedState)
            return "Wrong state for addHelper in pass 2a";

        xaRecoveryModule.addXAResourceRecoveryHelper(xaResourceRecoveryHelper);

        if (getScanState(xaRecoveryModule) != expectedState)
            return "Wrong state for addHelper in pass 2b";

        xaRecoveryModule.removeXAResourceRecoveryHelper(xaResourceRecoveryHelper);

        if (getScanState(xaRecoveryModule) != expectedState)
            return "Wrong state for addHelper in pass 2c";

        return null;
    }

    private void testTimelyXAResourceRecoveryHelperRemoval(final boolean somethingToRecover) throws Exception {
        final XARecoveryModule xaRecoveryModule = new XARecoveryModule();
        final long xAResourcesSleepMillis = 2000;
        final long betweenPassesSleepMillis = 100;
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
        remover.start();
        remover2.start();

        System.out.printf("Before pass 1 (%d)%n", System.currentTimeMillis() - millis);
        xaRecoveryModule.periodicWorkFirstPass();
        System.out.printf("Finished pass 1 (%d)%n", System.currentTimeMillis() - millis);
        Thread.sleep(betweenPassesSleepMillis);
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
