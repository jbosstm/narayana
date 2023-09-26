/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc.drivers.modifiers;

public class list
{
    public list ()
    {
		for (String driver : new String[] { "jConnect (TM) for JDBC (TM)",
				"Oracle JDBC driver",
				"IBM DB2 JDBC Universal Driver Architecture",
				"MySQL Connector Java",
				"MySQL-AB JDBC Driver",
				"MariaDB connector/J",
				"H2 JDBC Driver",
				"IBM Data Server Driver for JDBC and SQLJ",
				"Microsoft JDBC Driver 6.4 for SQL Server"}) {
			ModifierFactory.putModifier(driver, -1, -1,
					IsSameRMModifier.class.getName());
		}

		ModifierFactory.putModifier("PostgreSQL Native Driver", -1, -1,
				SupportsMultipleConnectionsModifier.class.getName());
	}
}