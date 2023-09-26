package integration.java;
/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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