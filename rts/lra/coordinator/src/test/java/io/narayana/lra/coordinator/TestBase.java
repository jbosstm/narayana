/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.coordinator;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import io.narayana.lra.LRAData;
import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.lra.coordinator.domain.service.LRAService;
import io.narayana.lra.logging.LRALogger;
import org.eclipse.microprofile.lra.annotation.LRAStatus;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;

public abstract class TestBase {

    static UndertowJaxrsServer server;
    static LRAService service;

    NarayanaLRAClient lraClient;
    String coordinatorPath;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void start() {
        service = new LRAService();
        System.setProperty("lra.coordinator.url", TestPortProvider.generateURL('/' + COORDINATOR_PATH_NAME));
        RecoveryManager.manager();
    }

    @Before
    public void before() {
        LRALogger.logger.debugf("Starting test %s", testName);
        server = new UndertowJaxrsServer().start();

        lraClient = new NarayanaLRAClient();

        coordinatorPath = TestPortProvider.generateURL('/' + COORDINATOR_PATH_NAME);
    }

    @After
    public void after() {
        LRALogger.logger.debugf("Finished test %s", testName);
        clearObjectStore();
        lraClient.close();
        server.stop();
    }

    int recover() {
        List<LRAData> recoveringLRAs = service.getAllRecovering(true);

        return recoveringLRAs.size();
    }

    void doWait(long millis) throws InterruptedException {
        if (millis > 0L) {
            Thread.sleep(millis);
        }
    }

    LRAStatus getStatus(URI lra) {
        try {
            return lraClient.getStatus(lra);
        } catch (NotFoundException ignore) {
            return null;
        }
    }

    void clearObjectStore() {

        List<LRAData> LRAList = new ArrayList<>();

        try {
            LRAList = service.getAll();
        } catch (Exception ex) {
            LRALogger.logger.debugf(ex,"Cannot fetch LRAs through LRAService");
        }

        for (LRAData lra : LRAList) {
            service.remove(lra.getLraId());
        }
    }
}
