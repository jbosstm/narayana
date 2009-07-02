/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2007,
 * @author JBoss, a division of Red Hat.
 */
package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.coordinator.ReaperElement;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Exercises some aspects of the TransactionReaper functionality.
 *
 * @author jonathan.halliday@redhat.com, 2007-04-30
 */
public class ReaperTestCase
{
    @Test
    public void testReaper() throws Exception
    {

        // test set+readback of interval
        TransactionReaper.create(100);
        TransactionReaper reaper = TransactionReaper.transactionReaper();
        // set value is ignored in default DYNAMIC mode, it uses max long instead.
        assertEquals(Long.MAX_VALUE, reaper.checkingPeriod());


        Reapable reapable = new MockReapable(new Uid());
        Reapable reapable2 = new MockReapable(new Uid());
        Reapable reapable3 = new MockReapable(new Uid());

        ReaperElement reaperElement = new ReaperElement(reapable, 30);
        ReaperElement reaperElement2 = new ReaperElement(reapable2, 20);
        ReaperElement reaperElement3 = new ReaperElement(reapable3, 10);

        // test that ordering is by timeout, regardless of insertion order
        SortedSet sortedSet = new TreeSet();
        sortedSet.add(reaperElement);
        sortedSet.add(reaperElement3);
        sortedSet.add(reaperElement2);

        assertEquals(sortedSet.first(), reaperElement3);
        assertEquals(sortedSet.last(), reaperElement);

        // test insertion of timeout=0 is a nullop
        assertTrue(reaper.insert(reapable, 0));
        assertEquals(0, reaper.numberOfTransactions());
        assertEquals(0, reaper.numberOfTimeouts());
        assertFalse(reaper.remove(reapable));

        // test that duplicate insertion fails
        assertTrue(reaper.insert(reapable, 10));
        assertFalse(reaper.insert(reapable, 10));
        assertEquals(1, reaper.numberOfTransactions());
        assertEquals(1, reaper.numberOfTimeouts());
        assertTrue(reaper.remove(reapable));
        assertEquals(0, reaper.numberOfTransactions());
        assertEquals(0, reaper.numberOfTimeouts());

        // test that timeout change fails
        assertTrue(reaper.insert(reapable, 10));
        assertFalse(reaper.insert(reapable, 20));
        assertEquals(1, reaper.numberOfTransactions());
        assertEquals(1, reaper.numberOfTimeouts());
        assertEquals(10, reaper.getTimeout(reapable));
        assertTrue(reaper.remove(reapable));
        assertEquals(0, reaper.numberOfTransactions());
        assertEquals(0, reaper.numberOfTimeouts());

        // test reaping
        reaper.insert(reapable, 1); // seconds
        reaper.insert(reapable2, 5);
        assertEquals(2, reaper.numberOfTransactions());
        assertEquals(2, reaper.numberOfTimeouts());
        reaper.check();
        assertEquals(2, reaper.numberOfTransactions());
        Thread.sleep(2 * 1000);
        reaper.check();
        assertEquals(1, reaper.numberOfTransactions());
        assertEquals(1, reaper.numberOfTimeouts());
        Thread.sleep(4 * 1000);
        reaper.check();
        assertEquals(0, reaper.numberOfTransactions());
        assertEquals(0, reaper.numberOfTimeouts());

    }

    public class MockReapable implements Reapable
    {
        public MockReapable(Uid uid)
        {
            this.uid = uid;
        }

        public boolean running()
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean preventCommit()
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public int cancel()
        {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Uid get_uid()
        {
            return uid;
        }

        private Uid uid;
    }
}
