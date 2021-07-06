/*
 * Copyright Red Hat
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.arquillian.deployment;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
//import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * Arquillian extension to generate a Deployment scenario to be used
 * uniquely in a Wildfly container.
 */
public class WildflyLRACoordinatorDeployment implements Deployment<WebArchive> {

    private final String DEFAULT_DEPLOYMENT_QUALIFIER = "lra-coordinator";
    private final Logger log = Logger.getLogger(WildflyLRACoordinatorDeployment.class);
    private final String ManifestMF = "Dependencies: org.jboss.jandex, org.jboss.jts export services, org.jboss.logging\n";

    /**
     * This method produces a lra-coordinator {@link WebArchive}. In the Arquillian
     * deployment process, this WebArchive can be used to deploy the lra-coordinator
     * as web service.
     *
     * @return {@link WebArchive} to deploy the lra-coordinator module as a web service.
     */
    @Override
    public Archive<WebArchive> create(String deploymentName) {

        // Checks if deploymentName is not defined
        if (deploymentName == null || deploymentName.isEmpty()) {
            deploymentName = DEFAULT_DEPLOYMENT_QUALIFIER;
        }

        String eclipseLraVersion = System.getProperty("version.microprofile.lra");
        String projectVersion = System.getProperty("project.version");

        // Creates the WAR archive
        WebArchive war = ShrinkWrap.create(WebArchive.class, deploymentName + ".war")
                // Support libraries
                .addAsLibraries(Maven.resolver()
                        .resolve("org.eclipse.microprofile.lra:microprofile-lra-api:" + eclipseLraVersion,
                                "org.eclipse.microprofile.lra:microprofile-lra-tck:" + eclipseLraVersion)
                        .withoutTransitivity().asFile())
                // Support libraries from the local store of Maven
                .addAsLibraries(Maven.configureResolver()
                        .workOffline()
                        .withMavenCentralRepo(false)
                        .withClassPathResolution(true)
                        .resolve("org.jboss.narayana.rts:lra-coordinator-jar:" + projectVersion,
                                "org.jboss.narayana.rts:lra-proxy-api:" + projectVersion,
                                "org.jboss.narayana.rts:narayana-lra:" + projectVersion,
                                "org.jboss.narayana.rts:lra-client:" + projectVersion,
                                "org.jboss.narayana.rts:lra-service-base:" + projectVersion)
                        .withoutTransitivity().asFile())
                // Adds a manifest to activate jts and logging submodules of Wildfly
                // TODO: try without "export services"
                .addAsManifestResource(new StringAsset(ManifestMF),"MANIFEST.MF")
                // Adds an empty beans.xml to activate the bean in discovery-mode set to "all"
                .addAsWebInfResource(EmptyAsset.INSTANCE,"beans.xml");

        if(log.isDebugEnabled()) {
            log.debugf("Content of the LRA Coordinator WAR is:%n%s%n", war.toString(Formatters.VERBOSE));
        }

        return war;
    }
}
