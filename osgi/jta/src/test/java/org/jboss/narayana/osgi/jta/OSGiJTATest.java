/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
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
package org.jboss.narayana.osgi.jta;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.narayana.osgi.jta.internal.Activator;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.transaction.TransactionManager;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 */

@RunWith(Arquillian.class)
public class OSGiJTATest {
    @ArquillianResource
    BundleContext context;

    @Deployment
    public static JavaArchive createTestArchive() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar");
        archive.addClass(Activator.class);
        archive.addPackages(true, "com.arjuna");
        archive.addPackages(true, "org.jboss");
        archive.addAsResource("jbossts-properties.xml");

        archive.setManifest(new Asset() {
            public InputStream openStream() {
                OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
                builder.addBundleSymbolicName(archive.getName());
                builder.addBundleManifestVersion(2);

                builder.addImportPackages("javax.xml.parsers", "org.xml.sax", "org.xml.sax.helpers", "javax.naming.spi");
                builder.addBundleActivator(Activator.class.getName());
                builder.addManifestHeader("arjuna-properties-file", "jbossts-properties.xml");
                return builder.openStream();
            }
        });
        return archive;
    }

    @Test
    public  void testTransactionManager(@ArquillianResource Bundle bundle) throws Exception {
        assertEquals("System Bundle ID", 0, context.getBundle().getBundleId());

        bundle.start();
        assertEquals("Bundle ACTIVE", Bundle.ACTIVE, bundle.getState());

        BundleContext context = bundle.getBundleContext();
        assertNotNull("BundleContext available", context);

        ServiceReference<TransactionManager> serviceReference = context.getServiceReference(TransactionManager.class);
        assertNotNull("TransactionManager reference available", serviceReference);

        TransactionManager tm = context.getService(serviceReference);
        assertNotNull("TransactionManager service available", tm);

        tm.begin();
        tm.commit();

        bundle.stop();
        assertEquals("Bundle RESOLVED", Bundle.RESOLVED, bundle.getState());
    }

}
