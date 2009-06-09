/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss by Red Hat.
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import com.arjuna.ats.internal.arjuna.objectstore.JDBCImple;

import java.sql.Statement;
import java.sql.SQLException;

/**
 * JDBC store implementation driver-specific code.
 * This version for MySQL JDBC Drivers.
 */
public class mysql_ab_driver extends JDBCImple
{
    protected void createTable (Statement stmt, String tableName) throws SQLException
	{
		stmt.executeUpdate("CREATE TABLE "+tableName+" (StateType INTEGER, TypeName VARCHAR(255), UidString VARCHAR(255), ObjectState BLOB, PRIMARY KEY(UidString, StateType, TypeName))");
	}

	public String name ()
	{
		return "mysql";
	}

    protected int getMaxStateSize()
    {
        return 65535;
    }
}
