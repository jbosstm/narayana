/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.distributed.server;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IsolatableServersClassLoaderTest {
    private int failed;
    private int errors;

    @Test
    public void multiThreadedTest() throws NoSuchMethodException, IOException, InterruptedException {
        for (int j = 0; j < 100; j++) {
            IsolatableServersClassLoader loader = new IsolatableServersClassLoader(null, IsolatableServersClassLoaderTest.class.getName(), Thread.currentThread().getContextClassLoader());
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                threads.add(new Thread() {
                    public void run() {
                        try {
                            loader.loadClass("com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager");
                        } catch (ClassNotFoundException e) {
                            failed++;
                        } catch (java.lang.LinkageError e) {
                            errors++;
                        }
                    }
                });
            }
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
        }
        assertTrue("Failures: " + failed, failed == 0);
        assertTrue("Errors: " + errors, errors == 0);
    }
}