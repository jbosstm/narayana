/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package io.narayana.lra.arquillian.api;

import io.narayana.lra.arquillian.TestBase;
import org.hamcrest.MatcherAssert;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.function.Supplier;

import static org.hamcrest.Matchers.containsString;

/**
 * This testcase verifies behaviour of API endpoints when an unsupported version is provided within
 * the header {@link io.narayana.lra.LRAConstants#NARAYANA_LRA_API_VERSION_HEADER_NAME}
 * (see supported versions at {@link io.narayana.lra.LRAConstants#NARAYANA_LRA_API_SUPPORTED_VERSIONS}).
 *
 * When client requests the unsupported version then is expected that
 * the implementation responds with {@code 417} HTTP status code.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class CoordinatorApiWrongVersionIT extends TestBase {
    private static final Logger log = Logger.getLogger(CoordinatorApiWrongVersionIT.class);

    static final String NARAYANA_LRA_API_VERSION_HEADER = "Narayana-LRA-API-version";
    static final String NOT_SUPPORTED_FUTURE_LRA_VERSION = Integer.MAX_VALUE + ".1";

    @Rule
    public TestName testName = new TestName();

    @Override
    public void before() {
        super.before();
        log.info("Running test " + testName.getMethodName());
    }

    @Test
    public void getAllLRAsWrongVersion() {
        verifyStatusCode(() -> client.target(coordinatorUrl)
                .request().header(NARAYANA_LRA_API_VERSION_HEADER, NOT_SUPPORTED_FUTURE_LRA_VERSION).get());
    }

    @Test
    public void getLRAStatusWrongVersion() {
        verifyStatusCode(() -> client.target(coordinatorUrl).path("status")
                .request().header(NARAYANA_LRA_API_VERSION_HEADER, NOT_SUPPORTED_FUTURE_LRA_VERSION).get());
    }

    @Test
    public void getLRAInfoWrongVersion() {
        verifyStatusCode(() -> client.target(coordinatorUrl).path("lra-id")
                .request().header(NARAYANA_LRA_API_VERSION_HEADER, NOT_SUPPORTED_FUTURE_LRA_VERSION).get());
    }

    @Test
    public void startLRAWrongVersion() {
        verifyStatusCode(() -> client.target(coordinatorUrl).path("start")
                .request().header(NARAYANA_LRA_API_VERSION_HEADER, NOT_SUPPORTED_FUTURE_LRA_VERSION).post(null));
    }

    @Test
    public void renewTimeLimitWrongVersion() {
        verifyStatusCode(() -> client.target(coordinatorUrl).path("lra-id").path("renew")
                .request().header(NARAYANA_LRA_API_VERSION_HEADER, NOT_SUPPORTED_FUTURE_LRA_VERSION).put(null));
    }

    @Test
    public void closeLRAWrongVersion() {
        verifyStatusCode(() -> client.target(coordinatorUrl).path("lra-id").path("close")
                .request().header(NARAYANA_LRA_API_VERSION_HEADER, NOT_SUPPORTED_FUTURE_LRA_VERSION).put(null));
    }

    @Test
    public void cancelLRAWrongVersion() {
        verifyStatusCode(() -> client.target(coordinatorUrl).path("lra-id").path("cancel")
                .request().header(NARAYANA_LRA_API_VERSION_HEADER, NOT_SUPPORTED_FUTURE_LRA_VERSION).put(null));
    }

    @Test
    public void joinViaBodyWrongVersion() {
        verifyStatusCode(() -> client.target(coordinatorUrl).path("lra-id").request()
                .header(NARAYANA_LRA_API_VERSION_HEADER, NOT_SUPPORTED_FUTURE_LRA_VERSION).put(Entity.text("compensator-url")));
    }

    @Test
    public void leaveLRAWrongVersion() {
        verifyStatusCode(() -> client.target(coordinatorUrl).path("lra-id").path("remove").request()
                .header(NARAYANA_LRA_API_VERSION_HEADER, NOT_SUPPORTED_FUTURE_LRA_VERSION).put(Entity.text("participant-url")));
    }

    private static void verifyStatusCode(Supplier<Response> responseSupplier) {
        try (Response response = responseSupplier.get()) {
            Assert.assertEquals("Expected different error code when used unsupported version at Coordinator REST API endpoint",
                    Status.EXPECTATION_FAILED.getStatusCode(), response.getStatus());
            MatcherAssert.assertThat("Expected the response to contain error message with the unsupported version included",
                    response.readEntity(String.class), containsString(NOT_SUPPORTED_FUTURE_LRA_VERSION));
        }
    }
}
