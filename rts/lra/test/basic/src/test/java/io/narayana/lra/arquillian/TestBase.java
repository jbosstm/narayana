/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.arquillian;

import io.narayana.lra.LRAConstants;
import io.narayana.lra.LRAData;
import io.narayana.lra.client.NarayanaLRAClient;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
        List<URI> lraURIList = lraClient.getAllLRAs().stream().map(LRAData::getLraId).collect(Collectors.toList());
        for (URI lraToFinish: lrasToAfterFinish) {
            if (lraURIList.contains(lraToFinish)) {
                lraClient.cancelLRA(lraToFinish);
            }
        }

        if (client != null) {
            client.close();
        }
    }

    protected JsonArray getAllRecords(URI lra) {
        String coordinatorUrl = LRAConstants.getLRACoordinatorUrl(lra) + "/";

        try (Response response = client.target(coordinatorUrl).path("").request().get()) {
            Assert.assertTrue("Missing response body when querying for all LRAs", response.hasEntity());
            String allLRAs = response.readEntity(String.class);

            JsonReader jsonReader = Json.createReader(new StringReader(allLRAs));
            return jsonReader.readArray();
        }
    }

    protected URI invokeInTransaction(URL baseURL, int expectedStatus) {
        try(Response response = client.target(baseURL.toURI())
                .path(io.narayana.lra.arquillian.resource.LRAUnawareResource.ROOT_PATH)
                .path(io.narayana.lra.arquillian.resource.LRAUnawareResource.RESOURCE_PATH)
                .request()
                .get()) {

            Assert.assertTrue("Expecting a non empty body in response from " + io.narayana.lra.arquillian.resource.LRAUnawareResource.ROOT_PATH + "/" + io.narayana.lra.arquillian.resource.LRAUnawareResource.RESOURCE_PATH,
                    response.hasEntity());

            String entity = response.readEntity(String.class);

            Assert.assertEquals(
                    "response from " + io.narayana.lra.arquillian.resource.LRAUnawareResource.ROOT_PATH + "/" + io.narayana.lra.arquillian.resource.LRAUnawareResource.RESOURCE_PATH + " was " + entity,
                    expectedStatus, response.getStatus());

            return new URI(entity);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("response cannot be converted to URI: " + e.getMessage());
        }
    }
}
