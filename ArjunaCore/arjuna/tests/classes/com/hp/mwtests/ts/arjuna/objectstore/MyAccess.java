/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.objectstore;



import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

public class MyAccess implements
		com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess {

	public Connection getConnection() throws SQLException {
		return null;
	}

	public void initialise(StringTokenizer objName) {
	}

}