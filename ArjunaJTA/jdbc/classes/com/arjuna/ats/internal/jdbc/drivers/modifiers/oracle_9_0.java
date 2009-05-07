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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: oracle_9_0.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc.drivers.modifiers;

import com.arjuna.ats.jdbc.logging.jdbcLogger;
import com.arjuna.ats.jta.xa.XAModifier;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jta.exceptions.NotImplementedException;

import java.util.*;
import java.sql.*;
import javax.sql.*;
import javax.transaction.xa.Xid;
import oracle.jdbc.xa.OracleXid;

import java.sql.SQLException;

/*
 * This is a stateless class to allow us to get round
 * problems in Oracle. For example, they can't work with
 * an arbitrary implementation of Xid - it has to be their
 * own implementation!
 */

public class oracle_9_0 implements XAModifier, ConnectionModifier
{

    public String initialise (String dbName)
    {
	int index = dbName.indexOf(extensions.reuseConnectionTrue);
	int end = extensions.reuseConnectionTrue.length();

	if (index != -1)
	    _reuseConnection  = true;
	else
	{
	    index = dbName.indexOf(extensions.reuseConnectionFalse);
	    end = extensions.reuseConnectionFalse.length();
	}

	/*
	 * If at start, then this must be a JNDI URL. So remove component.
	 */

	if (index != 0)
	    return dbName;
	else
	    return dbName.substring(end + 1);  // remember colon
    }

    public Xid createXid (XidImple xid) throws SQLException, NotImplementedException
    {
	try
	{
	    return new OracleXid(xid.getFormatId(), xid.getGlobalTransactionId(),
				 xid.getBranchQualifier());
	}
	catch (Exception e)
	{
        SQLException sqlException = new SQLException(e.toString());
        sqlException.initCause(e);
	    throw sqlException;
	}
    }

    public XAConnection getConnection (XAConnection conn) throws SQLException, NotImplementedException
    {
	/* We don't want to call close() on the connection any more. */
	return null;
    }

    public boolean supportsMultipleConnections () throws SQLException, NotImplementedException
    {
	return true;
    }

    /**
     * @message com.arjuna.ats.internal.jdbc.drivers.modifiers.notSupported Oracle does not support isolation level
     */

    public void setIsolationLevel (Connection conn, int level) throws SQLException, NotImplementedException
    {
	/*
	 * Not supported by oracle 9 at this level. Must be set during the
	 * xa protocol.
	 */

	switch (level)
	{
	case Connection.TRANSACTION_SERIALIZABLE:
	case Connection.TRANSACTION_READ_COMMITTED:
	    break;
	case Connection.TRANSACTION_REPEATABLE_READ:
	case Connection.TRANSACTION_READ_UNCOMMITTED:
	default:
	    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.drivers.modifiers.notSupported")+level);
	}

	_isolationLevel = level;
    }

    public int xaStartParameters (int level) throws SQLException, NotImplementedException
    {
	int extraFlag = 0;

	switch (_isolationLevel)
	{
	case Connection.TRANSACTION_SERIALIZABLE:
	    extraFlag = oracle.jdbc.xa.XAResource.ORATMSERIALIZABLE;
	    break;
	case Connection.TRANSACTION_READ_COMMITTED:  // default so do nothing
	    break;
	case Connection.TRANSACTION_REPEATABLE_READ:
	case Connection.TRANSACTION_READ_UNCOMMITTED:
	default:
	    throw new SQLException(jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.drivers.modifiers.notSupported")+level);
	}

	return level | extraFlag;
    }

    private boolean _reuseConnection = true;

    private static int _isolationLevel = oracle.jdbc.xa.XAResource.ORATMSERIALIZABLE;

}
