/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package org.narayana.tools.perf;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

public abstract class WorkerTask implements Runnable {
    protected CyclicBarrier cyclicBarrier;
    protected AtomicInteger count;
    protected AtomicInteger rid = new AtomicInteger(0);
    protected int batch_size = 0;
	protected Map<String, XAResource> resources = new HashMap<String, XAResource> ();

    protected WorkerTask(CyclicBarrier cyclicBarrier, AtomicInteger count, int batch_size) {
        String name = Thread.currentThread().getName() + rid.incrementAndGet();
        this.cyclicBarrier = cyclicBarrier;
        this.count = count;
        this.batch_size = batch_size;

        resources.put(name, new XAResourceImpl(name, 0));
        name = Thread.currentThread().getName() + rid.incrementAndGet();
        resources.put(name, new XAResourceImpl(name, 0));
    }

    protected void init() {
    }

    protected void fini() {
    }

    protected void registerResource(String name, XAResource res) {

    }

    protected void unregisterResource(String name, XAResource res) {

    }

    protected void registerResources() {
        for (Map.Entry<String, XAResource> e : resources.entrySet())
            registerResource(e.getKey(), e.getValue());
    }

    protected void unregisterResources() {
        for (Map.Entry<String, XAResource> e : resources.entrySet())
            unregisterResource(e.getKey(), e.getValue());
    }

	protected void doTx(TransactionManager tm) throws Exception {
		tm.begin();
		Transaction t = tm.getTransaction();

		for (XAResource xar : resources.values())
			t.enlistResource(xar);

		tm.commit();
	}
 
	protected abstract TransactionManager getTransactionManager();
//		return new DummyTransactionManager();

    public void run() {
		TransactionManager tm = getTransactionManager();

        if (tm == null)
            throw new RuntimeException("No suitable transaction manager for " + this.getClass().getName());

        try {
            registerResources();

            cyclicBarrier.await();

            while(count.decrementAndGet() >= 0) {
                for(int i = 0; i < batch_size; i++)
                    doTx(tm);
            }

            cyclicBarrier.await();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            unregisterResources();
        }
    }
}
