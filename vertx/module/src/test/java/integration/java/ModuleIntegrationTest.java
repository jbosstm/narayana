package integration.java;
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
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import io.vertx.test.core.VertxTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.Test;

/**
 * Example Java integration test that deploys the module that this project builds.
 *
 * Quite often in integration tests you want to deploy the same module for all tests and you don't want tests
 * to start before the module has been deployed.
 *
 * This test demonstrates how to do that.
 */
public class ModuleIntegrationTest extends VertxTestBase {

  private Logger logger = LoggerFactory.getLogger(ModuleIntegrationTest.class);

  @Test
  //public void testPing(TestContext context) {
  public void testPing() {
TestContext context = null;
      System.out.printf("in testPing()%n");
      logger.info("in testPing()");
      EventBus eb = vertx.eventBus();
      eb.publish("ping-address", "ping!");
      eb.consumer("ping-address", (Message<JsonObject> reply) -> {
          context.assertEquals("pong! 12", reply.body());
          /*
          If we get here, the test is complete
          You must always call `testComplete()` at the end. Remember that testing is *asynchronous* so
          we cannot assume the test is complete by the time the test method has finished executing like
          in standard synchronous tests
          */
          testComplete();
      });
  }

  @Test
  public void testSomethingElse() {
    // Whatever
    testComplete();
  }
}
