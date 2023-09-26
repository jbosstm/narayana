/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc.drivers;

import java.sql.Connection;
import java.sql.SQLException;

import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple_driver;

/*
 * Note: This impl has come from HP-TS-2.2 via. HP-MS 1.0
 */

/**
 * JDBC store implementation driver-specific code. This version for MS SQL
 * Server JDBC Drivers 2 (server 2005/2008).
 */
public class microsoft_driver extends JDBCImple_driver {
	@Override
	protected String getObjectStateSQLType() {
		return "VARBINARY(MAX)";
	}

	@Override
	protected void checkCreateTableError(SQLException ex) throws SQLException {
		if (!ex.getSQLState().equals(30001) && ex.getErrorCode() != 2714) {
			throw ex;
		}
	}

	@Override
	protected void checkDropTableException(Connection connection,
			SQLException ex) throws SQLException {
		if (!ex.getSQLState().equals("S0005") && ex.getErrorCode() != 3701) {
			throw ex;
		}
	}
}
