/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
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

import io.narayana.lra.arquillian.spi.NarayanaLRARecovery;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * The appender provides way to bundle LRA interfaces implementation classes to the deployment.
 * <ul>
 *     <li>The Thorntail container does not bundle the LRA classes to the fat jar until there is no fraction for it.</li>
 *     <li>The WildFly container bundles nothing which is not explicitly part of the war file (until WFLY extension is created)</li>
 * </ul>
 */
public class ConfigAuxiliaryArchiveAppender implements AuxiliaryArchiveAppender {

    // manifest for WildFly deployment, it requires access to some other WildFly internal modules
    final String ManifestMF = "Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.jandex, org.jboss.logging, org.jboss.modules\n";

    @Override
    public Archive<?> createAuxiliaryArchive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            // adding LRA spec interfaces under the client test deployment
            .addPackages(true, org.eclipse.microprofile.lra.annotation.Compensate.class.getPackage())
            // adding whole Narayana LRA implementation under the client test deployment
            .addPackages(true, io.narayana.lra.LRAConstants.class.getPackage())
             // registration of LRACDIExtension as Weld extension to be booted-up
            .addAsResource("META-INF/services/javax.enterprise.inject.spi.Extension")
            .addClass(org.jboss.weld.exceptions.DefinitionException.class)
             // explicitly define to work with annotated beans
            .addAsManifestResource(new StringAsset("<beans version=\"1.1\" bean-discovery-mode=\"annotated\"></beans>"), "beans.xml")
             // for WildFly we need dependencies to be part of the deployment's class path
            .addAsManifestResource(new StringAsset(ManifestMF), "MANIFEST.MF");

        // adding Narayana LRA filters under the client test deployment
        String filtersAsset = String.format("%s%n%s",
            io.narayana.lra.filter.ClientLRAResponseFilter.class.getName(),
            io.narayana.lra.filter.ClientLRARequestFilter.class.getName());
        archive.addPackages(true, io.narayana.lra.filter.ClientLRARequestFilter.class.getPackage())
            .addAsResource(new StringAsset(filtersAsset), "META-INF/services/javax.ws.rs.ext.Providers")
            .addAsResource(new StringAsset("org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder"),
                "META-INF/services/javax.ws.rs.client.ClientBuilder");

        // adding TCK required SPI implementations
        archive.addPackage(NarayanaLRARecovery.class.getPackage());
        archive.addAsResource(new StringAsset("io.narayana.lra.arquillian.spi.NarayanaLRARecovery"),
            "META-INF/services/org.eclipse.microprofile.lra.tck.service.spi.LRARecoveryService");

        return archive;
    }

}
