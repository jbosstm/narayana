/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.functional.ws.ba.coordinatorCompletion;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.wst.TransactionRolledBackException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.common.URLUtils;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Cancel;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Close;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Complete;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.ConfirmCompleted;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.narayana.txframework.functional.common.SomeApplicationException;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static org.jboss.narayana.txframework.functional.common.ServiceCommand.CANNOT_COMPLETE;
import static org.jboss.narayana.txframework.functional.common.ServiceCommand.THROW_APPLICATION_EXCEPTION;

@RunWith(Arquillian.class)
public class BACoordinatorCompletionTest {

    @Deployment()
    public static JavaArchive createTestArchive() {
        //todo: Does the application developer have to specify the interceptor?
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(false, BACoordinatorCompletionTest.class.getPackage())
                .addPackage(EventLog.class.getPackage())
                .addAsManifestResource("persistence.xml")
                .addClass(ParticipantCompletionCoordinatorRules.class)
                .addClass(URLUtils.class)
                .addAsManifestResource(EmptyAsset.INSTANCE,
                        ArchivePaths.create("beans.xml"));

        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.xts, org.jboss.narayana.txframework\n";


        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }


    UserBusinessActivity uba;
    BACoordinatorCompletion client;

    @Before
    public void setupTest() throws Exception {

        uba = UserBusinessActivityFactory.userBusinessActivity();
        client = BACoordinatorCompletionClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception {

        assertDataAvailable();
        client.clearEventLog();
        cancelIfActive(uba);
    }

    @Test
    public void testSimple() throws Exception {

        uba.begin();
        client.saveData();
        uba.close();

        assertOrder(Complete.class, ConfirmCompleted.class, Close.class);
    }

    @Test
    public void testMultiInvoke() throws Exception {

        uba.begin();
        client.saveData();
        client.saveData();
        uba.close();

        assertOrder(Complete.class, ConfirmCompleted.class, Close.class);
    }

    @Test
    public void testClientDrivenCancel() throws Exception {

        uba.begin();
        client.saveData();
        uba.cancel();

        assertOrder(Cancel.class);
    }

    @Test
    public void testApplicationException() throws Exception {

        try {
            uba.begin();
            client.saveData(THROW_APPLICATION_EXCEPTION);
            Assert.fail("Exception should have been thrown by now");
        } catch (SomeApplicationException e) {
            //Exception expected
        } finally {
            uba.cancel();
        }
        assertOrder();
    }

    @Test(expected = TransactionRolledBackException.class)
    public void testCannotComplete() throws Exception {

        uba.begin();
        client.saveData(CANNOT_COMPLETE);
        uba.close();

        assertOrder();
    }

    private void assertOrder(Class<? extends Annotation>... expectedOrder) {

        org.junit.Assert.assertEquals(Arrays.asList(expectedOrder), client.getEventLog().getEventLog());
    }

    private void assertDataAvailable() {

        List<Class<? extends Annotation>> log = client.getEventLog().getDataUnavailableLog();
        if (!log.isEmpty()) {
            org.junit.Assert.fail("One or more lifecycle methods could not access the managed data: " + log.toString());
        }
    }

    public void cancelIfActive(UserBusinessActivity uba) {

        try {
            uba.cancel();
        } catch (Throwable e) {
            // do nothing, not active
        }
    }
}
