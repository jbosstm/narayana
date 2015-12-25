package unit;

import org.jboss.stm.STMVerticle;
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

import com.arjuna.ats.arjuna.AtomicAction;
import org.junit.Test;

import static org.vertx.testtools.VertxAssert.*;

public class STMUnitTest {

  @Test
  public void testVerticle() {
    STMVerticle vert = new STMVerticle();

    // do something with verticle

    AtomicAction A = new AtomicAction();

    A.begin();

    int amount = vert.value();

    A.abort();

    assertTrue(vert.value() == amount);
  }
}
