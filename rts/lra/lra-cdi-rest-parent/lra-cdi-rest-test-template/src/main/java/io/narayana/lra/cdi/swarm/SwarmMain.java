/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package io.narayana.lra.cdi.swarm;

import java.io.File;
import java.io.IOException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.UnknownExtensionTypeException;
import org.jboss.shrinkwrap.api.importer.ArchiveImportException;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.cdi.CDIFraction;
import org.wildfly.swarm.jaxrs.JAXRSFraction;
import org.wildfly.swarm.logging.LoggingFraction;

/**
 * <p>
 * A Swarm main class which provides dependencies needed for testing
 * <code>LraAnnotationProcessingExtension</code>.
 * The main functionality of the class is providing capability to define
 * a deployment for the WildFly Swarm.
 * <p>
 * This is only for the testing purposes.
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
public class SwarmMain {
    private static final String DEFAULT_FILENAME = "target/test.war";

    public static void main(String... args) {
        for(String str: args) {
            System.out.println(" ==> " + str);
        }
        String fileName = System.getProperty("file", DEFAULT_FILENAME);

        try {
            final Swarm swarm = new Swarm()
                    .fraction(new JAXRSFraction())
                    .fraction(new CDIFraction())
                    .fraction(new LoggingFraction());

            WebArchive deployment = ShrinkWrap.createFromZipFile(WebArchive.class, new File(fileName));

            swarm.start();
            swarm.deploy(deployment);
        } catch (ArchiveImportException | IllegalArgumentException | UnknownExtensionTypeException | IOException e) {
            throw new RuntimeException("Can't create deployment from file " + fileName, e);
        } catch (Exception ee) {
            throw new RuntimeException("Error on running swarm container", ee);
        }

    }
}
