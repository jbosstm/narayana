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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BasicXARecovery.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc.recovery;

import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;
import com.arjuna.ats.jdbc.logging.jdbcLogger;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

import com.arjuna.common.util.logging.*;

import java.sql.*;
import javax.transaction.xa.*;
import java.util.*;

import java.lang.NumberFormatException;
import com.arjuna.common.internal.util.propertyservice.plugins.io.XMLFilePlugin;

/**
 * This class implements the XAResourceRecovery interface for XAResources. The
 * parameter supplied in setParameters can contain arbitrary information
 * necessary to initialise the class once created. In this instance it contains
 * the name of the property file in which the db connection information is
 * specified, as well as the number of connections that this file contains
 * information on (separated by ;).
 *
 * IMPORTANT: this is only an *example* of the sorts of things an
 * XAResourceRecovery implementor could do. This implementation uses a property
 * file which is assumed to contain sufficient information to recreate
 * connections used during the normal run of an application so that we can
 * perform recovery on them. It is not recommended that information such as user
 * name and password appear in such a raw text format as it opens up a potential
 * security hole.
 *
 * The db parameters specified in the property file are assumed to be in the
 * format:
 *
 * <transaction-service>
 *   <properties name="jdbc">
 *     <property name="DB_X_DatabaseUser" value="username"/>
 *     <property name="DB_X_DatabasePassword" value="password"/>
 *     <property name="DB_X_DatabaseDynamicClassA" value="DynamicClass"/>
 *     <property name="DB_X_DatabaseURL" value="theURL"/>
 *   </properties>
 * </transaction-service>
 *
 * where X is the number of the connection information, starting from 1.
 * The DynamicClass is optional. If not present, JNDI will be used for resolving
 * the DatabaseURL value into a XADataSource.
 *
 * <properties depends="arjuna" name="jta">
 *   <property name="com.arjuna.ats.jta.recovery.XAResourceRecovery1"
 *      value="com.arjuna.ats.internal.jdbc.recovery.BasicXARecovery;jbossts-properties.xml[;X]"/>
 *
 * @since JTS 2.1.
 */

public class BasicXARecovery implements XAResourceRecovery
{
	/*
	 * Some XAResourceRecovery implementations will do their startup work here,
	 * and then do little or nothing in setDetails. Since this one needs to know
	 * dynamic class name, the constructor does nothing.
	 */

	public BasicXARecovery () throws SQLException
	{
		if (jdbcLogger.logger.isDebugEnabled())
		{
			jdbcLogger.logger
					.debug(
							DebugLevel.CONSTRUCTORS,
							VisibilityLevel.VIS_PUBLIC,
							com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY,
							"BasicXARecovery ()");
		}

		numberOfConnections = 1;
		connectionIndex = 0;
		props = null;
	}

	/**
	 * The recovery module will have chopped off this class name already. The
	 * parameter should specify a property file from which the url, user name,
	 * password, etc. can be read.
	 *
	 * @message com.arjuna.ats.internal.jdbc.recovery.basic.initexp An exception
	 *          occurred during initialisation.
	 */

	public boolean initialise (String parameter) throws SQLException
	{
		if (jdbcLogger.logger.isDebugEnabled())
		{
			jdbcLogger.logger
					.debug(
							DebugLevel.CONSTRUCTORS,
							VisibilityLevel.VIS_PUBLIC,
							com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY,
							"BasicXARecovery.setDetail(" + parameter + ")");
		}

		if (parameter == null)
			return true;

		int breakPosition = parameter.indexOf(BREAKCHARACTER);
		String fileName = parameter;

		if (breakPosition != -1)
		{
			fileName = parameter.substring(0, breakPosition - 1);

			try
			{
				numberOfConnections = Integer.parseInt(parameter
						.substring(breakPosition + 1));
			}
			catch (NumberFormatException e)
			{
				if (jdbcLogger.loggerI18N.isWarnEnabled())
				{
					jdbcLogger.loggerI18N.warn(
							"com.arjuna.ats.internal.jdbc.recovery.basic.initexp",
							new Object[] { e });
				}

				return false;
			}
		}

		try
		{
			jdbcPropertyManager.propertyManager.load(XMLFilePlugin.class
					.getName(), fileName);

			props = jdbcPropertyManager.propertyManager.getProperties();
		}
		catch (Exception e)
		{
			if (jdbcLogger.loggerI18N.isWarnEnabled())
			{
				jdbcLogger.loggerI18N.warn(
						"com.arjuna.ats.internal.jdbc.recovery.basic.initexp",
						new Object[] { e });
			}

			return false;
		}

		if (jdbcLogger.logger.isDebugEnabled())
		{
			jdbcLogger.logger
					.debug(
							DebugLevel.CONSTRUCTORS,
							VisibilityLevel.VIS_PUBLIC,
							com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY,
							"BasicXARecovery properties file = " + parameter);
		}

		return true;
	}

	/**
	 * @message com.arjuna.ats.internal.jdbc.recovery.basic.xarec {0} could not find
	 *          information for connection!
	 */

	public synchronized XAResource getXAResource () throws SQLException
	{
		JDBC2RecoveryConnection conn = null;

		if (hasMoreResources())
		{
			connectionIndex++;

			conn = getConnection();

			if (conn == null)
			{
				if (jdbcLogger.loggerI18N.isWarnEnabled())
				{
					jdbcLogger.loggerI18N.warn(
							"com.arjuna.ats.internal.jdbc.recovery.basic.xarec",
							new Object[] { "BasicXARecovery.getConnection -" });
				}
			}
		}

		return conn.recoveryConnection().getConnection().getXAResource();
	}

	public synchronized boolean hasMoreResources ()
	{
		if (connectionIndex == numberOfConnections)
			return false;
		else
			return true;
	}

	private final JDBC2RecoveryConnection getConnection ()
			throws SQLException
	{
		String number = new String("" + connectionIndex);
		String url = new String(dbTag + number + urlTag);
		String password = new String(dbTag + number + passwordTag);
		String user = new String(dbTag + number + userTag);
		String dynamicClass = new String(dbTag + number + dynamicClassTag);

		Properties dbProperties = new Properties();

		String theUser = props.getProperty(user);
		String thePassword = props.getProperty(password);
        String theURL = props.getProperty(url);

		if (theUser != null)
		{
			dbProperties.put(TransactionalDriver.userName, theUser);
			dbProperties.put(TransactionalDriver.password, thePassword);

			String dc = props.getProperty(dynamicClass);

			if (dc != null)
				dbProperties.put(TransactionalDriver.dynamicClass, dc);

			return new JDBC2RecoveryConnection(theURL, dbProperties);
		}
		else
			return null;
	}

	private int numberOfConnections;

	private int connectionIndex;

	private Properties props;

	private static final String dbTag = "DB_";

	private static final String urlTag = "_DatabaseURL";

	private static final String passwordTag = "_DatabasePassword";

	private static final String userTag = "_DatabaseUser";

	private static final String dynamicClassTag = "_DatabaseDynamicClass";

	private static final char BREAKCHARACTER = ';'; // delimiter for parameters

}
