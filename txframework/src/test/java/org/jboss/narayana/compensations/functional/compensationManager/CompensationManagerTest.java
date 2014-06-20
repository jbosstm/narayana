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

package org.jboss.narayana.compensations.functional.compensationManager;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyCompensationHandler2;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler1;
import org.jboss.narayana.compensations.functional.common.DummyConfirmationHandler2;
import org.jboss.narayana.compensations.functional.common.MyRuntimeException;
import org.jboss.narayana.compensations.impl.BAControllerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.inject.Inject;


/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
@RunWith(Arquillian.class)
public abstract class CompensationManagerTest {

    @Inject
    CompensationManagerService compensationManagerService;


    @After
    public void tearDown() {

        try {
            BAControllerFactory.getInstance().cancelBusinessActivity();
        } catch (Exception e) {
            // do nothing
        }
    }

    @Before
    public void resetParticipants() {

        DummyCompensationHandler1.reset();
        DummyConfirmationHandler1.reset();
        DummyCompensationHandler2.reset();
        DummyConfirmationHandler2.reset();
    }


    @Test
    public void testSimple() throws Exception {

        try {
            compensationManagerService.doWork();
            Assert.fail("Expected TransactionRolledBackException to be thrown, but it was not");
        } catch (MyRuntimeException e) {
            //expected
        }

        Assert.assertEquals(false, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler1.getCalled());
    }


    @Test
    public void testNested() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        try {
            compensationManagerService.doWorkRecursively();
            Assert.fail("Expected TransactionRolledBackException to be thrown, but it was not");
        } catch (TransactionCompensatedException e) {
            //expected
        }

        Assert.assertEquals(true, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler1.getCalled());

        Assert.assertEquals(false, DummyCompensationHandler2.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler2.getCalled());
    }


    @Test
    public void testSimpleCompensateIfFail() throws Exception {

        try {
            compensationManagerService.doWorkCompensateIfFail();
            Assert.fail("Expected TransactionRolledBackException to be thrown, but it was not");
        } catch (MyRuntimeException e) {
            //expected
        }

        Assert.assertEquals(false, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler1.getCalled());
    }


    @Test
    public void testNestedCancelOnFailureWithFailure() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(3);

        try {
            compensationManagerService.doWorkRecursivelyCompensateIfFail(true);
            Assert.fail("Expected TransactionRolledBackException to be thrown, but it was not");
        } catch (TransactionCompensatedException e) {
            //expected
        }

        Assert.assertEquals(true, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler1.getCalled());

        Assert.assertEquals(false, DummyCompensationHandler2.getCalled());
        Assert.assertEquals(false, DummyConfirmationHandler2.getCalled());
    }

    @Test
    public void testNestedCancelOnFailureWithNoFailure() throws Exception {

        ParticipantCompletionCoordinatorRules.setParticipantCount(6);

        compensationManagerService.doWorkRecursivelyCompensateIfFail(false);

        Assert.assertEquals(false, DummyCompensationHandler1.getCalled());
        Assert.assertEquals(true, DummyConfirmationHandler1.getCalled());

        Assert.assertEquals(false, DummyCompensationHandler2.getCalled());
        Assert.assertEquals(true, DummyConfirmationHandler2.getCalled());
    }

}
