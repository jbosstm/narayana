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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: oracle_jndi.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc.drivers.modifiers;

import com.arjuna.ats.jta.xa.XAModifier;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jta.exceptions.NotImplementedException;

import java.util.*;
import java.sql.*;
import javax.sql.*;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.Xid;
import oracle.jdbc.xa.OracleXid;

import java.sql.SQLException;

/*
 * This is a stateless class to allow us to get round
 * problems in Oracle. For example, they can't work with
 * an arbitrary implementation of Xid - it has to be their
 * own implementation!
 */

public class oracle_jndi implements XAModifier, ConnectionModifier
{

    public String initialise (String dbName)
    {
	int index = dbName.indexOf(extensions.reuseConnectionTrue);
	int end = extensions.reuseConnectionTrue.length();
	
	if (index != -1)
	    _reuseConnection  = false;
	else
	{
	    index = dbName.indexOf(extensions.reuseConnectionFalse);
	    end = extensions.reuseConnectionFalse.length();
	}

	/*
	 * If at start, then this must be a JNDI URL. So remove component.
	 */

	if (index != 0)
	    return dbName;
	else
	    return dbName.substring(end + 1);  // remember colon
    }
    
    public Xid createXid (XidImple xid) throws SQLException, NotImplementedException
    {
	try
	{
	    return new OracleXid(xid.getFormatId(), xid.getGlobalTransactionId(),
				 xid.getBranchQualifier());
	}
	catch (Exception e)
	{
	    throw new SQLException(e.toString());
	}
    }

    public int xaStartParameters (int level) throws SQLException, NotImplementedException
    {
	return level;
    }

    public XAConnection getConnection (XAConnection conn) throws SQLException, NotImplementedException
    {
	/* We don't want to call close() on the connection any more. */
	return null;
    }

    public boolean supportsMultipleConnections () throws SQLException, NotImplementedException
    {
	return true;
    }

    public void setIsolationLevel (Connection conn, int level) throws SQLException, NotImplementedException
    {

	TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
	Transaction tx = null;

	try
	{
	    tx = tm.getTransaction();
	}
	catch (javax.transaction.SystemException se)
	{
	    /* Ignore: tx is null already */
	}

	if (tx != null && conn.getTransactionIsolation() != level)
	    conn.setTransactionIsolation(level);

    }
    
    private boolean _reuseConnection = false;
 
}
