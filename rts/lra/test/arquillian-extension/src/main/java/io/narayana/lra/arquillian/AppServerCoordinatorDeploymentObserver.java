/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2020, Red Hat, Inc., and individual contributors
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

package io.narayana.lra.arquillian;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AppServerCoordinatorDeploymentObserver {
    private static final Logger log = Logger.getLogger(AppServerCoordinatorDeploymentObserver.class);

    /**
     * When the container name {@link Container#getName()} contains this string
     * then this observer prepares the 'war' deployment of the LRA Coordinator and serve it to the container.
     */
    private static final String CONTAINER_NAME_RECOGNITION = "as-coordinator";
    private static final String LRA_COORDINATOR_DEPLOYMENT_NAME = "lra-coordinator";
    private static final Map<String,Archive<?>> deployments = new ConcurrentHashMap<>();

    /**
     * The goal of this method is to create a WAR deployment of the LRA coordinator
     * which is deployed when the app server starts.
     */
    public void handleAfterStartup(@Observes AfterStart event, Container container) throws Exception {
        if(!container.getName().contains(CONTAINER_NAME_RECOGNITION)) {
            log.debugf("Handling before after start-up event for container '%s'. The container name does not contain substring '%s' " +
                            "thus skipping execution.", container.getName(), CONTAINER_NAME_RECOGNITION);
            return;
        }

        log.debugf("handleAfterStartup for container %s", container.getName());
        Archive<?> deployment = createLRACoordinatorDeployment();
        if(deployments.put(deployment.getName(), deployment) == null) {
            log.infof("Deploying LRA Coordinator war deployment: %s", deployment.getName());
            container.getDeployableContainer()
                    .deploy(deployment);
        }
    }

    /**
     * This method undeploys the LRA coordinator deployed by method
     * {@link #handleAfterStartup(AfterStart, Container)}.
     */
    public void handleBeforeStop(@Observes BeforeStop event, Container container) throws Exception {
        if(!container.getName().contains(CONTAINER_NAME_RECOGNITION)) {
            log.debugf("Handling before stop event for container '%s'. The container name does not contain substring '%s' " +
                    "thus skipping execution.", container.getName(), CONTAINER_NAME_RECOGNITION);
            return;
        }

        log.debugf("handleBeforeStop for container %s", container.getName());
        for(Archive<?> deployment: deployments.values()) {
            log.infof("Undeploying LRA Coordinator war deployment: %s", deployment.getName());
            container.getDeployableContainer().undeploy(deployment);
        }
    }

    public static WebArchive createLRACoordinatorDeployment() {
        // LRA uses ArjunaCore - for WildFly we need to pull the org.jboss.jts module to get it on the classpath
        final String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.jts, org.jboss.logging\n";

        String mavenProjectVersion = System.getProperty("project.version");

        File[] files = Maven.resolver()
                .resolve("org.jboss.narayana.rts:lra-coordinator-war:war:" + mavenProjectVersion)
                .withTransitivity().asFile();

        ZipImporter zip = ShrinkWrap.create(ZipImporter.class, LRA_COORDINATOR_DEPLOYMENT_NAME + ".war");
        for(File file: files) {
            zip.importFrom(file);
        }
        WebArchive war = zip.as(WebArchive.class);

        if(log.isDebugEnabled()) {
            log.debugf("Content of the LRA Coordinator deployment is:%n%s%n", war.toString(true));
        }
        return war;
    }
}