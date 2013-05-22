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

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.compensations.functional.common.DummyData;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
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
    DummyData dummyData;

    UserBusinessActivity uba = UserBusinessActivityFactory.userBusinessActivity();
    BusinessActivityManager bam = BusinessActivityManagerFactory.businessActivityManager();

    @Deployment
    public static JavaArchive createTestArchive() {

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, "org.jboss.narayana.compensations.functional")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource("META-INF/services/javax.enterprise.inject.spi.Extension", "services/javax.enterprise.inject.spi.Extension");

        archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.narayana.txframework,org.jboss.xts\n";

        archive.setManifest(new StringAsset(ManifestMF));

        return archive;
    }

    @After
    public void tearDown() {

        try {
            uba.close();
        } catch (Exception e) {
            // do nothing
        }
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


}