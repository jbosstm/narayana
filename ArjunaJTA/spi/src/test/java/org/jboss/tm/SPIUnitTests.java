/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.tm;

import org.junit.Assert;
import org.junit.Test;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SPIUnitTests {
    @Test
    public void testResourceAnnotations() throws Exception {
        TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

        transactionManager.begin();

        Transaction txn = transactionManager.getTransaction();
        List<TestResource> resources = new ArrayList<TestResource>();
        List<TestResource> prepareOrder = new CopyOnWriteArrayList<TestResource>();
        List<TestResource> commitOrder = new CopyOnWriteArrayList<TestResource>();

        resources.add(new LastTestResource(prepareOrder, commitOrder));
        resources.add(new TestResource(prepareOrder, commitOrder));
        resources.add(new FirstTestResource(prepareOrder, commitOrder));

        for (TestResource resource : resources)
            txn.enlistResource(resource);

        transactionManager.commit();

        // verify that the resources were prepared and committed in the correct order
        Assert.assertEquals(resources.get(2), prepareOrder.get(0));
        Assert.assertEquals(resources.get(1), prepareOrder.get(1));
        Assert.assertEquals(resources.get(0), prepareOrder.get(2));

        Assert.assertEquals(resources.get(2), commitOrder.get(0));
        Assert.assertEquals(resources.get(1), commitOrder.get(1));
        Assert.assertEquals(resources.get(0), commitOrder.get(2));
    }
}