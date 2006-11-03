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
 * $Id: JDBCAccess.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.objectstore.jdbc;

import com.arjuna.ats.arjuna.gandiva.ObjectName;

import java.sql.Connection;

import java.sql.SQLException;

/**
 * Do not return a connection which participates within the
 * transaction 2-phase commit protocol! All connections will have
 * auto-commit set to true, or we will not be able to use them.
 * So don't return an Arjuna JDBC 1.0 or 2.x connection.
 *
 * @since JTS 2.1.
 */

public interface JDBCAccess
{

    /**
     * @return the connection to use for the object store.  If a pool of
     * connections is used, this method may be called up to maxpoolsize
     * times.  It <EM>must<EM> not return the same connection each time.
     */

    public Connection getConnection () throws SQLException;
    
    /**
     * @called from the object store when a connection is no longer used.
     */

    public void putConnection (Connection conn);
    
    /**
     * @return the name of the table to use for storing the state.
     * @deprecated
     */

     public String tableName ();

    /**
     * @return <code>true</code> if the table is to be deleted and recreated
     * prior to use, <code>false</code> otherwise.
     * @deprecated
     */

    public boolean dropTable ();

    /**
     * This method can be used to pass additional information to the
     * implementation.
     */

    public void initialise (ObjectName objName);
    
}
