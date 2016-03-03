/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
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
 */
package org.jboss.narayana.blacktie.jatmibroker.xatmi;

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;

public class TestTPAlloc extends TestCase {
    private static final Logger log = LogManager.getLogger(TestTPAlloc.class);
    private Buffer m_allocated;
    private Connection connection;

    public void setUp() throws ConfigurationException {
        log.info("TestTPAlloc::setUp");
        m_allocated = null;
        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();

    }

    public void tearDown() {
        log.info("TestTPAlloc::tearDown");
        // if (m_allocated != null) {
        // connection.tpfree( m_allocated);
        // m_allocated = null;
        // }
    }

    public void test_tpalloc_zero() throws ConfigurationException, ConnectionException {
        log.info("test_tpalloc_zero");
        m_allocated = connection.tpalloc("X_OCTET", null);
    }

    public void test_tpalloc_x_octet_subtype_ignored() throws ConnectionException, ConfigurationException {
        log.info("test_tpalloc_x_octet_subtype_ignored");
        m_allocated = connection.tpalloc("X_OCTET", "fail");
    }

    // 9.1.1
    public void test_tpalloc_x_octet() throws ConnectionException, ConfigurationException {
        log.info("test_tpalloc_x_octet");
        m_allocated = connection.tpalloc("X_OCTET", null);

        assertTrue(m_allocated.getType().equals("X_OCTET"));
        assertTrue(m_allocated.getSubtype().equals(""));
    }

    // 9.1.2
    public void test_tpalloc_x_common() throws ConnectionException, ConfigurationException {
        log.info("test_tpalloc_x_common");
        X_COMMON dptr = (X_COMMON) connection.tpalloc("X_COMMON", "deposit");
        m_allocated = dptr;

        // ASSIGN SOME VALUES
        dptr.setLong("acct_no", 12345678);
        dptr.setShort("amount", (short) 50);
        dptr.setShort("balance", (short) 0);
        dptr.setByteArray("status", "c".getBytes());
        dptr.setShort("status_len", (short) 0);

        // CHECK THE ASSIGNATIONS
        assertTrue(dptr.getLong("acct_no") == 12345678);
        assertTrue(dptr.getShort("amount") == 50);
        assertTrue(dptr.getShort("balance") == 0);
        assertTrue(Arrays.equals(dptr.getByteArray("status"), "c".getBytes()));
        assertTrue(dptr.getShort("status_len") == 0);
    }

    public void test_tpalloc_x_common_bigsubtype() throws ConnectionException, ConfigurationException {
        log.info("test_tpalloc_x_common_bigsubtype");
        X_COMMON dptr = (X_COMMON) connection.tpalloc("X_COMMON", "abcdefghijklmnop");
        m_allocated = dptr;

        assertTrue(m_allocated.getType().equals("X_COMMON"));
        assertFalse(m_allocated.getSubtype().equals("abcdefghijklmnopq"));
        assertTrue(m_allocated.getSubtype().equals("abcdefghijklmnop"));
    }

    // 9.1.3
    public void test_tpalloc_x_c_type() throws ConnectionException, ConfigurationException {
        log.info("test_tpalloc_x_c_type");
        X_C_TYPE aptr;
        aptr = (X_C_TYPE) connection.tpalloc("X_C_TYPE", "acct_info");
        m_allocated = aptr;

        // ASSIGN SOME VALUES
        aptr.setLong("acct_no", 12345678);
        aptr.setByteArray("name", "12345678901234567890123456789012345678901234567890".getBytes());
        double[] balances = new double[2];
        balances[0] = 0;
        balances[1] = 0;
        aptr.setDoubleArray("balances", balances);

        // CHECK THE ASSIGNATIONS
        assertTrue(aptr.getLong("acct_no") == 12345678);
        assertTrue(Arrays.equals(aptr.getByteArray("name"), "12345678901234567890123456789012345678901234567890".getBytes()));
        assertTrue(aptr.getByteArray("address") == null);
        assertTrue(aptr.getDoubleArray("balances")[0] == 0);
        assertTrue(aptr.getDoubleArray("balances")[1] == 0);
    }

    public void test_tpalloc_unknowntype() throws ConfigurationException {
        log.info("test_tpalloc_unknowntype");
        try {
            m_allocated = connection.tpalloc("TOM", null);
            fail("Should not have got here");
        } catch (ConnectionException e) {
            assertTrue(e.getTperrno() == Connection.TPENOENT);
        }
    }

    public void test_tpalloc_x_common_subtype_required() throws ConnectionException {
        log.info("test_tpalloc_x_common_subtype_required");
        try {
            m_allocated = connection.tpalloc("X_COMMON", null);
            fail("Should not have got here");
        } catch (ConfigurationException e) {
        }
    }

    public void test_tpalloc_x_c_type_subtype_required() throws ConnectionException {
        log.info("test_tpalloc_x_c_type_subtype_required");
        try {
            m_allocated = connection.tpalloc("X_C_TYPE", null);
            fail("Should not have got here");
        } catch (ConfigurationException e) {
        }
    }

    public void test_tpalloc_x_common_unknown_subtype() throws ConnectionException {
        log.info("test_tpalloc_x_common_unknown_subtype");
        try {
            m_allocated = connection.tpalloc("X_COMMON", "UNKNOWN");
            fail("Should not have got here");
        } catch (ConfigurationException e) {
        }
    }

    public void test_tpalloc_x_c_type_unknown_subtype() throws ConnectionException {
        log.info("test_tpalloc_x_c_type_unknown_subtype");
        try {
            m_allocated = connection.tpalloc("X_C_TYPE", "UNKNOWN");
            fail("Should not have got here");
        } catch (ConfigurationException e) {
        }
    }
}
