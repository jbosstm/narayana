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

import org.vertx.java.platform.Verticle;
import java.util.concurrent.ConcurrentMap;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.AtomicAction;
import org.jboss.stm.Container;

public class SampleVerticle2 extends Verticle {

  public void start()
  {
      ConcurrentMap<String, String> map = vertx.sharedData().getMap("demo.mymap");
      Container<Sample> theContainer = new Container<Sample>("Demo", Container.TYPE.PERSISTENT, Container.MODEL.SHARED);
      String uidName = map.get(ClientVerticle.LEADER);
      Sample obj1 = theContainer.clone(new SampleLockable(10), new Uid(uidName));
      AtomicAction A = new AtomicAction();
      int value = -1;
      int initialValue = -1;
      boolean shouldCommit = true;

      A.begin();

      try
      {
	  initialValue = obj1.value();

	  obj1.increment();
      }
      catch (final Throwable ex)
      {
	  ex.printStackTrace();

	  shouldCommit = false;
      }

      try
      {
	  if (shouldCommit)
	  {
	      obj1.increment();

	      value = obj1.value();
	  }
      }
      catch (final Throwable ex)
      {
	  ex.printStackTrace();

	  shouldCommit = false;
      }

      if (shouldCommit)
	  A.commit();
      else
      {
	  A.abort();
	  value = -1;
      }

      System.err.println("SampleVerticle2 initialised state with: "+value);

      if (value == initialValue +2)
	  System.err.println("SampleVerticle2 SUCCEEDED!");
      else
	  System.err.println("SampleVerticle2 FAILED!");
  }
}
