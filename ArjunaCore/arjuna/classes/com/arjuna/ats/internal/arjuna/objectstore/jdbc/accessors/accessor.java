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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: accessor.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors;

import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.arjuna.gandiva.ObjectName;

import java.sql.*;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import java.sql.SQLException;

/**
 * Do not return a connection which participates within the
 * transaction 2-phase commit protocol! All connections will have
 * auto-commit set to true, or we will not be able to use them.
 * So don't return an Arjuna JDBC 1.0 or 2.x connection.
 *
 * @since JTS 2.1.
 */

public class accessor implements JDBCAccess
{

    public accessor ()
    {
	_tableName = null;
	_dropTable = false;
	_url = null;
    }
    
    public Connection getConnection () throws SQLException
    {
	return DriverManager.getConnection(_url, null);
    }
    
    public void putConnection (Connection conn)
    {
    }

    public String tableName ()
    {
	return _tableName;
    }
    
    public boolean dropTable ()
    {
	return _dropTable;
    }

    public void initialise (ObjectName objName)
    {
	try
	{
	    _url = objName.getStringAttribute(ArjunaNames.Implementation_ObjectStore_JDBC_url());
	    _tableName = objName.getStringAttribute(ArjunaNames.Implementation_ObjectStore_JDBC_tableName());

	    long drop = objName.getLongAttribute(ArjunaNames.Implementation_ObjectStore_JDBC_dropTable());

	    if (drop == 1)
		_dropTable = true;
	}
	catch (Exception ex)
	{
	    throw new FatalError(toString()+" : "+ex);
	}

	if (_url == null)
	    throw new FatalError(toString()+" : invalid ObjectName parameter!");
    }

    private String  _tableName;
    private boolean _dropTable;
    private String  _url;
    
}
