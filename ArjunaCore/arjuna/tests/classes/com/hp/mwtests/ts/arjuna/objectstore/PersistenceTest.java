/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.objectstore;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Calendar;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.CacheStore;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore;

public class PersistenceTest
{
    @Test
    public void test()
    {
        boolean passed = false;
        boolean threaded = false;
        long stime = Calendar.getInstance().getTime().getTime();

        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();

        for (int i = 0; i < 1000; i++) {
            try {
                ParticipantStore store = null;

                if (!threaded)
                    store = new ShadowingStore(objectStoreEnvironmentBean);
                else
                    store = new CacheStore(objectStoreEnvironmentBean);

                byte[] data = new byte[10240];
                OutputObjectState state = new OutputObjectState();
                Uid u = new Uid();

                state.packBytes(data);

                if (store.write_committed(u, "/StateManager/LockManager/foo", state)) {
                    passed = true;
                } else
                    passed = false;
            }
            catch (ObjectStoreException e) {
                System.out.println(e.getMessage());

                passed = false;
            }
            catch (IOException ex) {
                ex.printStackTrace();

                passed = false;
            }
        }

        try {
            Thread.currentThread().sleep(1000);
        }
        catch (Exception ex) {
        }

        long ftime = Calendar.getInstance().getTime().getTime();
        long timeTaken = ftime - stime;

        System.out.println("time for 1000 write transactions is " + timeTaken);

        try {
            Thread.currentThread().sleep(1000);
        }
        catch (Exception ex) {
        }

        assertTrue(passed);
    }
}