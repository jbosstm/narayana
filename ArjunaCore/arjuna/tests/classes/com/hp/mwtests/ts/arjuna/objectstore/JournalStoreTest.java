/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqJournalEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.hornetq.HornetqObjectStoreAdaptor;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class JournalStoreTest {
    private static HornetqJournalEnvironmentBean hornetqJournalEnvironmentBean;
    private final String typeName = "/StateManager/junit";

    @BeforeClass
    public static void before() {
        BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class).
                setObjectStoreType(HornetqObjectStoreAdaptor.class.getName());

        hornetqJournalEnvironmentBean = BeanPopulator.getDefaultInstance(HornetqJournalEnvironmentBean.class);
    }

    @AfterClass
    public static void after() {
        hornetqJournalEnvironmentBean.setMaxIO(2);
        hornetqJournalEnvironmentBean.setAsyncIO(false);
    }

    @Test
    public void test1() throws ObjectStoreException {
        // remark to test AIO on linux you must set LD_LIBRARY_PATH (setting it via -Djava.library.path is ignored)
        hornetqJournalEnvironmentBean.setAsyncIO(true);
        hornetqJournalEnvironmentBean.setMaxIO(2); // should work since on 1 is invalid
        assertEquals(2, hornetqJournalEnvironmentBean.getMaxIO());
        // add a record to the object store
        assertTrue(StoreManager.getRecoveryStore().write_committed(new Uid(), typeName, new OutputObjectState()));
    }

    @Test
    public void test2() throws ObjectStoreException {
        // remark to test AIO on linux you must set LD_LIBRARY_PATH (setting it via -Djava.library.path is ignored)
        hornetqJournalEnvironmentBean.setAsyncIO(true);
        hornetqJournalEnvironmentBean.setMaxIO(1); // should not work since 1 is invalid
        assertNotEquals(1, hornetqJournalEnvironmentBean.getMaxIO());
        // add a record to the object store
        assertTrue(StoreManager.getRecoveryStore().write_committed(new Uid(), typeName, new OutputObjectState()));
    }

    @Test
    public void test3() throws ObjectStoreException {
        hornetqJournalEnvironmentBean.setAsyncIO(false);
        hornetqJournalEnvironmentBean.setMaxIO(2); // should work since the restriction only applies to AIO
        assertEquals(2, hornetqJournalEnvironmentBean.getMaxIO());
        // add a record to the object store
        assertTrue(StoreManager.getRecoveryStore().write_committed(new Uid(), typeName, new OutputObjectState()));
    }

    @Test
    public void test4() throws ObjectStoreException {
        hornetqJournalEnvironmentBean.setAsyncIO(false);
        hornetqJournalEnvironmentBean.setMaxIO(1); // should work since the restriction only applies to AIO
        assertEquals(1, hornetqJournalEnvironmentBean.getMaxIO());
        // add a record to the object store
        assertTrue(StoreManager.getRecoveryStore().write_committed(new Uid(), typeName, new OutputObjectState()));
    }
}