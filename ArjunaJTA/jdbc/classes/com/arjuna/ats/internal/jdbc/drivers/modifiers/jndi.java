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
 * $Id: jndi.java 2342 2006-03-30 13:06:17Z  $
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
