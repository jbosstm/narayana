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

package org.jboss.narayana.txframework.functional.ws.ba.bridged;

import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.common.URLUtils;
import org.jboss.narayana.txframework.functional.common.EventLog;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(Arquillian.class)
public class BABridgedTest {

    UserBusinessActivity uba;
    BABridged client;

    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(false, BABridgedTest.class.getPackage())
                .addPackage(EventLog.class.getPackage())
                .addClass(URLUtils.class)
                .addClass(ParticipantCompletionCoordinatorRules.class)
                .addAsWebInfResource(new File("persistence.xml"), "classes/META-INF/persistence.xml")
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");

        String ManifestMF = "Dependencies: org.jboss.xts, org.jboss.narayana.txframework services";
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
    public void setupTest() throws Exception {

        uba = UserBusinessActivityFactory.userBusinessActivity();
        client = BABridgedClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception {

        client.reset();
        cancelIfActive(uba);
    }

    @Test
    public void testClose() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(1);

        uba.begin();
        client.incrementCounter(2);
        Assert.assertEquals(2, client.getCounter());
        uba.close();
        Assert.assertEquals(2, client.getCounter());
        Assert.assertTrue(client.isConfirmed());
    }

    @Test
    public void testCompensate() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(1);

        uba.begin();
        client.incrementCounter(2);
        Assert.assertEquals(2, client.getCounter());
        uba.cancel();
        Assert.assertEquals(0, client.getCounter());
        Assert.assertFalse(client.isConfirmed());
    }

    public void cancelIfActive(UserBusinessActivity uba) {

        try {
            uba.cancel();
        } catch (Throwable e) {
            // do nothing, not active
        }
    }
}
