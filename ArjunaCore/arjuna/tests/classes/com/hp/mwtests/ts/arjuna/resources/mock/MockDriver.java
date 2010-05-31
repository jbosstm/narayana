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
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SyncRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.resources.mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;
import com.arjuna.ats.internal.arjuna.objectstore.JDBCImple;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.ibm_driver;

public class MockDriver extends ibm_driver
{   
    public boolean initialise(Connection conn, JDBCAccess jdbcAccess, String tableName) throws SQLException
    {
        super.initialise(conn, jdbcAccess, tableName);
        
        super._poolSizeInit = 1;
        super._inUse = new boolean[1];
        super._theConnection = new Connection[1];
        super._preparedStatements = new PreparedStatement[1][];
        
        super._preparedStatements[0] = new PreparedStatement[JDBCImple.STATEMENT_SIZE];
        
        super._preparedStatements[0][JDBCImple.READ_STATE] = new MockPreparedStatement(false);
        super._preparedStatements[0][JDBCImple.READ_WRITE_SHORTCUT] = new MockPreparedStatement();
        super._preparedStatements[0][JDBCImple.WRITE_STATE_NEW] = new MockPreparedStatement();
        super._preparedStatements[0][JDBCImple.SELECT_FOR_WRITE_STATE] = new MockPreparedStatement();
        super._preparedStatements[0][JDBCImple.PRECOMMIT_CLEANUP] = new MockPreparedStatement();
        super._preparedStatements[0][JDBCImple.COMMIT_STATE] = new MockPreparedStatement();
        super._preparedStatements[0][JDBCImple.CURRENT_STATE] = new MockPreparedStatement(false);
        super._preparedStatements[0][JDBCImple.HIDE_STATE] = new MockPreparedStatement();
        super._preparedStatements[0][JDBCImple.REVEAL_STATE] = new MockPreparedStatement();
        super._preparedStatements[0][JDBCImple.REMOVE_STATE] = new MockPreparedStatement();
        
        super._inUse[0] = false;
        super._theConnection[0] = new MockConnection();
        
        return true;
    }
    
    public void setState (int state)
    {
        _state = state;
    }
    
    public int currentState(Uid objUid, String typeName, String tableName) throws ObjectStoreException
    {
        super.currentState(objUid, typeName, tableName);
        
        return _state;
    }
    
    public void setValid (boolean v)
    {
        super._isValid = v;
    }
    
    public void setReadWriteShortcut (boolean res)
    {
        super._preparedStatements[0][JDBCImple.READ_WRITE_SHORTCUT] = new MockPreparedStatement(res);
    }
    
    public void addTable(String tableName) throws Exception
    {
        super.addTable(tableName);
    }
    
    public int getTheState(String state)
    {
        return super.getState(state);
    }
    
    public void addToTheCache(Uid state, int status)
    {
        super.addToCache(state, status);
    }
    
    public void removeFromTheCache(String state)
    {
        super.removeFromCache(state);
    }
    
    public boolean retryConnection(Throwable e, int pool)
    {
        return super.retryConnection(e, pool);
    }
    
    public void reconnect(int pool) throws SQLException
    {
        super.reconnect(pool);
    }
    
    public void createTable (Statement stmt, String tableName) throws SQLException
    {
        super.createTable(stmt, tableName);
    }

    public int getMaxStateSize()
    {
        return super.getMaxStateSize();
    }
    
    private int _state;
}

