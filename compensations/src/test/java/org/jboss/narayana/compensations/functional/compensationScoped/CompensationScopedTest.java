/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.compensationScoped;

import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.compensations.functional.common.DummyData;
import org.jboss.narayana.compensations.internal.BAController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;


/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public abstract class CompensationScopedTest {

    @Inject
    Service service;

    @Inject
    DummyData dummyData;

    abstract BAController getBAController();

    @After
    public void tearDown() {

        try {
            getBAController().cancelBusinessActivity();
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

        getBAController().beginBusinessActivity();
        dummyData.setValue("1");
        getBAController().closeBusinessActivity();
    }

    @Test
    public void contextNotActiveTest() throws Exception {

        assertContextUnavailable();
    }

    @Test
    public void testScopeDestroy() throws Exception {

        assertContextUnavailable();

        getBAController().beginBusinessActivity();
        getBAController().closeBusinessActivity();

        assertContextUnavailable();
    }

    @Test
    public void testSuspendResume() throws Exception {

        assertContextUnavailable();

        getBAController().beginBusinessActivity();
        dummyData.setValue("1");
        Assert.assertEquals("1", dummyData.getValue());
        Object txContext1 = getBAController().suspend();

        assertContextUnavailable();

        getBAController().beginBusinessActivity();
        dummyData.setValue("2");
        Assert.assertEquals("2", dummyData.getValue());
        Object txContext2 = getBAController().suspend();

        assertContextUnavailable();

        getBAController().resume(txContext1);
        Assert.assertEquals("1", dummyData.getValue());
        getBAController().closeBusinessActivity();

        assertContextUnavailable();

        getBAController().resume(txContext2);
        Assert.assertEquals("2", dummyData.getValue());
        getBAController().closeBusinessActivity();

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

        getBAController().beginBusinessActivity();
        service.doWork("blah");
        getBAController().cancelBusinessActivity();

        Assert.assertEquals(true, MyCompensationHandler.dataAvailable);
        Assert.assertEquals(false, MyConfirmationHandler.dataAvailable);
    }

    @Test
    public void testTwoCompensationHandlers() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        MyCompensationHandler.expectedData = "blah";
        MyConfirmationHandler.expectedData = "blah";

        getBAController().beginBusinessActivity();
        service.doWork("blah");
        service.doWork("blah");
        getBAController().cancelBusinessActivity();

        Assert.assertTrue(MyCompensationHandler.dataAvailable);
        Assert.assertFalse(MyConfirmationHandler.dataAvailable);
    }

    @Test
    public void testConfirmationHandler() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        MyCompensationHandler.expectedData = "blah";
        MyConfirmationHandler.expectedData = "blah";

        getBAController().beginBusinessActivity();
        service.doWork("blah");
        getBAController().closeBusinessActivity();

        Assert.assertEquals(false, MyCompensationHandler.dataAvailable);
        Assert.assertEquals(true, MyConfirmationHandler.dataAvailable);
    }

    @Test
    public void testTwoConfirmationHandlers() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        MyCompensationHandler.expectedData = "blah";
        MyConfirmationHandler.expectedData = "blah";

        getBAController().beginBusinessActivity();
        service.doWork("blah");
        service.doWork("blah");
        getBAController().closeBusinessActivity();

        Assert.assertFalse(MyCompensationHandler.dataAvailable);
        Assert.assertTrue(MyConfirmationHandler.dataAvailable);
    }

}