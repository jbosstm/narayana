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
package org.jboss.narayana.jta.quickstarts;

import java.io.File;

import javax.transaction.UserTransaction;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class HornetqStoreExample {
    private static final String storeClassName =  com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqObjectStoreAdaptor.class.getName();
    private static final String storeDir = "target/HornetqStore";

    public static void main(String[] args) throws Exception {
        setupStore();
        UserTransaction utx = com.arjuna.ats.jta.UserTransaction.userTransaction();

        utx.begin();
        utx.commit();

		shutdownStore();
    }

    public static void setupStore() throws Exception {
        File hornetqStoreDir = new File(storeDir);

        BeanPopulator.getDefaultInstance(HornetqJournalEnvironmentBean.class)
                    .setStoreDir(hornetqStoreDir.getCanonicalPath());

        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).setObjectStoreType(storeClassName);
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreDir(storeDir);

        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "default").setObjectStoreType(storeClassName);
        // TODO figure out why we can't use the hornetqStore as the communications store
        //BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore").setObjectStoreType(storeClassName);
    }

    public static void shutdownStore() throws Exception {
        StoreManager.shutdown();

        if (!new File(storeDir).exists())
            throw new RuntimeException(storeDir + " should have been created");
	}
}
