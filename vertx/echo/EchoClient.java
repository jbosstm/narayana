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

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;
import org.jboss.stm.Container;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;

public class EchoClient extends Verticle {

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

  public void start() {
    vertx.createNetClient().connect(1234, "localhost", new AsyncResultHandler<NetSocket>() {
      public void handle(AsyncResult<NetSocket> asyncResult) {
        if (asyncResult.succeeded()) {
          NetSocket socket = asyncResult.result();
          socket.dataHandler(new Handler<Buffer>() {
            public void handle(Buffer buffer) {
              System.out.println("Net client receiving: " + buffer);
            }
          });

	  /*
	   * If you are running this for the first time then leave as is.
	   * If you are running this more than once and want clients to share the STM objects between
	   * address spaces then go into the ObjectStore dir and look for the Uid that represents the state
	   * you want to share. Then uncomment the Uid line below and replace the Uid in quotes with the Uid
	   * you have selected. Uncomment the other obj1 creation line and comment out the original.
	   *
	   * If you want to see how this might work then just go with the example state in the ObjectStore
	   * shipped as part of this example and uncomment the lines.
	   */
	  
	  /*
	   * STM states are identified by Uids in the ObjectStore. This is an example.
	   */

	  // Modify this line if sharing state and uncomment.
	  // Uid u = new Uid("0:ffffc0a80003:c915:529f59de:1");
	  Uid u = new Uid("0:ffffc0a8000f:e84e:5325d6d2:0");

	  Container<Sample> theContainer = new Container<Sample>("Demo", Container.TYPE.PERSISTENT, Container.MODEL.SHARED);

	  // Modify this line if sharing state and uncomment.
	  Sample obj1 = theContainer.clone(new SampleLockable(10), u);

	  // Comment it out if you are going to share state.
	  // Sample obj1 = theContainer.create(new SampleLockable(10));

	System.out.println("Object name: "+theContainer.getIdentifier(obj1));

          //Now send some data
          for (int i = 0; i < 10; i++) {
	      AtomicAction A = new AtomicAction();

	      A.begin();
	      obj1.increment();

	      String str = "hello" + obj1.value() + "\n";
            System.out.print("Net client sending: " + str);
            socket.write(new Buffer(str));

	    A.commit();
          }
        } else {
          asyncResult.cause().printStackTrace();
        }
      }
    });
  }
}
