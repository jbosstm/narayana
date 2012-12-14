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

package org.jboss.narayana.txframework.functional;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Prepare;
import org.jboss.narayana.txframework.functional.clients.ATStatefullClient;
import org.jboss.narayana.txframework.functional.interfaces.ATStatefull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

@RunWith(Arquillian.class)
public class ATStatefullTest extends BaseFunctionalTest {

    private UserTransaction ut;
    private ATStatefull client;

    @Before
    public void setupTest() throws Exception {

        ut = UserTransactionFactory.userTransaction();
        client = ATStatefullClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception {

        assertDataAvailable();
        client.clearLogs();
        rollbackIfActive(ut);
    }

    @Test
    public void testSimple() throws Exception {

        ut.begin();
        client.invoke1();
        client.invoke2();
        ut.commit();

        assertOrder(Prepare.class, Commit.class);
    }

    /*@Test
    public void testClientDrivenRollback() throws Exception
    {
        ut.begin();
        client.invoke();
        ut.rollback();

        //todo: should rollback be called twice? once for volatile and once for durable
        assertOrder(Rollback.class, Rollback.class);

    }

    @Test(expected = TransactionRolledBackException.class)
    public void testParticipantDrivenRollback() throws Exception
    {
        try
        {
            ut.begin();
            client.invoke(VOTE_ROLLBACK);
            ut.commit();
        }
        catch (TransactionRolledBackException e)
        {
            //todo: should rollback be called twice? once for volatile and once for durable
            assertOrder(Prepare.class, Rollback.class);
            throw e;
        }
    }

    @Test
    public void testApplicationException() throws Exception
    {
        try
        {
            ut.begin();
            client.invoke(THROW_APPLICATION_EXCEPTION);
            Assert.fail("Exception should have been thrown by now");
        }
        catch (SOAPFaultException e)
        {
            //todo: can we pass application exceptions over SOAP when using an EJB exposed as a JAX-WS ws?
            System.out.println("Caught exception");
        }
        finally
        {
            ut.rollback();
        }
        //todo: should this cause Rollback?
        assertOrder(Rollback.class, Rollback.class);
    }

*/

    private void assertOrder(Class<? extends Annotation>... expectedOrder) {

        Assert.assertEquals(Arrays.asList(expectedOrder), client.getEventLog().getEventLog());
    }

    private void assertDataAvailable() {

        List<Class<? extends Annotation>> log = client.getEventLog().getDataUnavailableLog();
        if (!log.isEmpty()) {
            Assert.fail("One or more lifecycle methods could not access the managed data: " + log.toString());
        }
    }

}
