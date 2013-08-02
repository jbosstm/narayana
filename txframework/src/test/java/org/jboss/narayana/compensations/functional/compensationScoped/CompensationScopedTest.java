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

package org.jboss.narayana.compensations.functional.compensationScoped;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.compensations.functional.common.DataCompensationHandler;
import org.jboss.narayana.compensations.functional.common.DataConfirmationHandler;
import org.jboss.narayana.compensations.functional.common.DataTxLoggedHandler;
import org.jboss.narayana.compensations.functional.common.DummyData;
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

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;


/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
@RunWith(Arquillian.class)
public class CompensationScopedTest {

    @Inject
    Service service;

    @Inject
    DummyData dummyData;

    UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();
    BusinessActivityManager bam = BusinessActivityManagerFactory.businessActivityManager();


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

        DataConfirmationHandler.reset();
        DataCompensationHandler.reset();
        DataTxLoggedHandler.reset();

        MyCompensationHandler.dataAvailable = false;
        MyConfirmationHandler.dataAvailable = false;
        MyTransactionLoggedHandler.dataAvailable = false;
    }

    @Test
    public void testSimple() throws Exception {

        uba.begin();

        dummyData.setValue("1");
        Assert.assertEquals("1", dummyData.getValue());

        uba.close();
    }

    @Test
    public void contextNotActiveTest() throws Exception {

        assertContextUnavailable();
    }

    @Test
    public void testScopeDestroy() throws Exception {

        assertContextUnavailable();

        uba.begin();
        dummyData.setValue("1");
        Assert.assertEquals("1", dummyData.getValue());
        uba.close();

        assertContextUnavailable();
    }

    @Test
    public void testSuspendResume() throws Exception {

        assertContextUnavailable();

        uba.begin();
        dummyData.setValue("1");
        Assert.assertEquals("1", dummyData.getValue());
        TxContext txContext1 = bam.suspend();

        assertContextUnavailable();

        uba.begin();
        dummyData.setValue("2");
        Assert.assertEquals("2", dummyData.getValue());
        TxContext txContext2 = bam.suspend();

        assertContextUnavailable();

        bam.resume(txContext1);
        Assert.assertEquals("1", dummyData.getValue());
        uba.close();

        assertContextUnavailable();

        bam.resume(txContext2);
        Assert.assertEquals("2", dummyData.getValue());
        uba.close();

        assertContextUnavailable();
    }

    private void assertContextUnavailable() {

        try {
            dummyData.getValue();
            Assert.fail("Context should not be active here");
        } catch (ContextNotActiveException e) {
            //expected
        }
    }


    @Test
    public void testCompensationHandler() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(1);

        MyCompensationHandler.expectedData = "blah";
        MyConfirmationHandler.expectedData = "blah";
        MyTransactionLoggedHandler.expectedData = "blah";

        uba.begin();
        service.doWork("blah");
        uba.cancel();

        Assert.assertEquals(true, MyCompensationHandler.dataAvailable);
        Assert.assertEquals(false, MyConfirmationHandler.dataAvailable);
        Assert.assertEquals(true, MyTransactionLoggedHandler.dataAvailable);
    }


    @Test
    public void testConfirmationHandler() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(1);

        MyCompensationHandler.expectedData = "blah";
        MyConfirmationHandler.expectedData = "blah";
        MyTransactionLoggedHandler.expectedData = "blah";

        uba.begin();
        service.doWork("blah");
        uba.close();

        Assert.assertEquals(false, MyCompensationHandler.dataAvailable);
        Assert.assertEquals(true, MyConfirmationHandler.dataAvailable);
        Assert.assertEquals(true, MyTransactionLoggedHandler.dataAvailable);
    }

}
