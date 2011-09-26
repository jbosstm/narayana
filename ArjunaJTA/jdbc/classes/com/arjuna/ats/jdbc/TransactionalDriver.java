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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionalDriver.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jdbc;

import com.arjuna.ats.jdbc.logging.*;

import com.arjuna.ats.internal.jdbc.ConnectionManager;

import com.arjuna.ats.arjuna.common.*;

import java.util.*;
import java.sql.*;

import java.sql.SQLException;
import java.lang.ExceptionInInitializerError;
import java.util.logging.Logger;

/**
 * Transactional JDBC 2.0 driver. This returns transactional JDBC connections
 * when required.
 *
 * We try to make this look as much like a standard
 * JDBC driver as possible.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TransactionalDriver.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class TransactionalDriver implements java.sql.Driver
{

public static final String arjunaDriver = "jdbc:arjuna:";
public static final String jdbc = "jdbc:";
public static final String userName = "user";
public static final String password = "password";
public static final String dynamicClass = "DYNAMIC_CLASS";
public static final String createDb = "CREATE_DB";

    public TransactionalDriver ()
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("TransactionalDriver.TransactionalDriver ()");
    }
    }

    public Connection connect (String url, Properties info) throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("TransactionalDriver.connect ( " + url + " )");
    }

	if (!url.startsWith(TransactionalDriver.arjunaDriver))
	{
	    return null;
	}
	else
	{
	    return ConnectionManager.create(url.substring(TransactionalDriver.arjunaDriver.length()), info);
	}
    }

    public boolean acceptsURL (String url) throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("TransactionalDriver.acceptsURL ( " + url + " )");
    }

	if (url != null)
	{
	    if (url.indexOf(TransactionalDriver.arjunaDriver) == -1)
		return false;
	    else
		return true;
	}
	else
	    return false;
    }

    public int getMajorVersion ()
    {
	return 2;
    }

    public int getMinorVersion ()
    {
	return 0;
    }

    public boolean jdbcCompliant ()
    {
	return true;
    }

    public DriverPropertyInfo[] getPropertyInfo (String url, Properties info) throws SQLException
    {
	if (jdbcLogger.logger.isTraceEnabled()) {
        jdbcLogger.logger.trace("TransactionalDriver.getPropertyInfo ( " + url + " )");
    }

	int index = url.indexOf(TransactionalDriver.arjunaDriver);

	if (index == -1)
	    return null;
	else
	{
	    String theUrl = url.substring(index+TransactionalDriver.arjunaDriver.length());
	    Driver d = DriverManager.getDriver(theUrl);

	    if (d != null)
		return d.getPropertyInfo(theUrl, info);
	    else
		return null;
	}
    }

    //@Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        throw new SQLFeatureNotSupportedException();
    }

    static
    {
	try
	{
	    DriverManager.registerDriver(new TransactionalDriver());
	}
	catch (Exception e)
	{
	    throw new ExceptionInInitializerError(e);
	}
    }

}

