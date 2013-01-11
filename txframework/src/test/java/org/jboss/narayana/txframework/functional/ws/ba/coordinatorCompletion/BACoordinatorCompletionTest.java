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
import junit.framework.Assert;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Cancel;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Close;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Complete;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.ConfirmCompleted;
import org.jboss.narayana.txframework.functional.BaseFunctionalTest;
import org.jboss.narayana.txframework.functional.ws.ba.coordinatorCompletion.BACoordinatorCompletionClient;
import org.jboss.narayana.txframework.functional.SomeApplicationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static org.jboss.narayana.txframework.functional.ServiceCommand.CANNOT_COMPLETE;
import static org.jboss.narayana.txframework.functional.ServiceCommand.THROW_APPLICATION_EXCEPTION;

@RunWith(Arquillian.class)
public class BACoordinatorCompletionTest extends BaseFunctionalTest {

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
}
