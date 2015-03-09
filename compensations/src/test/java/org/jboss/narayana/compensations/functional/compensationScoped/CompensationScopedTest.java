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

import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.compensations.functional.common.DummyData;
import org.jboss.narayana.compensations.impl.BAControler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;


/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public abstract class CompensationScopedTest {

    @Inject
    Service service;

    @Inject
    DummyData dummyData;

    abstract BAControler getBAControler();

    @After
    public void tearDown() {

        try {
            getBAControler().cancelBusinessActivity();
        } catch (Exception e) {
        }
    }

    @Before
    public void resetParticipants() {

        MyCompensationHandler.dataAvailable = false;
        MyConfirmationHandler.dataAvailable = false;
    }

    @Test
    public void testSimple() throws Exception {

        getBAControler().beginBusinessActivity();
        dummyData.setValue("1");
        getBAControler().closeBusinessActivity();
    }

    @Test
    public void contextNotActiveTest() throws Exception {

        assertContextUnavailable();
    }

    @Test
    public void testScopeDestroy() throws Exception {

        assertContextUnavailable();

        getBAControler().beginBusinessActivity();
        getBAControler().closeBusinessActivity();

        assertContextUnavailable();
    }

    @Test
    public void testSuspendResume() throws Exception {

        assertContextUnavailable();

        getBAControler().beginBusinessActivity();
        dummyData.setValue("1");
        Assert.assertEquals("1", dummyData.getValue());
        Object txContext1 = getBAControler().suspend();

        assertContextUnavailable();

        getBAControler().beginBusinessActivity();
        dummyData.setValue("2");
        Assert.assertEquals("2", dummyData.getValue());
        Object txContext2 = getBAControler().suspend();

        assertContextUnavailable();

        getBAControler().resume(txContext1);
        Assert.assertEquals("1", dummyData.getValue());
        getBAControler().closeBusinessActivity();

        assertContextUnavailable();

        getBAControler().resume(txContext2);
        Assert.assertEquals("2", dummyData.getValue());
        getBAControler().closeBusinessActivity();

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

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        MyCompensationHandler.expectedData = "blah";
        MyConfirmationHandler.expectedData = "blah";

        getBAControler().beginBusinessActivity();
        service.doWork("blah");
        getBAControler().cancelBusinessActivity();

        Assert.assertEquals(true, MyCompensationHandler.dataAvailable);
        Assert.assertEquals(false, MyConfirmationHandler.dataAvailable);
    }

    @Test
    public void testTwoCompensationHandlers() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        MyCompensationHandler.expectedData = "blah";
        MyConfirmationHandler.expectedData = "blah";

        getBAControler().beginBusinessActivity();
        service.doWork("blah");
        service.doWork("blah");
        getBAControler().cancelBusinessActivity();

        Assert.assertTrue(MyCompensationHandler.dataAvailable);
        Assert.assertFalse(MyConfirmationHandler.dataAvailable);
    }

    @Test
    public void testConfirmationHandler() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        MyCompensationHandler.expectedData = "blah";
        MyConfirmationHandler.expectedData = "blah";

        getBAControler().beginBusinessActivity();
        service.doWork("blah");
        getBAControler().closeBusinessActivity();

        Assert.assertEquals(false, MyCompensationHandler.dataAvailable);
        Assert.assertEquals(true, MyConfirmationHandler.dataAvailable);
    }

    @Test
    public void testTwoConfirmationHandlers() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        MyCompensationHandler.expectedData = "blah";
        MyConfirmationHandler.expectedData = "blah";

        getBAControler().beginBusinessActivity();
        service.doWork("blah");
        service.doWork("blah");
        getBAControler().closeBusinessActivity();

        Assert.assertFalse(MyCompensationHandler.dataAvailable);
        Assert.assertTrue(MyConfirmationHandler.dataAvailable);
    }

}
