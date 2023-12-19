/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.arquillian;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class Deployer {

    public static WebArchive createDeployment(String appName, Class<?>... classes) {

        String resteasyClientVersion = System.getProperty("version.resteasy-client");
        String eclipseLraVersion = System.getProperty("version.microprofile.lra");
        String projectVersion = System.getProperty("project.version");

        return ShrinkWrap.create(WebArchive.class, appName + ".war")

                // Additional Services to deploy
                .addClasses(classes)
                // Support libraries
                .addAsLibraries(Maven.resolver()
                        .resolve("org.jboss.resteasy:resteasy-client:" + resteasyClientVersion,
                                "org.eclipse.microprofile.lra:microprofile-lra-api:" + eclipseLraVersion,
                                "org.eclipse.microprofile.lra:microprofile-lra-tck:" + eclipseLraVersion)
                        .withoutTransitivity().asFile())
                // Support libraries from the local store of Maven
                .addAsLibraries(Maven.configureResolver()
                                .workOffline()
                                .withMavenCentralRepo(false)
                                .withClassPathResolution(true)
                                .resolve("org.jboss.narayana.rts:lra-service-base:" + projectVersion,
                                "org.jboss.narayana.rts:lra-proxy-api:" + projectVersion,
                                "org.jboss.narayana.rts:lra-client:" + projectVersion,
                                "org.jboss.narayana.rts:narayana-lra:" + projectVersion)
                        .withoutTransitivity().asFile())
                // Adds a manifest to activate jts and logging submodules of Wildfly
                .addAsManifestResource(
                        new StringAsset("Dependencies: org.jboss.jandex, org.jboss.logging\n"),
                        "MANIFEST.MF")
                // Adds an empty beans.xml to activate the bean in discovery-mode set to "all"
                .addAsWebInfResource(EmptyAsset.INSTANCE,"beans.xml");
    }
}
