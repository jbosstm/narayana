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

import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class JotmWorkerTask extends WorkerTask {
    protected JotmWorkerTask(CyclicBarrier cyclicBarrier, AtomicInteger count, int batch_size) {
        super(cyclicBarrier, count, batch_size);
        try {
            jotm = new org.objectweb.jotm.Jotm(true, false);
        } catch (NamingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected void fini() {
        super.fini();
        org.objectweb.jotm.TimerManager.stop();
        if (jotm != null)
            jotm.stop();
        jotm = null;
    }

    @Override
    protected TransactionManager getTransactionManager() {
        return jotm.getTransactionManager();
    }

    private org.objectweb.jotm.Jotm jotm;
}
