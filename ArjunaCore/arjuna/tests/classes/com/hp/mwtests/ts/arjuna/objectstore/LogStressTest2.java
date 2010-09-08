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
 * (C) 2005-2009,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Performance2.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.internal.arjuna.objectstore.LogStore;
import com.hp.mwtests.ts.arjuna.resources.BasicRecord;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * Run with the log store for N hours and make sure there are
 * no logs left at the end.
 */

public class LogStressTest2
{
    @Test
    public void test()
    {
        arjPropertyManager.getCoordinatorEnvironmentBean().setCommitOnePhase(false);
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(LogStore.class.getName());
        arjPropertyManager.getObjectStoreEnvironmentBean().setTxLogSize(10000);

        int timeLimit = 4; // hours

        System.err.println("WARNING: this test will run for " + timeLimit + " hours.");

        final long stime = System.currentTimeMillis();
        final long endTime = timeLimit * 60 * 60 * 1000;
        long ftime;

        do {
            try {
                AtomicAction A = new AtomicAction();

                A.begin();

                A.add(new BasicRecord());

                A.commit();
            }
            catch (final Exception ex) {
            }

            ftime = System.currentTimeMillis();

        } while ((ftime - stime) < endTime);
    }
}
