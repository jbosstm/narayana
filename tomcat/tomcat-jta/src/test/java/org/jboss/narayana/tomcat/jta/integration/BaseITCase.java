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

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.narayana.tomcat.jta.integration.app.TestApplication;
import org.jboss.narayana.tomcat.jta.integration.app.TestExecutor;
import org.jboss.narayana.tomcat.jta.integration.app.TestXAResource;
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
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static org.jboss.narayana.tomcat.jta.integration.app.TestExecutor.BASE_PATH;
import static org.jboss.narayana.tomcat.jta.integration.app.TestExecutor.JNDI_TEST;
import static org.jboss.narayana.tomcat.jta.integration.app.TestExecutor.RECOVERY_TEST;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author <a href="mailto:karm@redhat.com">Michal Karm Babacek</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
public class BaseITCase extends AbstractCase {

    private static final Logger LOGGER = Logger.getLogger(BaseITCase.class.getName());

    private static final String DEPLOYMENT_NAME = "test";

    private static final String EXECUTOR_URL = "http://localhost:8080/" + DEPLOYMENT_NAME + "/" + BASE_PATH + "/";

    private static final String NARAYANA_DEPENDENCY = "org.jboss.narayana.tomcat:tomcat-jta:"
            + System.getProperty("version.org.jboss.narayana");

    private static final String RESTEASY_DEPENDENCY = "org.jboss.resteasy:resteasy-servlet-initializer:"
            + System.getProperty("version.org.jboss.resteasy");

    private Client client;

    @ArquillianResource
    private Deployer deployer;

    @ArquillianResource
    private ContainerController controller;

    /**
     * Prepares test web app for deployment, including Narayana and RestEasy.
     * The context.xml is tailored for a specific database data source as needed in @BeforeClass.
     * A specific driver for the particular database is added in @BeforeClass.
     *
     * @return war deployment for Tomcat
     */
    @Deployment(name = "Basic-app", managed = false)
    public static WebArchive getDeployment() {
        final File[] libraries = Maven.resolver().resolve(NARAYANA_DEPENDENCY, RESTEASY_DEPENDENCY).withTransitivity().asFile();
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
                .addClasses(TestApplication.class, TestExecutor.class, TestXAResource.class)
                .addAsLibraries(libraries)
                .addAsResource("jbossts-properties.xml", "jbossts-properties.xml")
                .addAsWebInfResource("web.xml", "web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        LOGGER.info(archive.toString(true));
        webArchive = archive;
        return archive;
    }

    @Before
    public void before() {
        // Clean Object store and stray deployments
        try {
            FileUtils.deleteDirectory(Paths.get(catalinaHome, "work", "Catalina", "localhost", "test", "ObjectStore").toFile());
            FileUtils.deleteDirectory(Paths.get(catalinaHome, "work", "Narayana", "ObjectStore").toFile());
            FileUtils.deleteDirectory(Paths.get(catalinaHome, "bin", "ObjectStore").toFile());
            FileUtils.deleteDirectory(Paths.get(catalinaHome, "webapps", "test").toFile());
            FileUtils.deleteQuietly(Paths.get(catalinaHome, "webapps", "test.war").toFile());
        } catch (IOException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete stray ObjectStore(s).", e);
        }
        controller.start("tomcat");
        client = ClientBuilder.newClient();
        deployer.deploy("Basic-app");
    }

    @After
    public void after() {
        client.close();
        deployer.undeploy("Basic-app");
        controller.stop("tomcat");
        assertTrue("Failed to clean DB, check logs for the root cause.", dba.cleanDB(db));
    }

    @Test
    public void testJndi() {
        test(EXECUTOR_URL + JNDI_TEST);
    }

    @Test
    public void testRecovery() {
        IntStream.range(0, 10).forEach(i -> test(EXECUTOR_URL + RECOVERY_TEST));
    }

    private void test(final String url) {
        final Response response = client.target(url).request().get();
        if (response.getStatus() == 500) {
            fail(response.readEntity(String.class));
        } else if (response.getStatus() != 204) {
            fail(String.format("Unexpected test status: %d", response.getStatus()));
        }
    }
}
