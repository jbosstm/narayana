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

import java.io.IOException;

import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.Container;

import com.arjuna.ats.arjuna.AtomicAction;

import org.junit.Test;
import static org.vertx.testtools.VertxAssert.*;

/**
 * @author Mark Little
 */

public class BasicUnitTest
{   
    /**
     * This is out Transactional interface. We'll use this as the type for a
     * Container.
     */

    // default pessimistic

    @Transactional
    public interface Atomic
    {
        public void change (int value) throws Exception;
        
        public void set (int value) throws Exception;
        
        public int get () throws Exception;
    }
    
    @Transactional
    public class ExampleSTM implements Atomic
    {   
	/*
	 * Define read/write operations here. Can do them in the interface
	 * if you want.
	 */

        @ReadLock
        public int get () throws Exception
        {
            return state;
        }

        @WriteLock
        public void set (int value) throws Exception
        {
            state = value;
        }
        
        @WriteLock
        public void change (int value) throws Exception
        {
            state += value;
        }

	/**
	 * This is the state that will be manipulated (saved and restored).
	 */

        private int state;
    }

    @Test
    public void testExampleSTM () throws Exception
    {
	/*
	 * Create the container for the Transactional interface.
	 */
        Container<Atomic> theContainer = new Container<Atomic>();

	/*
	 * Create the instance of the class. But this won't be an STM object yet, so don't
	 * manipulate it just yet.
	 */

        ExampleSTM basic = new ExampleSTM();
        boolean success = true;

	/*
	 * This object will be the one we actually use.
	 */

        Atomic obj = null;
        
        try
        {
	    /*
	     * Pass the instance we created previously to the Container so it
	     * can then create an STM object which we then use to manipulate
	     * the first object in a transactional manner.
	     */

            obj = theContainer.create(basic);
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            
            success = false;
        }
        
        assertTrue(success);
        
	// a transaction!

        AtomicAction a = new AtomicAction();
        
        a.begin();
        
        obj.set(1234);
        
        a.commit();

        assertEquals(obj.get(), 1234);
        
        a = new AtomicAction();

        a.begin();

        obj.change(1);  // the value at this stage will be 1235
        
        a.abort();

        assertEquals(obj.get(), 1234);  // we aborted, so the value should be back to 1234
    }
}