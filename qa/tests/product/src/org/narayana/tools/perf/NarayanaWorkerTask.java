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

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import javax.transaction.TransactionManager;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class NarayanaWorkerTask extends WorkerTask {
    protected NarayanaWorkerTask(CyclicBarrier cyclicBarrier, AtomicInteger count, int batch_size) {
        super(cyclicBarrier, count, batch_size);
    }

    protected void init() {
        String objectStoreBaseDirBaseName = System.getProperty("ObjectStoreBaseDir", "logs");
        File directory = new File(objectStoreBaseDirBaseName);
        File hornetqStoreDir = new File(directory, "HornetQStore");

        try {
            BeanPopulator.getDefaultInstance(HornetqJournalEnvironmentBean.class)
                    .setStoreDir(hornetqStoreDir.getCanonicalPath());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                .setObjectStoreType("com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqObjectStoreAdaptor");
    }

    @Override
    protected void fini() {
        super.fini();
        StoreManager.shutdown();
    }

    @Override
    protected TransactionManager getTransactionManager() {
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }
}
