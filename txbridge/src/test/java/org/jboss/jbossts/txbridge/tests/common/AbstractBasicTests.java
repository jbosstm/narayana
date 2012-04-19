/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.txbridge.tests.common;

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

import java.io.IOException;

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
                .addAsResource("inbound/jaxws-handlers-server.xml", "jaxws-handlers-server.xml")
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
                .addPackage("org.jboss.jbossts.txbridge.tests.outbound.service")
                .addPackage("org.jboss.jbossts.txbridge.tests.outbound.utility")
                .addAsResource("outbound/jaxws-handlers-server.xml", "jaxws-handlers-server.xml")
//                .addAsManifestResource("outbound/jboss-beans.xml", "jboss-beans.xml")
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.xts,org.jboss.jts\n"), "MANIFEST.MF");
        return archive;
    }

    protected static Archive<?> getOutboundClientArchive() {
        Archive<?> archive = ShrinkWrap.create(WebArchive.class, OUTBOUND_CLIENT_DEPLOYMENT_NAME + ".war")
                .addPackage("org.jboss.jbossts.txbridge.tests.outbound.client")
                .addAsManifestResource(new StringAsset("Dependencies: org.jboss.xts,org.jboss.jts\n"), "MANIFEST.MF");
        return archive;
    }


    @BeforeClass
    public static void beforeClass() throws Exception {
        if (instrumentor == null) {
            instrumentor = new Instrumentor(new Submit(), 1199);
        }
    }


    protected void execute(String url) throws Exception {
        execute(url, true);
    }

    protected void execute(String url, boolean expectResponse) throws Exception {
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = httpclient.execute(httpget, responseHandler);

            if (expectResponse) {
                Assert.assertEquals("Invalid response!", "finished", response.trim());
            }
        } catch (IOException e) {
            if (expectResponse) {
                throw e;
            }
        }
    }

}
