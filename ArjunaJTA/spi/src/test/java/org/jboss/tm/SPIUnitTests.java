/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Inc., and individual contributors as indicated
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
package org.jboss.tm;

import org.junit.Assert;
import org.junit.Test;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;


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
