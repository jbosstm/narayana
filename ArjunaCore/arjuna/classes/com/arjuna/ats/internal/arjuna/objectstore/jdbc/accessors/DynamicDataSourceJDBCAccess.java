/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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