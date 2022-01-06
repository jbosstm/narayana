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

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class Deployer {

    public static WebArchive deploy(String appName, Class... participants) {
        // manifest for WildFly deployment
        final String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.jandex, org.jboss.logging\n";

        return ShrinkWrap.create(WebArchive.class, appName + ".war")
                .addPackages(true,
                        "org.eclipse.microprofile.lra",
                        "io.narayana.lra.client.internal.proxy")
                .addPackages(false,
                        "io.narayana.lra",
                        "io.narayana.lra.logging",
                        "io.narayana.lra.filter",
                        "io.narayana.lra.provider",
                        "io.narayana.lra.client",
                        "io.narayana.lra.arquillian.spi")
                // adds the TestBase class, the test class itself seems to be uploaded during deployment by Arquillian
                // then it requires the parent class as well, otherwise Weld NoClassDefFoundError is shown
                .addClass(TestBase.class)
                // adds the lra-participant wanted
                .addClasses(participants)
                // activates Wildfly modules
                .addAsManifestResource(new StringAsset(ManifestMF), "MANIFEST.MF")
                // activates the bean and explicitly specifies to work with annotated classes
                .addAsWebInfResource(new StringAsset("<beans version=\"1.1\" bean-discovery-mode=\"all\"></beans>"), "beans.xml")
                .addAsResource(new StringAsset("org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder"),
                    "META-INF/services/jakarta.ws.rs.client.ClientBuilder");
    }
}
