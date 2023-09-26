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
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.CacheStore;

class ThreadWriter extends Thread
{
    private static final String TYPE = "test";

    public ThreadWriter(ParticipantStore theStore)
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
                Thread.yield();

                InputObjectState s = participantStore.read_committed(u, TYPE);

                Thread.yield();
                
                if (s != null) {
                    if (participantStore.remove_committed(u, TYPE)) {
                        passed = true;
                    } else {
                        System.err.println("Could not remove "+u);

                    }
                }
                else
                    System.err.println("No state found while trying to read "+u);
            } else {
                System.err.println("Could not write "+u);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        System.err.println("Thread for "+u+" "+((passed) ? "succeeded" : "failed"));
    }

    public boolean passed = false;

    private ParticipantStore participantStore = null;
}


public class CachedTest
{
    @Test
    public void test() throws Exception
    {
        int cacheSize = 2048;
        int threads = 1000;
        Thread[] t = new Thread[threads];

        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = arjPropertyManager.getObjectStoreEnvironmentBean();

        objectStoreEnvironmentBean.setCacheStoreSize(cacheSize);

        ParticipantStore store = new CacheStore(objectStoreEnvironmentBean);
        
        long stime = Calendar.getInstance().getTime().getTime();

        for (int i = 0; i < threads; i++) {
            System.err.println("i: "+i);
            t[i] = new ThreadWriter(store);
            t[i].start();
        }

        boolean passed = true;
        for (int j = 0; j < threads; j++) {
            System.err.println("j: "+j);
            t[j].join();
            passed = passed && ((ThreadWriter) t[j]).passed;
        }

        long ftime = Calendar.getInstance().getTime().getTime();
        long timeTaken = ftime - stime;

        store.sync();
        
        
        assertTrue(passed);

        System.err.println("time for " + threads + " users is " + timeTaken);
    }

}