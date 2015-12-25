package org.jboss.stm;

/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;
import org.vertx.java.core.eventbus.Message;

import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.Container;

import com.arjuna.ats.arjuna.AtomicAction;

/**
 * This verticle is pretty artificial and is here only so that the module
 * has something that people can look at and hopefully understand where STM
 * fits in.
 *
 * Could consider removing this and making the module a pure library version, with
 * some tests/examples separate.
 */

public class STMVerticle extends Verticle {
    
    public STMVerticle ()
    {
	transactionalObject = theContainer.create(new SampleLockable(10));

	System.out.println("Object name: "+theContainer.getIdentifier(transactionalObject));

	AtomicAction A = new AtomicAction();

	/*
	 * Flush state to disk (for this example).
	 */

	A.begin();

	transactionalObject.increment();

	A.commit();
    }

    @Transactional
    public interface Sample
    {
       public void increment ();
       public void decrement ();
       
       public int value ();
    }
    
    @Transactional
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

    public void start() {

    vertx.eventBus().registerHandler("ping-address", new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> message) {

          //Now send some data
          for (int i = 0; i < 10; i++) {
	      
	      int value = STMVerticle.value();

	    message.reply("pong! "+value);

	    container.logger().info("Sent back pong "+value);
	  }
      }
      });

    container.logger().info("STMVerticle started");
  }

    /*
     * Have a persistent container for this example, but it's likely recoverable and optimistic cc are better for Vert.x.
     */
    static final private Container<Sample> theContainer = new Container<Sample>("Demo", Container.TYPE.PERSISTENT, Container.MODEL.SHARED);
    static private Sample transactionalObject = null;
}
