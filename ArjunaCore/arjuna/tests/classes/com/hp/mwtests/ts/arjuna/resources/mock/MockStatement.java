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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class MockStatement implements Statement
{

    @Override
    public void addBatch (String arg0) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void cancel () throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearBatch () throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearWarnings () throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void close () throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean execute (String arg0) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean execute (String arg0, int arg1) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean execute (String arg0, int[] arg1) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean execute (String arg0, String[] arg1) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int[] executeBatch () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet executeQuery (String arg0) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int executeUpdate (String arg0) throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int executeUpdate (String arg0, int arg1) throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int executeUpdate (String arg0, int[] arg1) throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int executeUpdate (String arg0, String[] arg1) throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Connection getConnection () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getFetchDirection () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getFetchSize () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ResultSet getGeneratedKeys () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaxFieldSize () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxRows () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getMoreResults () throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getMoreResults (int arg0) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getQueryTimeout () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ResultSet getResultSet () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getResultSetConcurrency () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getResultSetHoldability () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getResultSetType () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getUpdateCount () throws SQLException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public SQLWarning getWarnings () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isClosed () throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPoolable () throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCursorName (String arg0) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setEscapeProcessing (boolean arg0) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFetchDirection (int arg0) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setFetchSize (int arg0) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMaxFieldSize (int arg0) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMaxRows (int arg0) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPoolable (boolean arg0) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setQueryTimeout (int arg0) throws SQLException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isWrapperFor (Class<?> arg0) throws SQLException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T unwrap (Class<T> arg0) throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }


    //@Override
    public void closeOnCompletion() throws SQLException
    {
    }

    //@Override
    public boolean isCloseOnCompletion() throws SQLException
    {
        return false;
    }
}

