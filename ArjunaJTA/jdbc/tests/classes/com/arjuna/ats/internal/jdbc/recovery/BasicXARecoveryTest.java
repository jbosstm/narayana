/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2017,
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.jdbc.recovery;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.xa.XAResource;

import java.sql.SQLException;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicXARecoveryTest {

    @Test
    public void test () throws SQLException, NamingException {
        Context ctx = new InitialContext();
        ctx.bind("h2DataSource", new JdbcDataSource());
        JdbcDataSource h2DataSource = (JdbcDataSource) ctx.lookup("h2DataSource");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("sa");
        h2DataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        BasicXARecovery bxar = new BasicXARecovery();
        bxar.initialise("h2recoveryproperties.xml");
        assertTrue(bxar.hasMoreResources());
        XAResource xar1 = bxar.getXAResource();
        assertFalse(bxar.hasMoreResources());
        assertTrue(bxar.hasMoreResources());
        XAResource xar2 = bxar.getXAResource();
        assertEquals(xar1, xar2);

    }
}
