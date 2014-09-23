/*
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2014
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.basic;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.hp.mwtests.ts.jta.common.TestLog;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SynchronizationOrderTest {
    private static List<String> synchronizationClassNameOrder = null;
    private static TestLog<String> log = new TestLog<>();
    private static TransactionManager tm;
    private static TransactionSynchronizationRegistry tsr;

    @BeforeClass
    public static void testSetup() {
        synchronizationClassNameOrder = new ArrayList<String> ();

        synchronizationClassNameOrder.add(0, TestSynchronization1.class.getName());
        synchronizationClassNameOrder.add(1, com.hp.mwtests.ts.jta.common.Synchronization.class.getName());
        synchronizationClassNameOrder.add(2, InterposedSync1.class.getName());

        BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class).setSynchronizationClassNameOrder(
                synchronizationClassNameOrder);
        tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        tsr = new TransactionSynchronizationRegistryImple();
    }

    private void assertCorrectOrder(List<String> calls, Synchronization ... synchronizations) {
        int i = 0;

        for (Synchronization synchronization : synchronizations) {
            assertEquals("wrong beforeCompletion order for sync " + i,
                    calls.get(i), synchronization.getClass().getName()+'#'+"beforeCompletion");
            i += 1;
        }

        i = calls.size();
        for (Synchronization synchronization : synchronizations) {
            i -= 1;
            assertEquals("wrong afterCompletion order for sync " + i,
                    calls.get(i), synchronization.getClass().getName()+'#'+"afterCompletion");
        }
    }

    private void permuteItemsInList(List<List<Synchronization>> perms, List<Synchronization> items, int k) {
        for (int i = k; i < items.size(); i++){
            Collections.swap(items, i, k);
            permuteItemsInList(perms, items, k+1);
            Collections.swap(items, k, i);
        }

        if (k == items.size() - 1)
            perms.add(new ArrayList<Synchronization> (items));
    }

    private void testSyncOrder(Synchronization ... synchronizations) throws Exception {
        List<List<Synchronization>> perms = new ArrayList<List<Synchronization>> ();

        permuteItemsInList(perms, java.util.Arrays.asList(synchronizations), 0);

        for (List<Synchronization> permutation : perms) {
            log.getEvents().clear();
            System.out.printf("Testing permutation %s%n", Arrays.toString(permutation.toArray()));

            tm.begin();
            Transaction txn = tm.getTransaction();

            for (Synchronization synchronization : permutation)
                txn.registerSynchronization(synchronization);

            tm.commit();

            assertCorrectOrder(log.getEvents(), synchronizations);
        }
    }

    private void testSyncOrder(List<Synchronization> interposedSynchs, Synchronization ... synchronizations) throws Exception {
        List<List<Synchronization>> perms = new ArrayList<List<Synchronization>> ();

        permuteItemsInList(perms, java.util.Arrays.asList(synchronizations), 0);

        for (List<Synchronization> permutation : perms) {
            log.getEvents().clear();
            System.out.printf("Testing permutation %s%n", Arrays.toString(permutation.toArray()));

            tm.begin();
            Transaction txn = tm.getTransaction();

            for (Synchronization synchronization : permutation)
                if (interposedSynchs.contains(synchronization))
                    tsr.registerInterposedSynchronization(synchronization);
                else
                    txn.registerSynchronization(synchronization);

            tm.commit();

            assertCorrectOrder(log.getEvents(), synchronizations);
        }
    }

    @Test
    public void testSynchronizationOrder() throws Exception {
        TestSynchronization1 firstSync = new TestSynchronization1(log); // occurs first in synchronizationClassNameOrder
        com.hp.mwtests.ts.jta.common.Synchronization secondSync =
                new com.hp.mwtests.ts.jta.common.Synchronization(log); // occurs second in synchronizationClassNameOrder
        TestSynchronization2 thirdSync = new TestSynchronization2(log); // does not occur in synchronizationClassNameOrder

        testSyncOrder(thirdSync, secondSync, firstSync);
    }

    @Test
    public void testTSR() throws Exception {
        TestSynchronization1 firstSync = new TestSynchronization1(log); // occurs first in synchronizationClassNameOrder
        com.hp.mwtests.ts.jta.common.Synchronization secondSync =
                new com.hp.mwtests.ts.jta.common.Synchronization(log); // occurs second in synchronizationClassNameOrder
        TestSynchronization2 thirdSync = new TestSynchronization2(log); // does not occur in synchronizationClassNameOrder
        InterposedSync1 firstISync = new InterposedSync1((log)); // occurs first in the interposed sync ordering (so should be called before other interposed synchs)
        InterposedSync2 secondISync = new InterposedSync2((log)); // does not occur in synchronizationClassNameOrder (but must be called before normal synchs)
        List<Synchronization> isynchs = new ArrayList<Synchronization>();

        isynchs.add(firstISync);
        isynchs.add(secondISync);
        testSyncOrder(isynchs, thirdSync, secondSync, firstSync, secondISync, firstISync);
    }
}

class TestSynchronization1 implements Synchronization {
    TestLog log;

    TestSynchronization1(TestLog log) {
        this.log = log;
    }

    @Override
    public void beforeCompletion() {
        log.add(getClass().getName() + '#' + "beforeCompletion");
    }

    @Override
    public void afterCompletion(int i) {
        log.add(getClass().getName() + '#' + "afterCompletion");
    }
}
class TestSynchronization2 extends TestSynchronization1 {
    TestSynchronization2(TestLog log) { super(log); }
}

class InterposedSync1 extends TestSynchronization1 {
    InterposedSync1(TestLog log) { super(log); }
}

class InterposedSync2 extends TestSynchronization1 {
    InterposedSync2(TestLog log) { super(log); }
}