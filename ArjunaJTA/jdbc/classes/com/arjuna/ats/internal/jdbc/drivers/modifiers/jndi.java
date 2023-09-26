/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc.drivers.modifiers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jdbc.logging.jdbcLogger;
import com.arjuna.ats.jta.exceptions.NotImplementedException;
import com.arjuna.ats.jta.xa.XAModifier;

public class jndi implements XAModifier
{

    public jndi ()
    {
    }

    public String initialise (String dbName)
    {
	int index = dbName.indexOf(extensions.reuseConnectionTrue);
	int end = extensions.reuseConnectionTrue.length();
	
	if (index == -1)
	{
	    index = dbName.indexOf(extensions.reuseConnectionFalse);
	    end = extensions.reuseConnectionFalse.length();
	}

	/*
	 * If at start, then this must be a JNDI URL. So release component.
	 */

	if (index != 0)
	    return dbName;
	else
	    return dbName.substring(end + 1);  // remember colon
    }

    public int xaStartParameters (int level) throws SQLException, NotImplementedException
    {
	return level;
    }
    
    public Xid createXid (Xid xid) throws NotImplementedException
    {
	throw new NotImplementedException();
    }

    public XAConnection getConnection (XAConnection conn) throws SQLException, NotImplementedException
    {
	throw new NotImplementedException();
    }

    public boolean supportsMultipleConnections () throws SQLException, NotImplementedException
    {
	throw new NotImplementedException();
    }

    public void setIsolationLevel (Connection conn, int level) throws SQLException, NotImplementedException
    {
	DatabaseMetaData metaData = conn.getMetaData();

	if (metaData.supportsTransactionIsolationLevel(level))
	{
	    try
	    {
		if (conn.getTransactionIsolation() != level)
		{
		    conn.setTransactionIsolation(level);
		}
	    }
	    catch (SQLException e)
	    {
            jdbcLogger.i18NLogger.warn_isolationlevelfailset("ConnectionImple.getConnection", e);

		throw e;
	    }
	}
    }
    
}