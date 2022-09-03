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

package org.jboss.narayana.compensations.functional.distributed;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.wst.TransactionRolledBackException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.common.URLUtils;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler1;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class DistributedTestRemote {

    UserBusinessActivity uba;

    @Inject
    TestService client;

    @Deployment()
    public static JavaArchive createTestArchive() {

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, DistributedTestRemote.class.getPackage())
                .addPackage(DummyCompensationHandler1.class.getPackage())
                .addClass(ParticipantCompletionCoordinatorRules.class)
                .addClass(URLUtils.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

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
    public void setupTest() throws Exception {

        uba = UserBusinessActivityFactory.userBusinessActivity();
    }

    @After
    public void teardownTest() throws Exception {

        client.resetHandlerFlags();
        cancelIfActive(uba);
    }

    @Test
    public void testSimple() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        client.saveData(false);

        Assert.assertEquals(true, client.wasTransactionConfirmedHandlerInvoked());
        Assert.assertEquals(false, client.wasCompensationHandlerInvoked());

    }


    @Test
    public void testClientDrivenCompensate() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        uba.begin();
        client.saveData(false);
        uba.cancel();

        Assert.assertEquals(false, client.wasTransactionConfirmedHandlerInvoked());
        Assert.assertEquals(true, client.wasCompensationHandlerInvoked());
    }


    @Test
    public void testRuntimeException() throws Exception {

        try {
            uba.begin();
            client.saveData(true);
            Assert.fail("Exception should have been thrown by now");
        } catch (RuntimeException e) {
            //Exception expected
        }

        uba.cancel();

        Assert.assertEquals(false, client.wasTransactionConfirmedHandlerInvoked());
        Assert.assertEquals(false, client.wasCompensationHandlerInvoked());
    }


    @Test
    @Ignore //JBTM-1715
    public void testRuntimeExceptionCancelOnFailure() throws Exception {

        try {
            uba.begin();
            client.saveDataCancelOnFailure(true);
            Assert.fail("Exception should have been thrown by now");
        } catch (RuntimeException e) {
            //Expected
        }

        try {
            uba.close();
            Assert.fail("TransactionRolledBackException should have been thrown by now");
        } catch (TransactionRolledBackException e) {
            //Expected
        }

        Assert.assertEquals(false, client.wasTransactionConfirmedHandlerInvoked());
        Assert.assertEquals(false, client.wasCompensationHandlerInvoked());
    }

    public void cancelIfActive(UserBusinessActivity uba) {

        try {
            uba.cancel();
        } catch (Throwable e) {
            // do nothing, not active
        }
    }
}
