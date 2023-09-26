/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.jta.distributed;

import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;
import com.arjuna.ats.jta.utils.JNDIManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.*;

public class TestUTSerializability {
    @BeforeClass
    public static void setUp() throws Exception {
        JndiProvider.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        JndiProvider.stop();
    }

    @Test
    public void svmUTTest() throws NamingException {
        InitialContext context = new InitialContext(null);
        // ensure the transaction manager is available via JNDI
        JNDIManager.bindJTATransactionManagerImplementation(context);

        ServerVMClientUserTransaction ut = ServerVMClientUserTransaction.getSingleton();

        // validate that ut can be bound to a JNDI context
        context.rebind("ut", ut);

        // validate that the instance that was bound is the same as the ServerVMClientUserTransaction singleton
        Object boundUT = context.lookup("ut");

        assertNotNull(boundUT);
        assertTrue(boundUT instanceof ServerVMClientUserTransaction);
        assertEquals(ut, boundUT);
    }
}
