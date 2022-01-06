package io.narayana.lra.coordinator.domain.model.objectstore;

import io.narayana.lra.LRAData;
import io.narayana.lra.client.NarayanaLRAClient;
import io.narayana.lra.coordinator.api.Coordinator;
import io.narayana.lra.filter.ServerLRAFilter;
import io.narayana.lra.logging.LRALogger;
import io.narayana.lra.provider.ParticipantStatusOctetStreamProvider;
import org.eclipse.microprofile.lra.annotation.LRAStatus;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.narayana.lra.LRAConstants.COORDINATOR_PATH_NAME;

public class TestBase {

    static UndertowJaxrsServer server;
    NarayanaLRAClient lraClient;
    Client client;

    @Rule
    public TestName testName = new TestName();

    @ApplicationPath("/base")
    public static class LRAInfo extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(Coordinator.class);
            classes.add(ServerLRAFilter.class);
            classes.add(ParticipantStatusOctetStreamProvider.class);
            return classes;
        }
    }

    @BeforeClass
    static void start() {
        System.setProperty("lra.coordinator.url", TestPortProvider.generateURL('/' + COORDINATOR_PATH_NAME));
    }

    @Before
    public void before() {
        LRALogger.logger.debugf("Starting test %s", testName.getMethodName());

        lraClient = new NarayanaLRAClient();

        client = ClientBuilder.newClient();

        server = new UndertowJaxrsServer().start();
        server.deploy(Coordinator.class);
        server.deploy(TestBase.LRAInfo.class);
    }

    @After
    public void after() {
        LRALogger.logger.debugf("Finished test %s", testName.getMethodName());

        lraClient.close();
        client.close();

        // Makes sure that all LRAs will be ended
        clearObjectStore();

        server.stop();
    }

    String convertLraUriToString(URI lraIdUri) {
        String lraIdString = lraIdUri.toString();
        return lraIdString.substring(lraIdString.lastIndexOf('/') + 1);
    }

    LRAStatus getStatus(URI lra) {
        try {
            return lraClient.getStatus(lra);
        } catch (NotFoundException ignore) {
            return null;
        }
    }

    LRAData getLastCreatedLRA() {

        List<LRAData> LRAList = new ArrayList<>();

        try {
            LRAList = lraClient.getAllLRAs();
        } catch (Exception ex) {
            LRALogger.logger.error(ex.getMessage());
        }

        return (LRAList.isEmpty() ? null : LRAList.get(LRAList.size() - 1));
    }

    void clearObjectStore() {

        try {
            List<LRAData> lraDataList = lraClient.getAllLRAs();

            for (LRAData lra : lraDataList) {
                lraClient.closeLRA(lra.getLraId());
            }
        } catch (WebApplicationException ex) {
            LRALogger.logger.error(ex.getMessage());
        }
    }
}
