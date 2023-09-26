/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.objectstore;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.CacheStore;

class WriterThread extends Thread
{
    private static final String TYPE = "test";

    public WriterThread(ParticipantStore theStore)
    {
        participantStore = theStore;
    }

    public void run()
    {
        byte[] data = new byte[1024];
        OutputObjectState state = new OutputObjectState(new Uid(), "type");
        Uid u = new Uid();

        try {
            state.packBytes(data);

            if (participantStore.write_committed(u, TYPE, state)) {
                InputObjectState s = participantStore.read_committed(u, TYPE);

                if (s != null)
                    passed = true;
                else
                    System.err.println("Could not read state.");
            } else
                System.err.println("Could not write state.");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean passed = false;

    private ParticipantStore participantStore = null;
}


public class WriteCachedTest
{
    @Test
    public void test() throws ObjectStoreException
    {
        boolean passed = true;
        String cacheSize = "20480";
        int threads = 10;

        Thread[] t = new Thread[threads];

        System.setProperty("com.arjuna.ats.internal.arjuna.objectstore.cacheStore.size", cacheSize);

        ParticipantStore store = new CacheStore(new ObjectStoreEnvironmentBean());
        long stime = Calendar.getInstance().getTime().getTime();

        for (int i = 0; (i < threads) && passed; i++) {
            try {
                t[i] = new WriterThread(store);
                t[i].start();
            }
            catch (Exception ex) {
                ex.printStackTrace();

                passed = false;
            }
        }

        for (int j = 0; j < threads; j++) {
            try {
                t[j].join();

                passed = passed && ((WriterThread) t[j]).passed;
            }
            catch (Exception ex) {
            }
        }

        long ftime = Calendar.getInstance().getTime().getTime();
        long timeTaken = ftime - stime;

        System.out.println("time for " + threads + " users is " + timeTaken);

        try {
            store.sync();
        }
        catch (Exception ex) {
        }

        assertTrue(passed);
    }

}