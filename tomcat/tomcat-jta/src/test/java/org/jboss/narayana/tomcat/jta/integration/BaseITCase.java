/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.tomcat.jta.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import java.io.File;
import java.util.Arrays;

import static org.jboss.narayana.tomcat.jta.integration.TestExecutor.BASE_PATH;
import static org.jboss.narayana.tomcat.jta.integration.TestExecutor.JNDI_TEST;
import static org.jboss.narayana.tomcat.jta.integration.TestExecutor.RECOVERY_TEST;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
public class BaseITCase {

    private static final String DEPLOYMENT_NAME = "test";

    private static final String EXECUTOR_URL = "http://localhost:8080/" + DEPLOYMENT_NAME + "/" + BASE_PATH + "/";

    private static final String NARAYANA_DEPENDENCY = "org.jboss.narayana.tomcat:tomcat-jta:"
            + System.getProperty("version.org.jboss.narayana");

    private static final String RESTEASY_DEPENDENCY = "org.jboss.resteasy:resteasy-servlet-initializer:"
            + System.getProperty("version.org.jboss.resteasy");

    private static final String H2_DEPENDENCY = "com.h2database:h2:" + System.getProperty("version.com.h2database");

    private Client client;

    @Deployment
    public static WebArchive getDeployment() {
        File[] libraries = Maven.resolver().resolve(NARAYANA_DEPENDENCY, RESTEASY_DEPENDENCY, H2_DEPENDENCY).withTransitivity()
                .asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
                .addClasses(TestApplication.class, TestExecutor.class, TestXAResource.class)
                .addAsLibraries(libraries)
                .addAsManifestResource("context.xml", "context.xml")
                .addAsResource("jbossts-properties.xml", "jbossts-properties.xml")
                .addAsWebInfResource("web.xml", "web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Before
    public void before() {
        File objectStore = new File(System.getenv("CATALINA_HOME") + File.separator + "bin" + File.separator + "ObjectStore");
        assertTrue("Failed to clear object store", deleteDirectory(objectStore));
        client = ClientBuilder.newClient();
    }

    @After
    public void after() {
        client.close();
    }

    @Test
    public void testJndi() {
        test(EXECUTOR_URL + JNDI_TEST);
    }

    @Test
    public void testRecovery() {
        test(EXECUTOR_URL + RECOVERY_TEST);
    }

    private void test(String url) {
        Response response = client.target(url).request().get();
        if (response.getStatus() == 500) {
            fail(response.readEntity(String.class));
        } else if (response.getStatus() != 204) {
            fail("Unexpected test status: " + response.getStatus());
        }
    }

    private boolean deleteDirectory(File path) {
        if (!path.exists()) {
            return true;
        }

        return Arrays.stream(path.listFiles()).map(file -> file.isDirectory() ? deleteDirectory(file) : file.delete())
                .allMatch(result -> result);
    }

}
