package io.narayana.lra.arquillian;

import io.narayana.lra.client.NarayanaLRAClient;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunAsClient
@RunWith(Arquillian.class)
public abstract class TestBase {

    public static NarayanaLRAClient lraClient;
    public static String coordinatorUrl;
    public Client client;
    public List<URI> lrasToAfterFinish;

    @BeforeClass
    public static void beforeClass() {
        lraClient = new NarayanaLRAClient();
        coordinatorUrl = lraClient.getCoordinatorUrl();
    }

    @Before
    public void before() {
        client = ClientBuilder.newClient();
        lrasToAfterFinish = new ArrayList<>();
    }

    @After
    public void after() {
        List<URI> lraURIList = lraClient.getAllLRAs().stream().map(x -> x.getLraId()).collect(Collectors.toList());
        for (URI lraToFinish: lrasToAfterFinish) {
            if (lraURIList.contains(lraToFinish)) {
                lraClient.cancelLRA(lraToFinish);
            }
        }

        if (client != null) {
            client.close();
        }
    }
}