/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc.recovery;

import java.sql.SQLException;
import java.util.Properties;

import com.arjuna.ats.internal.jdbc.ConnectionImple;

/**
 * To perform recovery on arbitrary connections we may need to recreate those
 * connections.
 * 
 * The ArjunaJDBC2Connection class must not be used directly by applications,
 * hence the requirement for this class.
 * 
 * @since JTS 2.1.
 */

public class JDBC2RecoveryConnection extends ConnectionImple
{

	public JDBC2RecoveryConnection (String dbName, Properties info)
			throws SQLException
	{
		super(dbName, info);
	}

}