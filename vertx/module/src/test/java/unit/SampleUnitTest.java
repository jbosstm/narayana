package unit;

import org.junit.Test;

/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

import org.jboss.stm.Container;
import org.jboss.stm.annotations.Optimistic;
import org.jboss.stm.annotations.Transactional;

import com.arjuna.ats.arjuna.AtomicAction;

import org.junit.Test;
import static org.vertx.testtools.VertxAssert.*;

/**
 * @author Mark Little
 */

public class SampleUnitTest
{   
    @Transactional
    @Optimistic
    public interface Sample
    {
       public void increment ();
       public void decrement ();
       
       public int value ();
    }
    
    @Transactional
    @Optimistic
    public class SampleLockable implements Sample
    {
        public SampleLockable ()
        {
            this(0);
        }
        
        public SampleLockable (int init)
        {
            _isState = init;
        }
        
        public int value ()
        {
            return _isState;
        }

        public void increment ()
        {
            _isState++;
        }

        public void decrement ()
        {
            _isState--;
        }

        private int _isState;
    }

    @Test
    public void test ()
    {
	/*
	 * Commented out until we get a fix in Narayana.
	 */

	/*
        Container<Sample> theContainer = new Container<Sample>();
        Sample obj1 = theContainer.create(new SampleLockable(10));
        Sample obj2 = theContainer.clone(new SampleLockable(), obj1);  // could we do this by inference (look at 2nd parameter) or by annotation?
        
        assertTrue(obj2 != null);
        
        AtomicAction act = new AtomicAction();
        
        act.begin();
        
        obj1.increment();
        
        act.commit();
        
        act = new AtomicAction();
        
        act.begin();
        
        assertEquals(obj2.value(), 11);
        
        act.commit();
	*/
    }
}