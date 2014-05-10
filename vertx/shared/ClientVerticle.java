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

import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.stm.Container;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;

public class ClientVerticle extends Verticle {
    public static String LEADER = "LEADER_SLOT";

  public void start() {

    ConcurrentMap<String, String> map = vertx.sharedData().getMap("demo.mymap");

    Container<Sample> theContainer = new Container<Sample>("Demo", Container.TYPE.PERSISTENT, Container.MODEL.SHARED);
    Sample obj1 = theContainer.create(new SampleLockable(10));

    map.put(LEADER, theContainer.getIdentifier(obj1).toString());

    container.deployVerticle("SampleVerticle1.java");

    // container.deployVerticle("SampleVerticle2.java", 4);

    System.out.println("Object name: "+theContainer.getIdentifier(obj1));

    //Now send some data
    for (int i = 0; i < 10; i++) {
	AtomicAction A = new AtomicAction();
	boolean shouldCommit = true;

	A.begin();

	try
	{
	    obj1.increment();

	    System.out.println("State value is: "+obj1.value());
	}
	catch (final Throwable ex)
	{
	    ex.printStackTrace();

	    shouldCommit = false;
	}

	if (shouldCommit)
	    A.commit();
	else
	    A.abort();
    }

    AtomicAction B = new AtomicAction();
    int value = -1;
    boolean doCommit = true;

    B.begin();

    try
    {
	value = obj1.value();
    }
    catch (final Throwable ex)
    {
	doCommit = false;
    }

    if (doCommit)
    {
	B.commit();
	
	System.out.println("ClientVerticle initialised state: "+value);
    }
    else
    {
	B.abort();

	System.out.println("ClientVerticle could not initialise state.");
    }
  }
}
