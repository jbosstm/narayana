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

package org.jboss.narayana.txframework.functional.ws.at.bridged;

import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.mw.wst11.UserTransactionFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txframework.functional.BaseFunctionalTest;
import org.jboss.narayana.txframework.functional.ws.at.bridged.ATBridgeClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class ATBridgeTest extends BaseFunctionalTest {

    private UserTransaction ut;
    private ATBridge client;

    @Before
    public void setupTest() throws Exception {

        ut = UserTransactionFactory.userTransaction();
        client = ATBridgeClient.newInstance();
    }

    @After
    public void teardownTest() throws Exception {

        ut.begin();
        client.reset();
        ut.commit();
        rollbackIfActive(ut);
    }

    @Test
    public void testSimple() throws Exception {

        ut.begin();
        client.incrementCounter(1);
        ut.commit();

        ut.begin();
        int counter = client.getCounter();
        ut.commit();

        Assert.assertEquals(1, counter);
    }

    @Test
    public void testClientDrivenRollback() throws Exception {

        ut.begin();
        client.incrementCounter(1);
        ut.rollback();

        ut.begin();
        int counter = client.getCounter();
        ut.commit();

        Assert.assertEquals(0, counter);
    }
}

