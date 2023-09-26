/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.common;

import java.io.IOException;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.byteman.agent.submit.Submit;
import org.jboss.byteman.contrib.dtest.Instrumentor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * Common methods for tx bridge test cases.
 *
 * @author Ivo Studensky (istudens@redhat.com)
 */
public abstract class AbstractBasicTests {

    protected static Instrumentor instrumentor = null;

    public static final String INBOUND_SERVICE_DEPLOYMENT_NAME = "txbridge-inbound-tests-service";
    public static final String INBOUND_CLIENT_DEPLOYMENT_NAME = "txbridge-inbound-tests-client";

    public static final String OUTBOUND_SERVICE_DEPLOYMENT_NAME = "txbridge-outbound-tests-service";
    public static final String OUTBOUND_CLIENT_DEPLOYMENT_NAME = "txbridge-outbound-tests-client";

    protected static final String CONTAINER = "jboss";

    protected static Archive<?> getInboundServiceArchive() {
        Archive<?> archive = ShrinkWrap.create(WebArchive.class, INBOUND_SERVICE_DEPLOYMENT_NAME + ".war")
                .addPackage("org.jboss.jbossts.txbridge.tests.inbound.service")
                .addPackage("org.jboss.jbossts.txbridge.tests.inbound.utility")
//                .addAsManifestResource("inbound/jboss-beans.xml", "jboss-beans.xml")
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.xts,org.jboss.jts\n"), "MANIFEST.MF");
//        archive.as(ZipExporter.class).exportTo(new File("/tmp/deployment.zip"), true);
        return archive;
    }

    protected static Archive<?> getInboundClientArchive() {
        Archive<?> archive = ShrinkWrap.create(WebArchive.class, INBOUND_CLIENT_DEPLOYMENT_NAME + ".war")
                .addPackage("org.jboss.jbossts.txbridge.tests.inbound.client")
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.xts,org.jboss.jts\n"), "MANIFEST.MF");
        return archive;
    }

    protected static Archive<?> getOutboundServiceArchive() {
        Archive<?> archive = ShrinkWrap.create(WebArchive.class, OUTBOUND_SERVICE_DEPLOYMENT_NAME + ".war")
                .addClass(org.jboss.jbossts.txbridge.tests.outbound.service.TestServiceImpl.class)
                .addPackage("org.jboss.jbossts.txbridge.tests.outbound.utility")
                .addAsResource("outbound/jaxws-handlers-server.xml", "jaxws-handlers-server.xml")
//                .addAsManifestResource("outbound/jboss-beans.xml", "jboss-beans.xml")
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.xts,org.jboss.jts\n"), "MANIFEST.MF");
        return archive;
    }

    protected static Archive<?> getOutboundClientArchive() {
        Archive<?> archive = ShrinkWrap.create(WebArchive.class, OUTBOUND_CLIENT_DEPLOYMENT_NAME + ".war")
                .addClass(org.jboss.jbossts.txbridge.tests.outbound.client.TestClient.class)
                .addClass(org.jboss.jbossts.txbridge.tests.outbound.client.TestService.class)
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.xts,org.jboss.jts\n"), "MANIFEST.MF");
        return archive;
    }


    @BeforeClass
    public static void beforeClass() throws Exception {
        if (instrumentor == null) {
            instrumentor = new Instrumentor(new Submit(), 1199);
        }
    }


    protected String execute(String url) throws Exception {
        return execute(url, true);
    }

    protected String executeWithRuntimeException(String url) {
        try {
            return execute(url);
        } catch (Exception e) {
            throw new RuntimeException("Original exception '" + e.getClass().getName() + "' wrapped under RuntimeException", e);
        }
    }

    protected String execute(String url, boolean expectResponse) throws Exception {
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = httpclient.execute(httpget, responseHandler);

            if (expectResponse) {
                Assert.assertTrue("Invalid response! Expected to start with 'finished' but it's: "
                    + response, response.trim().startsWith("finished"));
                return response;
            }
        } catch (IOException e) {
            if (expectResponse) {
                throw e;
            }
        }
        return null;
    }

    protected String executeWithRuntimeException(String url, boolean expectResponse) {
        try {
            return execute(url, expectResponse);
        } catch (Exception e) {
            throw new RuntimeException("Original exception '" + e.getClass().getName() + "' wrapped under RuntimeException", e);
        }
    }
}