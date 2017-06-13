/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;

import org.jboss.narayana.rts.lra.coordinator.api.Coordinator;
import org.jboss.narayana.rts.lra.client.LRAClient;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LRAIT {

    @Deployment(testable = false)
    public static WebArchive createDeploymentFromFiles() {
        return ShrinkWrap.create(WebArchive.class).merge(
                ShrinkWrap.create(GenericArchive.class)
                        .as(ExplodedImporter.class)
                        .importDirectory("target/lra-coordinator")
                        .as(GenericArchive.class), "/", Filters.includeAll());
    }

//    @Deployment(testable = false)
    // this one does not register Coordinator.class as a JAXRS resource
    public static WebArchive createDeploymentFromUberJar() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/lra-coordinator-swarm.jar")).addClass(Coordinator.class);
    }

    @ArquillianResource
    private URL base;

    private LRAClient lraClient;

    @Before
    public void setupClass() throws MalformedURLException, URISyntaxException {
        lraClient = new LRAClient(base.getProtocol(), base.getHost(), base.getPort());
    }
}
