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
 * $Id: accessor.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;

public class DataSourceJDBCAccess implements JDBCAccess {

	private String datasourceName;
	private InitialContext context;

	public Connection getConnection() throws SQLException {
		DataSource dataSource;
		try {
			dataSource = (DataSource) context.lookup(datasourceName);
		} catch (NamingException ex) {
			throw new FatalError(toString() + " : " + ex, ex);
		}
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		return connection;
	}

	public void initialise(StringTokenizer tokenizer) {
		while (tokenizer.hasMoreElements()) {
			try {
				String[] split = tokenizer.nextToken().split("=");
				if (split[0].equalsIgnoreCase("datasourceName")) {
					datasourceName = split[1];
				}
			} catch (Exception ex) {
				throw new FatalError(toString() + " : " + ex, ex);
			}
		}

		if (datasourceName == null) {
			throw new FatalError(
					"The JDBC ObjectStore was not configured with a datasource name");
		}
		
		try {
			context = new InitialContext();
		} catch (NamingException ex) {
			throw new FatalError(toString() + " : " + ex, ex);
		}
	}
}
