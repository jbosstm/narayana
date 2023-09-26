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

/*
 * JDBC store implementation driver-specific code.
 * This version for Oracle 8.1/9.* JDBC Drivers (OCI or Thin) ONLY.
 */
public class oracle_driver extends JDBCImple_driver {

	@Override
	protected String getObjectStateSQLType() {
		return "BLOB";
	}

	@Override
	public int getMaxStateSize() {
		// Oracle BLOBs should be OK up to > 4 GB, but cap @ 10 MB for
		// testing/performance:
		return 1024 * 1024 * 10;
	}

	@Override
	protected void checkCreateTableError(SQLException ex) throws SQLException {
		if (!ex.getSQLState().equals("42000") && ex.getErrorCode() != 955) {
			throw ex;
		}
	}

	@Override
	protected void checkDropTableException(Connection connection,
			SQLException ex) throws SQLException {
		if (!ex.getSQLState().equals("42000") && ex.getErrorCode() != 942) {
			throw ex;
		}
	}
}
