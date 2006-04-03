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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: sequelink_5_1.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc.drivers;

import com.arjuna.ats.jdbc.logging.*;

import com.arjuna.ats.internal.jdbc.DynamicClass;

import java.util.*;
import java.sql.*;
import javax.sql.*;
import com.merant.sequelink.jdbcx.datasource.SequeLinkDataSource;
import javax.sql.XADataSource;

import java.sql.SQLException;

/*
 * This is a stateless class to allow us access to the Merant
 * specific API without hardwiring the code into the generic
 * JDBC2 driver.
 */

public class sequelink_5_1 implements DynamicClass
{

    public sequelink_5_1 ()
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
	    SequeLinkDataSource xads = new SequeLinkDataSource();
	    int index1 = dbName.indexOf(sequelink_5_1.driverName);
	    
	    if (index1 == -1)
		throw new SQLException("sequelink_5_1.getDataSource - "+jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.drivers.invaliddb")+" Merant");
	    else
	    {
		/*
		 * Strip off any spurious parameters.
		 */

		int index2 = dbName.indexOf(sequelink_5_1.semicolon);
		String theUrl = null;

		if (index2 == -1)
		{
		    theUrl = dbName.substring(index1+sequelink_5_1.driverName.length());
		}
		else
		{
		    theUrl = dbName.substring(index1+sequelink_5_1.driverName.length(), index2);
		}

		/*
		 * From the string, get the database name, the server name,
		 * the port, etc.
		 */

		int thePort = 0;
		String theServer = null;
		String theDbName = null;

		index1 = dbName.indexOf(sequelink_5_1.databaseName);
		    
		if (index1 != -1)
		{
		    index2 = dbName.indexOf(sequelink_5_1.semicolon, index1);
		    
		    if (index2 == -1)
		    {
			theDbName = dbName.substring(index1+sequelink_5_1.databaseName.length());
		    }
		    else
		    {
			theDbName = dbName.substring(index1+sequelink_5_1.databaseName.length(), index2);
		    }
		}

		if (theDbName != null)
		    xads.setDatabaseName(theDbName);

		index1 = theUrl.indexOf(sequelink_5_1.colon);
		
		if (index1 != -1)
		{
		    try
		    {
			Integer i = new Integer(theUrl.substring(index1+1));
			
			thePort = i.intValue();
		    }
		    catch (Exception e)
		    {
			throw new SQLException(e.toString());
		    }

		    theServer = theUrl.substring(0, index1);
		}
		else
		{
		    theServer = theUrl;
		}
		
		xads.setServerName(theServer);
		xads.setPortNumber(thePort);

		return xads;
	    }
	}
	catch (SQLException e1)
	{
	    throw e1;
	}
	catch (Exception e2)
	{
	    throw new SQLException("sequelink_5_1 "+jdbcLogger.logMesg.getString("com.arjuna.ats.internal.jdbc.drivers.exception")+e2);
	}
    }
    
    public synchronized void shutdownDataSource (XADataSource ds) throws SQLException
    {
    }

    private static final String driverName = "sequelink://";
    private static final String semicolon = ";";
    private static final String colon = ":";
    private static final String databaseName = "databaseName=";

}
