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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PersistenceTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.common.*;

import java.io.*;

import java.util.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import org.junit.Test;
import static org.junit.Assert.*;

public class PersistenceTest
{
    @Test
    public void test()
    {
        boolean passed = false;
        boolean threaded = false;
        long stime = Calendar.getInstance().getTime().getTime();

        for (int i = 0; i < 1000; i++) {
            try {
                ObjectStore store = null;

                if (!threaded)
                    store = new ObjectStore(ArjunaNames.Implementation_ObjectStore_ShadowingStore());
                else
                    store = new ObjectStore(ArjunaNames.Implementation_ObjectStore_CacheStore());

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
