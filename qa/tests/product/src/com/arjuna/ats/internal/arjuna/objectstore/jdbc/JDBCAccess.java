/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * Copyright (C) 2004
 *
 * Arjuna Technologies Ltd.,
 * Newcastle upon Tyne,
 * UK.
 *
 * $Id: JDBCAccess.java,v 1.2 2004/10/07 15:42:44 jcoleman Exp $
 *
 * Provide JDBC connections for JDBC object store and action store.
 * Uses profiles stored in JDBCProfiles and selected using property
 * "org.jboss.jbossts.qa.JDBCAccess".
 */

package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCAccess implements com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess
{
	private static final String dbProp = "org.jboss.jbossts.qa.Utils.JDBCAccess";

	public Connection getConnection() throws SQLException
	{
        try
        {   //CHECKPOINT SYNC
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection("jdbc:h2:~/os;WRITE_DELAY=0", "sa", "");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

	public void putConnection(Connection conn)
	{
	}

	public String tableName()
	{
		return "";
	}

	public boolean dropTable()
	{
		return false;
	}

	public void initialise(ObjectName objName)
	{
	}
}
