/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
            List<Thread> threads = new ArrayList<Thread>();
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
