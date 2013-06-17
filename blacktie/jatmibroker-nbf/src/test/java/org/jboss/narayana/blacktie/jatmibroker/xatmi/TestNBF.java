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

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;

public class TestNBF extends TestCase {
    private static final Logger log = LogManager.getLogger(TestNBF.class);

    private Connection connection;

    public void setUp() throws ConnectionException, ConfigurationException {
        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();
    }

    public void tearDown() throws ConnectionException, ConfigurationException {
        connection.close();
    }

    public void test() throws ConnectionException, ConfigurationException {
        log.info("TestNBF::test");
        BT_NBF buffer = (BT_NBF) connection.tpalloc("BT_NBF", "employee");
        assertFalse(buffer.btaddattribute("id", new Integer(1001)));
        assertTrue(buffer.btaddattribute("name", "zhfeng"));
        assertTrue(buffer.btaddattribute("id", new Long(1001)));

        // log.info(new String(buffer.serialize()));

        Object obj = buffer.btgetattribute("id", 0);
        assertTrue("java.lang.Long".equals(obj.getClass().getName()));
        assertTrue(((Long) obj).longValue() == 1001);

        obj = buffer.btgetattribute("id", 1);
        assertTrue(obj == null);

        obj = buffer.btgetattribute("name", 0);
        assertTrue("java.lang.String".equals(obj.getClass().getName()));
        assertTrue("zhfeng".equals((String) (obj)));

        BT_NBF test = (BT_NBF) connection.tpalloc("BT_NBF", "test");
        assertTrue(test.btaddattribute("employee", buffer));
        // log.info(new String(test.serialize()));

        obj = test.btgetattribute("employee", 0);
        assertTrue("org.jboss.narayana.blacktie.jatmibroker.xatmi.impl.BT_NBF_Impl".equals(obj.getClass().getName()));
        BT_NBF employee = (BT_NBF) obj;
        String name = (String) employee.btgetattribute("name", 0);
        assertTrue("zhfeng".equals(name));
        Long id = (Long) employee.btgetattribute("id", 0);
        assertTrue(id.longValue() == 1001);
    }

    public void testDel() throws ConnectionException, ConfigurationException {
        log.info("TestNBF::testDel");
        BT_NBF buffer = (BT_NBF) connection.tpalloc("BT_NBF", "employee");
        buffer.btaddattribute("id", new Long(1234));
        buffer.btaddattribute("id", new Long(1001));
        buffer.btaddattribute("id", new Long(1001));

        assertTrue(buffer.btdelattribute("id", 1));
        log.info("delete attribute id at index 1");

        assertFalse(buffer.btdelattribute("id", 1));
        log.info("double delete id failed");

        Long id = (Long) buffer.btgetattribute("id", 0);
        assertTrue(id.longValue() == 1234);

        assertTrue(buffer.btgetattribute("id", 1) == null);

        id = (Long) buffer.btgetattribute("id", 2);
        assertTrue(id.longValue() == 1001);

        buffer.btaddattribute("name", "test");
        assertTrue(buffer.btdelattribute("name", 0));
        log.info("delete attribute name at index 0");

        String name = (String) buffer.btgetattribute("name", 0);
        assertTrue(name == null);
        assertFalse(buffer.btdelattribute("unknow", 0));
    }

    public void testSet() throws ConnectionException, ConfigurationException {
        log.info("TestNBF::testSet");
        BT_NBF buffer = (BT_NBF) connection.tpalloc("BT_NBF", "employee");
        buffer.btaddattribute("id", new Long(1234));
        buffer.btaddattribute("id", new Long(1001));
        buffer.btaddattribute("name", "test");

        assertTrue(buffer.btdelattribute("id", 0));

        assertTrue(buffer.btsetattribute("id", 0, new Long(1002)));
        Long id = (Long) buffer.btgetattribute("id", 0);
        assertTrue(id.longValue() == 1002);

        assertTrue(buffer.btsetattribute("id", 1, new Long(1003)));
        id = (Long) buffer.btgetattribute("id", 1);
        assertTrue(id.longValue() == 1003);

        assertTrue(buffer.btsetattribute("name", 0, "test1"));
        String name = (String) buffer.btgetattribute("name", 0);
        assertTrue("test1".equals(name));

        assertTrue(buffer.btdelattribute("name", 0));
        assertTrue(buffer.btsetattribute("name", 0, "test2"));
        name = (String) buffer.btgetattribute("name", 0);
        assertTrue("test2".equals(name));

        assertFalse(buffer.btsetattribute("name", 1, "other"));
        assertFalse(buffer.btsetattribute("unknow", 0, "nothing"));
    }
}
