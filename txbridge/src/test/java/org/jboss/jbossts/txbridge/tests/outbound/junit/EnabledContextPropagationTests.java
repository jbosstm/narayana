/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.jbossts.txbridge.tests.outbound.junit;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.jbossts.txbridge.tests.outbound.client.TestATClient;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.json.JsonArray;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@RunWith(Arquillian.class)
public final class EnabledContextPropagationTests {

    public static final String OUTBOUND_SERVICE_DEPLOYMENT_NAME = "txbridge-outbound-tests-service";

    public static final String OUTBOUND_CLIENT_DEPLOYMENT_NAME = "txbridge-outbound-tests-client";

    private static final String CONTAINER = "jboss";

    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @Deployment(name = OUTBOUND_SERVICE_DEPLOYMENT_NAME, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> createServiceArchive() {
        Archive<?> archive = ShrinkWrap.create(WebArchive.class, OUTBOUND_SERVICE_DEPLOYMENT_NAME + ".war")
                .addClass(org.jboss.jbossts.txbridge.tests.outbound.service.TestATServiceImpl.class)
                .addClass(org.jboss.jbossts.txbridge.tests.outbound.service.TestNonATServiceImpl.class)
                .addClass(org.jboss.jbossts.txbridge.tests.outbound.service.TestATServiceParticipant.class)
                .addAsResource("outbound/jaxws-handlers-server.xml", "jaxws-handlers-server.xml")
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.xts,org.jboss.jts\n"), "MANIFEST.MF");
        return archive;
    }

    @Deployment(name = OUTBOUND_CLIENT_DEPLOYMENT_NAME, testable = false)
    @TargetsContainer(CONTAINER)
    public static Archive<?> createClientArchive() {
        Archive<?> archive = ShrinkWrap
                .create(WebArchive.class, OUTBOUND_CLIENT_DEPLOYMENT_NAME + ".war")
                .addClass(org.jboss.jbossts.txbridge.tests.outbound.client.CommonTestService.class)
                .addClass(org.jboss.jbossts.txbridge.tests.outbound.client.TestATService.class)
                .addClass(org.jboss.jbossts.txbridge.tests.outbound.client.TestNonATService.class)
                .addClass(org.jboss.jbossts.txbridge.tests.outbound.client.TestATClient.class)
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.xts,org.jboss.jts\n"),
                        "MANIFEST.MF");
        return archive;
    }

    @Before
    public void before() {
        controller.start(CONTAINER);
    }

    @After
    public void after() {
        controller.stop(CONTAINER);
    }

    // Tests without features

    /**
     * Tests commit without WSTXFeature and JTAOverWSATFeature.
     *
     * Prepare and commit calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCommitWithoutFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isCommit", "true"));
        parameters.add(new BasicNameValuePair("isTransaction", "true"));
        parameters.add(new BasicNameValuePair("isWSATService", "true"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITHOUT_FEATURES)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations, "prepare", "commit");
    }

    /**
     * Tests non-transactional invocation without WSTXFeature and JTAOverWSATFeature.
     *
     * No two-phase commit calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testNoTransactionWithoutFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isTransaction", "false"));
        parameters.add(new BasicNameValuePair("isWSATService", "true"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITHOUT_FEATURES)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations);
    }

    /**
     * Tests transactional invocation to non-transaction service without WSTXFeature and JTAOverWSATFeature.
     *
     * No two-phase commit calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testNonTransactionlServiceWithoutFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isCommit", "true"));
        parameters.add(new BasicNameValuePair("isTransaction", "true"));
        parameters.add(new BasicNameValuePair("isWSATService", "false"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITHOUT_FEATURES)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations);
    }

    // Tests with enabled JTAOverWSATFeature features

    /**
     * Tests commit with enabled JTAOverWSATFeature.
     *
     * Prepare and commit calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCommitWithEnabledJTAOverWSATFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isCommit", "true"));
        parameters.add(new BasicNameValuePair("isTransaction", "true"));
        parameters.add(new BasicNameValuePair("isWSATService", "true"));
        parameters.add(new BasicNameValuePair("isJTAOverWSATFeatureEnabled", "true"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITH_JTA_FEATURE)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations, "prepare", "commit");
    }

    /**
     * Tests non-transactional invocation with enabled JTAOverWSATFeature.
     *
     * No two-phase commit calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testNoTransactionWithEnabledJTAOverWSATFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isTransaction", "false"));
        parameters.add(new BasicNameValuePair("isWSATService", "true"));
        parameters.add(new BasicNameValuePair("isJTAOverWSATFeatureEnabled", "true"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITH_JTA_FEATURE)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations);
    }

    /**
     * Tests transactional invocation to non-transaction service with enabled JTAOverWSATFeature.
     *
     * Calls to the service will fail when the Web service does not support WS-AT.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testNonTransactionlServiceWithEnabledJTAOverWSATFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isCommit", "true"));
        parameters.add(new BasicNameValuePair("isTransaction", "true"));
        parameters.add(new BasicNameValuePair("isWSATService", "false"));
        parameters.add(new BasicNameValuePair("isJTAOverWSATFeatureEnabled", "true"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITH_JTA_FEATURE)));
        try {
            makeRequest(baseURL, parameters);
            Assert.fail("'MustUnderstand headers' exception should be thrown.");
        } catch (HttpResponseException e) {
            // this is expected
        }
    }

    // Tests with disabled JTAOverWSATFeature features

    /**
     * Tests commit with disabled JTAOverWSATFeature.
     *
     * No two-phase commit calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCommitWithDisabledJTAOverWSATFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isCommit", "true"));
        parameters.add(new BasicNameValuePair("isTransaction", "true"));
        parameters.add(new BasicNameValuePair("isWSATService", "true"));
        parameters.add(new BasicNameValuePair("isJTAOverWSATFeatureEnabled", "false"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITH_JTA_FEATURE)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations);
    }

    /**
     * Tests non-transactional invocation with disabled JTAOverWSATFeature.
     *
     * No two-phase commit calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testNoTransactionWithDisabledJTAOverWSATFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isTransaction", "false"));
        parameters.add(new BasicNameValuePair("isWSATService", "true"));
        parameters.add(new BasicNameValuePair("isJTAOverWSATFeatureEnabled", "false"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITH_JTA_FEATURE)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations);
    }

    /**
     * Tests transactional invocation to non-transaction service with disabled JTAOverWSATFeature.
     *
     * No two-phase commit calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testNonTransactionlServiceWithDisabledJTAOverWSATFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isCommit", "true"));
        parameters.add(new BasicNameValuePair("isTransaction", "true"));
        parameters.add(new BasicNameValuePair("isWSATService", "false"));
        parameters.add(new BasicNameValuePair("isJTAOverWSATFeatureEnabled", "false"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITH_JTA_FEATURE)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations);
    }

    // Tests with both features

    /**
     * Tests commit with enabled JTAOverWSATFeature and enabled WSTXFeature.
     *
     * Commit and prepare calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCommitWithEnabledJTAOverWSATFeatureAndEnabledWSTXFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isCommit", "true"));
        parameters.add(new BasicNameValuePair("isTransaction", "true"));
        parameters.add(new BasicNameValuePair("isWSATService", "true"));
        parameters.add(new BasicNameValuePair("isJTAOverWSATFeatureEnabled", "true"));
        parameters.add(new BasicNameValuePair("isWSTXFeatureEnabled", "true"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITH_BOTH_FEATURES)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations, "prepare", "commit");
    }

    /**
     * Tests commit with enabled JTAOverWSATFeature and disabled WSTXFeature.
     *
     * Commit and prepare calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCommitWithEnabledJTAOverWSATFeatureAndDisabledWSTXFeature(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isCommit", "true"));
        parameters.add(new BasicNameValuePair("isTransaction", "true"));
        parameters.add(new BasicNameValuePair("isWSATService", "true"));
        parameters.add(new BasicNameValuePair("isJTAOverWSATFeatureEnabled", "true"));
        parameters.add(new BasicNameValuePair("isWSTXFeatureEnabled", "false"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITH_BOTH_FEATURES)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations, "prepare", "commit");
    }

    // Test with manually added handlers

    /**
     * Tests commit with manually added handlers.
     *
     * Prepare and commit calls are expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testCommitWithManuallyAddedHandlers(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isCommit", "true"));
        parameters.add(new BasicNameValuePair("isTransaction", "true"));
        parameters.add(new BasicNameValuePair("isWSATService", "true"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITH_MANUAL_HANDLERS)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations, "prepare", "commit");
    }

    /**
     * Tests rollback with manually added handlers.
     *
     * Rollback call is expected.
     *
     * @throws Exception
     */
    @Test
    @OperateOnDeployment(OUTBOUND_CLIENT_DEPLOYMENT_NAME)
    public void testRollbackWithManuallyAddedHandlers(@ArquillianResource URL baseURL) throws Exception {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("isCommit", "false"));
        parameters.add(new BasicNameValuePair("isTransaction", "true"));
        parameters.add(new BasicNameValuePair("isWSATService", "true"));
        parameters.add(new BasicNameValuePair("clientType", String.valueOf(TestATClient.CLIENT_WITH_MANUAL_HANDLERS)));

        List<String> invocations = makeRequest(baseURL, parameters);
        assertInvocations(invocations, "rollback");
    }

    private List<String> makeRequest(URL baseURL, List<NameValuePair> parameters) throws ClientProtocolException, IOException {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(baseURL.toString() + TestATClient.URL_PATTERN);
        post.setEntity(new UrlEncodedFormEntity(parameters));

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String response = httpClient.execute(post, responseHandler);

        JsonArray jsonArray = jakarta.json.Json.createReader(new StringReader(response)).readArray();
        return jsonArray.getValuesAs((jsonString) -> jsonString.toString().replaceAll("^\"(.*)\"$", "$1"));
    }

    private void assertInvocations(List<String> actual, String... expected) {
        Assert.assertArrayEquals(expected, actual.toArray());
    }

}
