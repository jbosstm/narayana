/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
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
 * $Id: cloudscape_3_6.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc.drivers.modifiers;

import com.arjuna.ats.jta.xa.XAModifier;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jta.exceptions.NotImplementedException;

import com.arjuna.ats.jdbc.logging.*;

import com.arjuna.common.util.logging.*;

import java.util.*;
import java.sql.*;
import javax.sql.*;
import javax.transaction.xa.Xid;

import java.sql.SQLException;

public class cloudscape_3_6 implements XAModifier, ConnectionModifier
{

    public cloudscape_3_6 ()
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
	 * If at start, then this must be a JNDI URL. So remove component.
	 */

	if (index != 0)
	    return dbName;
	else
	    return dbName.substring(end + 1);  // remember colon
    }
    
    public Xid createXid (XidImple xid) throws SQLException, NotImplementedException
    {
	return xid;
    }

    public XAConnection getConnection (XAConnection conn) throws SQLException, NotImplementedException
    {
	/*
	 * Would like to call close on the connection here, but
	 * for some reason Cloudscape won't let us, or at least
	 * won't work afterwards!
	 */

	return null;
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
		if (jdbcLogger.loggerI18N.isWarnEnabled())
		{
		    jdbcLogger.loggerI18N.warn("com.arjuna.ats.internal.jdbc.isolationlevelfailset",
					       new Object[] {"ConnectionImple.getConnection", e});
		}

		throw e;
	    }
	}
    }

    public boolean supportsMultipleConnections () throws SQLException, NotImplementedException
    {
	return true;
    }
    
}
