/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc.drivers;

import java.sql.Connection;
import java.sql.SQLException;

import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple_driver;

/**
 * JDBC store implementation driver-specific code. This version for Sybase
 * jConnect 6 JDBC Drivers.
 */
public class jconnect_driver extends JDBCImple_driver {

	@Override
	protected String getObjectStateSQLType() {
		return "IMAGE";
	}

	@Override
	protected void checkCreateTableError(SQLException ex) throws SQLException {
		if (ex.getErrorCode() != 2714) {
			throw ex;
		}
	}

	@Override
	protected void checkDropTableException(Connection connection,
			SQLException ex) throws SQLException {
		if (ex.getErrorCode() != 3701) {
			throw ex;
		}
	}
}