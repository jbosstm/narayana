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

import java.io.IOException;

import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;

import com.arjuna.ats.arjuna.AtomicAction;

import junit.framework.TestCase;

/**
 * @author Mark Little
 */

class DummyVerticle
{
    public DummyVerticle ()
    {
        transactionalObject = theContainer.create(new SampleLockable(10));

        System.out.println("Object name: "+theContainer.getIdentifier(transactionalObject));
    }

    @Transactional
    public interface Sample
    {
       public void increment ();
       public void decrement ();
       
       public int value ();
    }

    public class SampleLockable implements Sample
    {
        public SampleLockable (int init)
        {
            _isState = init;
        }
        
        @ReadLock
        public int value ()
        {
            return _isState;
        }

        @WriteLock
        public void increment ()
        {
            _isState++;
        }
        
        @WriteLock
        public void decrement ()
        {
            _isState--;
        }

        @State
        private int _isState;
    }

    static public int value ()
    {
        AtomicAction A = new AtomicAction();
        int result = -1;

        A.begin();

        transactionalObject.increment();

        result = transactionalObject.value();

        A.commit();

        return result;
    }

    static final private Container<Sample> theContainer = new Container<Sample>("Demo", Container.TYPE.PERSISTENT, Container.MODEL.SHARED);
    static private Sample transactionalObject = null;
}

public class VertxUnitTest extends TestCase
{
    public void testVerticle()
    {
      DummyVerticle vert = new DummyVerticle();

      // do something with verticle

      AtomicAction A = new AtomicAction();

      A.begin();

      int amount = vert.value();
      
      A.commit();  // flush state to disk (if relevant)!
      
      assertEquals(amount, 11);  // initial state of 10 plus 1 from call to value (increment).
      
      A = new AtomicAction();
      
      A.begin();
      
      amount = vert.value();

      A.abort();

      assertEquals(vert.value(), amount);
    }
}

