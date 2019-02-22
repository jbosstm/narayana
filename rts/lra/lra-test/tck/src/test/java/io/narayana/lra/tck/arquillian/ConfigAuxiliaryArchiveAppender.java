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

package io.narayana.lra.tck.arquillian;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * The appender provides way to bundle LRA interfaces implementation classes to the deployment
 * as the Swarm container does not bundle the LRA classes to the fat jar until
 * there is no fraction for it.
 */
public class ConfigAuxiliaryArchiveAppender implements AuxiliaryArchiveAppender {

    @Override
    public Archive<?> createAuxiliaryArchive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
        // adding LRA spec interfaces under the WildFly Swarm deployment
                .addPackages(true, org.eclipse.microprofile.lra.annotation.LRA.class.getPackage())
                .addPackages(true, org.eclipse.microprofile.lra.client.LRAClient.class.getPackage())
                .addPackages(true, org.eclipse.microprofile.lra.participant.LRAParticipant.class.getPackage());
        // adding Narayana LRA implementation under the WildFly Swarm deployment
        archive.addPackages(true, io.narayana.lra.client.NarayanaLRAClient.class.getPackage())
                .addPackages(true, io.narayana.lra.Current.class.getPackage());

        // adding Narayana LRA filters under the WildFly Swarm deployment
        String filtersAsset = String.format("%s%n%s",
                io.narayana.lra.filter.ClientLRAResponseFilter.class.getName(),
                io.narayana.lra.filter.ClientLRARequestFilter.class.getName());
        archive.addPackages(true, io.narayana.lra.filter.ClientLRARequestFilter.class.getPackage())
               .addAsResource(new StringAsset(filtersAsset), "META-INF/services/javax.ws.rs.ext.Providers")
               .addAsResource(new StringAsset("org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder"),
                    "META-INF/services/javax.ws.rs.client.ClientBuilder");

        // adding LRA TCK implementation of interfaces
        archive.addPackages(false, io.narayana.lra.tck.LRATckInfo.class.getPackage());

        return archive;
    }

}
