/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.objectstore.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.naming.NamingException;

import com.arjuna.ats.arjuna.exceptions.FatalError;

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
     * times.  It <EM>must</EM> not return the same connection each time.
     * @throws SQLException 
     */

    public Connection getConnection () throws SQLException;

    /**
     * This method can be used to pass additional information to the
     * implementation.
     */

    public void initialise (StringTokenizer stringTokenizer);

}