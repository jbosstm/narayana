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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;

public class DynamicDataSourceJDBCAccess implements JDBCAccess {
	private DataSource dataSource;

	public Connection getConnection() throws SQLException {
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		return connection;
	}

	public void initialise(StringTokenizer tokenizer) {
		while (tokenizer.hasMoreElements()) {
			Map<String, String> configuration = new HashMap<String, String>();
			while (tokenizer.hasMoreTokens()) {
				String[] split = tokenizer.nextToken().split("=");
				configuration.put(split[0], split[1].replace("\\equ", "="));
			}
			try {
				this.dataSource = (DataSource) Class.forName(
						configuration.remove("ClassName")).newInstance();
				Iterator<String> iterator = configuration.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = configuration.get(key);
					Method method = null;
					try {
						method = dataSource.getClass().getMethod("set" + key,
								java.lang.String.class);
						String replace = value.replace("\\semi", ";");
						method.invoke(dataSource, value.replace("\\semi", ";"));
					} catch (NoSuchMethodException nsme) {
						method = dataSource.getClass().getMethod("set" + key,
								int.class);
						method.invoke(dataSource, Integer.valueOf(value));
					}
				}
			} catch (Exception ex) {
				dataSource = null;
				throw new FatalError(toString() + " : " + ex, ex);
			}
		}
	}
}
