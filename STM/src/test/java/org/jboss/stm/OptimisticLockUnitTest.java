/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.stm;

import junit.framework.TestCase;

import org.jboss.stm.annotations.Optimistic;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.WriteLock;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

/*
 * Provided by Tom Jenkinson.
 * 
 * Modified by nmcl.
 */

public class OptimisticLockUnitTest extends TestCase
{
    @Transactional
    @Optimistic
    public interface Atomic
    {
        public int get();

        public void set(int value);
    }

    public class ExampleSTM implements Atomic
    {
        @ReadLock
        public int get() {
            return state;
        }

        @WriteLock
        public void set(int value)
        {
            state = value;
        }

        private int state;
    }

    public void testConflict() throws Exception
    {
        Container<Atomic> theContainer1 = new Container<Atomic>();
        Container<Atomic> theContainer2 = new Container<Atomic>();

        final Atomic obj1 = theContainer1.create(new ExampleSTM());
        
        AtomicAction act = new AtomicAction();
        
        /*
         * Make sure there's state on "disk" before we try to do anything
         * with a shared instance.
         * 
         * https://issues.jboss.org/browse/JBTM-1732
         */
        
        act.begin();
        
        obj1.set(10);
        
        act.commit();
        
        final Atomic obj2 = theContainer2.clone(new ExampleSTM(), obj1);

        AtomicAction a = new AtomicAction();
        a.begin();
        obj1.set(1234);
        AtomicAction.suspend();

        AtomicAction b = new AtomicAction();
        b.begin();
        obj2.set(12345);
        b.commit();

        AtomicAction.resume(a);
        assertEquals(a.commit(), ActionStatus.ABORTED);

        AtomicAction c = new AtomicAction();
        c.begin();
        assertEquals(obj1.get(), 12345);
        assertEquals(obj2.get(), 12345);
        c.commit();
        System.out.println("done");
    }
}