/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: CachedTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.common.*;

import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

class ThreadWriter extends Thread
{
    private static final String TYPE = "test";

    public ThreadWriter(ObjectStore theStore)
    {
        store = theStore;
    }

    public void run()
    {
        byte[] data = new byte[1024];
        OutputObjectState state = new OutputObjectState(new Uid(), "type");
        Uid u = new Uid();

        try {
            state.packBytes(data);

            if (store.write_committed(u, TYPE, state)) {
                Thread.yield();

                InputObjectState s = store.read_committed(u, TYPE);

                Thread.yield();
                
                if (s != null) {
                    if (store.remove_committed(u, TYPE))
                        passed = true;
                }
                else
                    System.err.println("No state found while trying to read "+u);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        System.err.println("Thread for "+u+" "+((passed) ? "succeeded" : "failed"));
    }

    public boolean passed = false;

    private ObjectStore store = null;
}


public class CachedTest
{
    @Test
    public void test() throws Exception
    {
        String cacheSize = "2048";
        int threads = 100;
        Thread[] t = new Thread[threads];

        System.setProperty("com.arjuna.ats.internal.arjuna.objectstore.cacheStore.size", cacheSize);

        ObjectStore store = new ObjectStore(ArjunaNames.Implementation_ObjectStore_CacheStore());

        long stime = Calendar.getInstance().getTime().getTime();

        for (int i = 0; i < threads; i++) {
            System.err.println("i: "+i);
            t[i] = new ThreadWriter(store);
            t[i].start();
        }

        for (int j = 0; j < threads; j++) {
            System.err.println("j: "+j);
            t[j].join();
            assertTrue(((ThreadWriter) t[j]).passed);
        }

        long ftime = Calendar.getInstance().getTime().getTime();
        long timeTaken = ftime - stime;

        store.sync();

        System.err.println("time for " + threads + " users is " + timeTaken);
    }

}
