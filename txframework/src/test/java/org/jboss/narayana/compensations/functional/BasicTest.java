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

package org.jboss.narayana.compensations.functional;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.compensations.api.NoTransactionException;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler2;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler3;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler2;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler3;
import org.jboss.narayana.compensations.functional.common.DummyTransactionLoggedHandler1;
import org.jboss.narayana.compensations.functional.common.DummyTransactionLoggedHandler2;
import org.jboss.narayana.compensations.functional.common.DummyTransactionLoggedHandler3;
import org.jboss.narayana.compensations.functional.common.MultiService;
import org.jboss.narayana.compensations.functional.common.MyRuntimeException;
import org.jboss.narayana.compensations.functional.common.SingleService;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
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
public class BasicTest {


    @Inject
    SingleService singleService;

    @Inject
    MultiService multiService;

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

    @Before
    public void resetParticipants() {

        DummyCompensationHandler1.reset();
        DummyConfirmationHandler1.reset();
        DummyTransactionLoggedHandler1.reset();
        DummyCompensationHandler2.reset();
        DummyConfirmationHandler2.reset();
        DummyTransactionLoggedHandler2.reset();
        DummyCompensationHandler3.reset();
        DummyConfirmationHandler3.reset();
        DummyTransactionLoggedHandler3.reset();
    }


    @Test
    public void testSimple() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        singleService.testSingle1(false);

        Assert.assertEquals(false, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(true, DummyConfirmationHandler1.getCalled());
        Assert.assertEquals(true, DummyTransactionLoggedHandler1.getCalled());
    }

    @Test
    public void testMulti() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        multiService.testsMulti(false);

        Assert.assertEquals(false, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(true, DummyConfirmationHandler1.getCalled());
        Assert.assertEquals(true, DummyTransactionLoggedHandler1.getCalled());

        Assert.assertEquals(false, DummyCompensationHandler2.getCalled());
        Assert.assertEquals(true, DummyConfirmationHandler2.getCalled());
        Assert.assertEquals(true, DummyTransactionLoggedHandler2.getCalled());
    }

    @Test
    public void testCompensation() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        try {
            multiService.testsMulti(true);
            Assert.fail();
        } catch (MyRuntimeException e) {
            //expected
        }

        Assert.assertEquals(true, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler1.getCalled());
        Assert.assertEquals(true, DummyTransactionLoggedHandler1.getCalled());

        Assert.assertEquals(true, DummyCompensationHandler2.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler2.getCalled());
        Assert.assertEquals(true, DummyTransactionLoggedHandler2.getCalled());
    }

    @Test
    public void testAlternative() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        multiService.testAlternative(false);

        Assert.assertEquals(false, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(true, DummyConfirmationHandler1.getCalled());
        Assert.assertEquals(true, DummyTransactionLoggedHandler1.getCalled());

        Assert.assertEquals(false, DummyCompensationHandler2.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler2.getCalled());
        Assert.assertEquals(false, DummyTransactionLoggedHandler2.getCalled());

        Assert.assertEquals(false, DummyCompensationHandler3.getCalled());
        Assert.assertEquals(true, DummyConfirmationHandler3.getCalled());
        Assert.assertEquals(true, DummyTransactionLoggedHandler3.getCalled());
    }

    @Test
    public void testAlternativeThenFail() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        try {
            multiService.testAlternative(true);
            Assert.fail();
        } catch (MyRuntimeException e) {
            //expected
        }

        Assert.assertEquals(true, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler1.getCalled());
        Assert.assertEquals(true, DummyTransactionLoggedHandler1.getCalled());

        Assert.assertEquals(false, DummyCompensationHandler2.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler2.getCalled());
        Assert.assertEquals(false, DummyTransactionLoggedHandler2.getCalled());

        Assert.assertEquals(true, DummyCompensationHandler3.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler3.getCalled());
        Assert.assertEquals(true, DummyTransactionLoggedHandler3.getCalled());
    }


    @Test
    public void testNoTransaction() throws Exception {

        try {
            singleService.noTransactionPresent();
            Assert.fail();
        } catch (NoTransactionException e) {
            //expected
        }

        Assert.assertEquals(false, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler1.getCalled());
        Assert.assertEquals(false, DummyTransactionLoggedHandler1.getCalled());
    }
}
