/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jdbc.recovery;

import org.h2.jdbcx.JdbcDataSource;
import org.jnp.server.NamingBeanImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.xa.XAResource;

import java.sql.SQLException;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class BasicXARecoveryTest {
    private static final NamingBeanImpl NAMING_BEAN = new NamingBeanImpl();

    @BeforeClass
    public static void beforeClass() throws Exception {
        // Start JNDI server
        NAMING_BEAN.start();

        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("sa");
        h2DataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        Context ctx = new InitialContext();
        ctx.bind("h2DataSource", h2DataSource);
    }

    @AfterClass
    public static void afterClass() {
        NAMING_BEAN.stop();
    }

    @Test
    public void test () throws SQLException, NamingException {
        BasicXARecovery bxar = new BasicXARecovery();
        bxar.initialise("h2recoveryproperties.xml");
        assertTrue(bxar.hasMoreResources());
        XAResource xar1 = bxar.getXAResource();
        assertFalse(bxar.hasMoreResources());
        assertTrue(bxar.hasMoreResources());
        XAResource xar2 = bxar.getXAResource();
        assertEquals(xar1, xar2);
    }

    @Test
    public void testWithNumberOfConnections () throws SQLException, NamingException {
        BasicXARecovery bxar = new BasicXARecovery();
        bxar.initialise("h2recovery-multiple-properties.xml;2");
        assertTrue(bxar.hasMoreResources());
        XAResource xar1 = bxar.getXAResource();
        assertTrue(bxar.hasMoreResources());
        assertTrue(bxar.hasMoreResources());
        XAResource xar2 = bxar.getXAResource();
        assertNotEquals(xar1, xar2);
    }
}