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
 * $Id: oracle_8_1_6.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc.drivers;

import com.arjuna.ats.jdbc.logging.*;
import com.arjuna.ats.jdbc.TransactionalDriver;

import com.arjuna.ats.internal.jdbc.DynamicClass;

import java.util.*;
import java.sql.*;
import javax.sql.*;
import oracle.jdbc.xa.client.OracleXADataSource;
import oracle.jdbc.xa.OracleXid;
import oracle.jdbc.pool.*;
import javax.sql.XADataSource;

import java.sql.SQLException;

/*
 * This is a stateless class to allow us access to the Oracle
 * specific API without hardwiring the code into the generic
 * JDBC2 driver.
 */

public class oracle_8_1_6 implements DynamicClass
{

    public oracle_8_1_6 ()
    {
    }
    
    public XADataSource getDataSource (String dbName) throws SQLException
    {
	return getDataSource(dbName, true);
    }
    
    public synchronized XADataSource getDataSource (String dbName, boolean create) throws SQLException
    {
	try
	{
	    OracleXADataSource xads = new OracleXADataSource();
	    int index1 = dbName.indexOf(oracle_8_1_6.driverName);
	    
	    if (index1 == -1)
	    {
		throw new SQLException("oracle_8_1_6.getDataSource - "+jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.drivers.invaliddb")+" Oracle");
	    }
	    else
	    {
		/*
		 * Strip off any spurious parameters.
		 */

		int index2 = dbName.indexOf(oracle_8_1_6.semicolon);
		String theDbName = null;

		if (index2 == -1)
		{
		    theDbName = dbName.substring(index1+oracle_8_1_6.driverName.length());
		}
		else
		{
		    theDbName = dbName.substring(index1+oracle_8_1_6.driverName.length(),index2);
		}

		/*
		 * Rather than work our way through the string to determine
		 * the server name, the port, etc., we can make use
		 * of the Oracle specific method.
		 */

		xads.setURL(TransactionalDriver.jdbc+oracle_8_1_6.driverName+theDbName);

		return xads;
	    }
	}
	catch (SQLException e1)
	{
	    throw e1;
	}
	catch (Exception e2)
	{
	    throw new SQLException("oracle_8_1_6 "+jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.drivers.exception")+e2);
	}
    }
    
    public synchronized void shutdownDataSource (XADataSource ds) throws SQLException
    {
    }

    private static final String driverName = "oracle:";
    private static final String semicolon = ";";
    
}
