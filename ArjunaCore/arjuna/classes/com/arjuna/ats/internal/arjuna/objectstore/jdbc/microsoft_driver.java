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
 * $Id: microsoft_driver.java 2342 2006-03-30 13:06:17Z  $
 *
 * Copyright (c) 2001 Hewlett-Packard Company
 * Hewlett-Packard Company Confidential
 * Copyright (c) 2004 Arjuna Technologies Limited
 *
 * $Project: ArjunaCore$
 * $Revision: 2342 $
 * $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * $Author: $
 */

/*
 * Note: This impl has come from HP-TS-2.2 via. HP-MS 1.0
 */

package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.internal.arjuna.objectstore.JDBCImple;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/**
 * JDBC store implementation driver-specific code.
 * This version for MS SQL Server JDBC Drivers 2 (server 2005/2008).
 */
public class microsoft_driver extends JDBCImple
{
	protected void createTable (Statement stmt, String tableName) throws SQLException
	{
		stmt.executeUpdate("CREATE TABLE "+tableName+" (StateType INTEGER, TypeName VARCHAR(1024), UidString VARCHAR(255), ObjectState VARBINARY(MAX), PRIMARY KEY(UidString, StateType, TypeName))");
	}

	public String name ()
	{
		return "mssqlserver";
	}

    protected int getMaxStateSize()
    {
        return 65535;
    }
}
