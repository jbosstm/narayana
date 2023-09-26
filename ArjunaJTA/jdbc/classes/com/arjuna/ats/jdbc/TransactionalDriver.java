/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import com.arjuna.ats.internal.jdbc.ConnectionManager;
import com.arjuna.ats.jdbc.logging.jdbcLogger;

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
    public static final String poolConnections = "POOL_CONNECTIONS";
	public static final Object XADataSource = "XADATASOURCE";
    public static final String maxConnections = "MAXCONNECTIONS";

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

    /**
     * Static block registers instance of TransactionalDriver with sql drivers manager.
     */
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