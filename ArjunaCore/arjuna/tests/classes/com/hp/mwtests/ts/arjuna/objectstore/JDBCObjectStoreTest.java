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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.arjuna.objectstore;

import java.sql.SQLException;

import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStoreEnvironmentBean;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors.accessor;
import com.hp.mwtests.ts.arjuna.resources.mock.MockAccessor;
import com.hp.mwtests.ts.arjuna.resources.mock.MockConnection;
import com.hp.mwtests.ts.arjuna.resources.mock.MockDriver;
import com.hp.mwtests.ts.arjuna.resources.mock.MockIBMDriver;
import com.hp.mwtests.ts.arjuna.resources.mock.MockJConnectDriver;
import com.hp.mwtests.ts.arjuna.resources.mock.MockMSFTDriver;
import com.hp.mwtests.ts.arjuna.resources.mock.MockMySqlDriver;
import com.hp.mwtests.ts.arjuna.resources.mock.MockOracleDriver;
import com.hp.mwtests.ts.arjuna.resources.mock.MockPostgresDriver;
import com.hp.mwtests.ts.arjuna.resources.mock.MockStatement;

import static org.junit.Assert.*;


public class JDBCObjectStoreTest
{
    @Test
    public void testJDBCImple () throws SQLException, ObjectStoreException, Exception
    {
        MockDriver imple = new MockDriver();
        
        assertFalse(imple.storeValid());
        
        imple.setValid(true);
        
        assertTrue(imple.initialise(new MockConnection(), new MockAccessor(), "tableName", new JDBCStoreEnvironmentBean()));
        
        imple.setState(StateStatus.OS_UNCOMMITTED);
        
        assertTrue(imple.commit_state(new Uid(), "typeName", "tableName"));
        
        try
        {
            assertFalse(imple.commit_state(new Uid(), null, "tableName"));
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        imple.setValid(false);
        
        assertFalse(imple.commit_state(new Uid(), "typeName", "tableName"));
        
        imple.setValid(true);
        
        assertTrue(imple.hide_state(new Uid(), "typeName", "tableName"));
        
        imple.setState(StateStatus.OS_UNKNOWN);
        
        assertFalse(imple.hide_state(new Uid(), "typeName", "tableName"));
        
        imple.setState(StateStatus.OS_COMMITTED);
        
        assertTrue(imple.hide_state(new Uid(), "typeName", "tableName"));
        
        imple.setValid(false);
        
        assertFalse(imple.hide_state(new Uid(), "typeName", "tableName"));
        
        imple.setValid(true);
        
        assertTrue(imple.reveal_state(new Uid(), "typeName", "tableName"));
        
        imple.setState(StateStatus.OS_UNKNOWN);
        
        assertFalse(imple.reveal_state(new Uid(), "typeName", "tableName"));
        
        imple.setState(StateStatus.OS_COMMITTED_HIDDEN);
        
        assertTrue(imple.reveal_state(new Uid(), "typeName", "tableName"));
        
        imple.setState(StateStatus.OS_UNCOMMITTED_HIDDEN);
        
        assertTrue(imple.reveal_state(new Uid(), "typeName", "tableName"));
        
        imple.setValid(false);
        
        assertFalse(imple.reveal_state(new Uid(), "typeName", "tableName"));
        
        imple.setState(StateStatus.OS_COMMITTED);
        
        assertFalse(imple.remove_state(new Uid(), "name", StateStatus.OS_COMMITTED, "tableName"));
        
        imple.setValid(true);
        
        assertTrue(imple.remove_state(new Uid(), "name", StateStatus.OS_COMMITTED, "tableName"));
        
        imple.setState(StateStatus.OS_UNKNOWN);
        
        assertTrue(imple.remove_state(new Uid(), "name", StateStatus.OS_COMMITTED, "tableName"));
        
        assertFalse(imple.remove_state(new Uid(), "name", StateStatus.OS_UNKNOWN, "tableName"));
        
        assertFalse(imple.remove_state(new Uid(), null, StateStatus.OS_UNKNOWN, "tableName"));
        
        imple.setValid(false);
        
        assertTrue(imple.read_state(new Uid(), "tName", StateStatus.OS_COMMITTED, "tableName") == null);
        
        imple.setValid(true);
        
        try
        {
            assertTrue(imple.read_state(new Uid(), null, StateStatus.OS_COMMITTED, "tableName") == null);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        imple.setState(StateStatus.OS_COMMITTED);
        
        assertTrue(imple.read_state(new Uid(), "tName", StateStatus.OS_COMMITTED, "tableName") == null);
        
        assertTrue(imple.write_state(new Uid(), "tName", new OutputObjectState(), StateStatus.OS_UNCOMMITTED, "tableName"));
        
        try
        {
            byte[] buff = new byte[imple.getMaxStateSize()+1];
            
            assertTrue(imple.write_state(new Uid(), "tName", new OutputObjectState(new Uid(), "tName", buff), StateStatus.OS_UNCOMMITTED, "tableName"));
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        imple.setReadWriteShortcut(false);
        
        assertTrue(imple.write_state(new Uid(), "tName", new OutputObjectState(), StateStatus.OS_UNCOMMITTED, "tableName"));
        
        imple.addTable("tname");
        
        assertEquals(imple.getTheState("foo"), StateStatus.OS_UNKNOWN);

        // TODO: fixme
//        Uid key = new Uid();
//        imple.addToTheCache(key, StateStatus.OS_COMMITTED);
//        imple.removeFromTheCache(key);
        
        assertFalse(imple.retryConnection(new ObjectStoreException(), 0));
        
        assertTrue(imple.retryConnection(new SQLException(), 0));
        
        imple.reconnect(0);
    }
    
    @Test
    public void testAccessor () throws SQLException
    {
        Object[] params = new Object[3];
        
        params[JDBCAccess.URL] = "foobar";
        params[JDBCAccess.DROP_TABLE] = new Long(1);
        params[JDBCAccess.TABLE_NAME] = "mytable";
        
        accessor a = new accessor();
        
        a.initialise(params);
        
        a.putConnection(null);
        
        try
        {
            a.getConnection();
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        assertTrue(a.dropTable());
        
        assertEquals(a.tableName(), params[JDBCAccess.TABLE_NAME]);
        
        params[JDBCAccess.URL] = null;
        
        try
        {
            a.initialise(params);
            
            fail();
        }
        catch (final Throwable ex)
        {
        }
        
        params[JDBCAccess.DROP_TABLE] = 0;
        
        try
        {
            a.initialise(params);
            
            fail();
        }
        catch (final Throwable ex)
        {
        }
    }
    
    @Test
    public void testOracleDriver () throws SQLException, ObjectStoreException
    {
        MockOracleDriver drvr = new MockOracleDriver();
        
        drvr.createTable(new MockStatement(), "foobar");
        
        assertTrue(drvr.name() != null);
        assertEquals(drvr.getMaxStateSize(), 1024 * 1024 * 10);   
        
        drvr.setValid(false);
        
        assertTrue(drvr.read_state(new Uid(), "foobar", StateStatus.OS_COMMITTED, "mytable") == null);
        
        drvr.setValid(true);
        
        assertTrue(drvr.read_state(new Uid(), "foobar", StateStatus.OS_COMMITTED, "mytable") == null);
        
        drvr.resetReadState();
        
        assertTrue(drvr.read_state(new Uid(), "foobar", StateStatus.OS_COMMITTED, "mytable") == null);
        
        byte[] buff = new byte[drvr.getMaxStateSize()+1];
        
        try
        {
            assertTrue(drvr.write_state(new Uid(), "foobar", new OutputObjectState(new Uid(), "foobar", buff), StateStatus.OS_COMMITTED, "mytable"));
            
            fail();
        }
        catch (final ObjectStoreException ex)
        {
        }
        
        assertTrue(drvr.write_state(new Uid(), "foobar", new OutputObjectState(), StateStatus.OS_COMMITTED, "mytable"));
        
        drvr.resetReadWriteShortcut();
        
        assertTrue(drvr.write_state(new Uid(), "foobar", new OutputObjectState(), StateStatus.OS_COMMITTED, "mytable"));
    }
    
    @Test
    public void testPostgresDriver () throws SQLException
    {
        MockPostgresDriver drvr = new MockPostgresDriver();
        
        drvr.createTable(new MockStatement(), "foobar");
        
        assertTrue(drvr.name() != null);
        assertEquals(drvr.getMaxStateSize(), 65535);
    }
    
    @Test
    public void testMySqlDriver () throws SQLException
    {
        MockMySqlDriver drvr = new MockMySqlDriver();
        
        drvr.createTable(new MockStatement(), "foobar");
        
        assertTrue(drvr.name() != null);
        assertEquals(drvr.getMaxStateSize(), 65535);
    }
    
    @Test
    public void testMSFTDriver () throws SQLException
    {
        MockMSFTDriver drvr = new MockMSFTDriver();
        
        drvr.createTable(new MockStatement(), "foobar");
        
        assertTrue(drvr.name() != null);
        assertEquals(drvr.getMaxStateSize(), 65535);
    }
    
    @Test
    public void testJConnectDriver () throws SQLException
    {
        MockJConnectDriver drvr = new MockJConnectDriver();
        
        drvr.createTable(new MockStatement(), "foobar");
        
        assertTrue(drvr.name() != null);
        assertEquals(drvr.getMaxStateSize(), 65535);
    }
    
    @Test
    public void testIBMDriver () throws SQLException
    {
        MockIBMDriver drvr = new MockIBMDriver();
        
        drvr.createTable(new MockStatement(), "foobar");
        
        assertTrue(drvr.name() != null);
        assertEquals(drvr.getMaxStateSize(), 65535);
    }
}
