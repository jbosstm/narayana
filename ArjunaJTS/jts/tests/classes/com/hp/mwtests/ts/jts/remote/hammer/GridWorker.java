/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
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
 */
package com.hp.mwtests.ts.jts.remote.hammer;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.orbportability.ORB;
import com.hp.mwtests.ts.jts.TestModule.grid;
import com.hp.mwtests.ts.jts.TestModule.gridHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;
import io.narayana.perf.Measurement;
import io.narayana.perf.Worker;
import org.omg.CosTransactions.NoTransaction;

class GridWorker implements Worker {
    ORB myORB;
    String gridReference;
    grid gridVar = null;  // pointer the grid object that will be used.
    CurrentImple current;
    int initialValue = -1;
    int finalValue = -1;
    int newValue = 123;
    int h, w;

    GridWorker(final ORB orb, final String gridReference) throws Exception {
        this.myORB = orb;
        this.gridReference = gridReference;
        this.current = OTSImpleManager.current();

        try {
            gridVar = gridHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(gridReference)));

            h = gridVar.height();
            w = gridVar.width();

        }  catch (Exception sysEx) {
            TestUtility.fail("failed to bind to grid: "+sysEx);
            sysEx.printStackTrace(System.err);
            throw sysEx;
        }
    }

    @Override
    public Object doWork(Object context, int niters, Measurement opts) {
        boolean running = false;
        try {
            for (int i = 0; i < niters; i++) {
                current.begin();
                running = true;
                gridVar.set(2, 4, newValue, current.get_control());
                current.commit(false);
                running = false;
                opts.setInfo("grid[2,4] should be " + initialValue);
            }
        } catch (Exception sysEx) {
            sysEx.printStackTrace(System.err);
            opts.setInfo("work exception " + sysEx.getMessage());
            opts.incrementErrorCount();
            TestUtility.fail(sysEx.toString());
        } finally {
            if (running) {
                opts.incrementErrorCount();
                opts.setInfo("work exception txn should have finished");
                TestUtility.fail("work exception txn should have finished");

                try {
                    current.rollback();
                } catch (NoTransaction noTransaction) {
                    // ignore
                }
            }
        }

        return null;
    }

    @Override
    public void finishWork(Measurement measurement) {
    }

    @Override
    public void init() {
        try {
            current.begin();
            initialValue = gridVar.get(2, 4, current.get_control());
            current.commit(false);
        } catch (Exception e) {
            String declaringClassName = this.getClass().getDeclaringClass().getSimpleName();
            System.err.printf("%s: Exception reading initial value: %s%n", declaringClassName, e.getMessage());
            TestUtility.fail("Exception reading initial value: " + declaringClassName);
        }
    }

    @Override
    public void fini() {
        try {
            current.begin();
            finalValue = gridVar.get(2, 4, current.get_control());
            current.commit(false);
            if (finalValue != newValue) {
                String declaringClassName = this.getClass().getDeclaringClass().getSimpleName();

                TestUtility.fail("final value not equal to target value: " + declaringClassName);
            }
        } catch (Exception e) {
            String declaringClassName = this.getClass().getDeclaringClass().getSimpleName();
            System.err.printf("%s: Exception reading final value: %s%n", declaringClassName, e.getMessage());
            TestUtility.fail("Exception reading final value: " + declaringClassName);
        }
    }

    public int getFinalValue() {
        return finalValue;
    }

    public int getInitialValue() {
        return initialValue;
    }
};
