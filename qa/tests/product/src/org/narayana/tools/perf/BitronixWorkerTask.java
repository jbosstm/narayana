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

import bitronix.tm.BitronixTransactionManager;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

class BitronixWorkerTask extends WorkerTask {

    protected BitronixWorkerTask(CyclicBarrier cyclicBarrier, AtomicInteger count, int batch_size) {
        super(cyclicBarrier, count, batch_size);
    }

    @Override
    protected TransactionManager getTransactionManager() {
        return new BitronixTransactionManager();
    }

    protected void registerResource(String name, XAResource res) {
        bitronix.tm.resource.ehcache.EhCacheXAResourceProducer.registerXAResource(name, res);
    }

    protected void unregisterResource(String name, XAResource res) {
        bitronix.tm.resource.ehcache.EhCacheXAResourceProducer.unregisterXAResource(name, res);
    }

    public static void main(String[] args) {
        XAResource res = new XAResourceImpl("x", 0);
        bitronix.tm.resource.ehcache.EhCacheXAResourceProducer.registerXAResource("rm1", res);
    }
}
