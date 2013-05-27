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

package org.jboss.narayana.compensations.functional.dataMap;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.compensations.functional.common.DataCompensationHandler;
import org.jboss.narayana.compensations.functional.common.DataConfirmationHandler;
import org.jboss.narayana.compensations.functional.common.DataTxLoggedHandler;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;


/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
@RunWith(Arquillian.class)
public class DataMapTest {

    @Inject
    Service service;

    UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();

    @Deployment
    public static JavaArchive createTestArchive() {

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, "org.jboss.narayana.compensations.functional")
                .addClass(ParticipantCompletionCoordinatorRules.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource("META-INF/services/javax.enterprise.inject.spi.Extension", "services/javax.enterprise.inject.spi.Extension");

        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.narayana.txframework,org.jboss.xts\n";

        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }


    @BeforeClass()
    public static void submitBytemanScript() throws Exception {

        BMScript.submit(ParticipantCompletionCoordinatorRules.RESOURCE_PATH);
    }

    @AfterClass()
    public static void removeBytemanScript() {

        BMScript.remove(ParticipantCompletionCoordinatorRules.RESOURCE_PATH);
    }


    @After
    public void tearDown() {

        try {
            uba.close();
        } catch (Exception e) {
            // do nothing
        }
    }


    @Before
    public void resetParticipants() {

        DataConfirmationHandler.reset();
    }


    @Test
    public void testSimple() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        service.doWork();
        Assert.assertEquals(true, DataConfirmationHandler.getDataNotNull());
        Assert.assertEquals(true, DataConfirmationHandler.getDataAvailable());

        Assert.assertEquals(false, DataCompensationHandler.getDataNotNull());
        Assert.assertEquals(false, DataCompensationHandler.getDataAvailable());

        Assert.assertEquals(true, DataTxLoggedHandler.getDataNotNull());
        Assert.assertEquals(true, DataTxLoggedHandler.getDataAvailable());
    }

    @Test
    public void testCompensate() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        uba.begin();
        service.doWork();
        uba.cancel();

        Assert.assertEquals(false, DataConfirmationHandler.getDataNotNull());
        Assert.assertEquals(false, DataConfirmationHandler.getDataAvailable());

        Assert.assertEquals(true, DataCompensationHandler.getDataNotNull());
        Assert.assertEquals(true, DataCompensationHandler.getDataAvailable());

        Assert.assertEquals(true, DataTxLoggedHandler.getDataNotNull());
        Assert.assertEquals(true, DataTxLoggedHandler.getDataAvailable());
    }


}
