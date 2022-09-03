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
 * (C) 2005-2009,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ConnectionManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc;

import com.arjuna.ats.jdbc.TransactionalDriver;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/*
 * Only ever create a single instance of a given connection, based upon the
 * user/password/url/dynamic_class options. If the connection we have cached
 * has been closed, then create a new one.
 */
public class ConnectionManager {


    /*
     * Connections are pooled for the duration of a transaction.
     */
    public static Connection create (String dbUrl, Properties info) throws SQLException
    {
        String user = info.getProperty(TransactionalDriver.userName, "");
        String passwd = info.getProperty(TransactionalDriver.password, "");
        String dynamic = info.getProperty(TransactionalDriver.dynamicClass, "");
        String poolConnections = info.getProperty(TransactionalDriver.poolConnections, "true");
        Object xaDataSource = info.get(TransactionalDriver.XADataSource);
        int maxConnections = Integer.valueOf(info.getProperty(TransactionalDriver.maxConnections, "10"));

        if (dbUrl == null) {
            dbUrl = "";
        }

        boolean poolingEnabled = "true".equalsIgnoreCase(poolConnections);

        ConnectionImple conn = null;

        synchronized (_connections) {
            if (poolingEnabled) {
                Iterator<ConnectionImple> iterator = _connections.iterator();
                while (iterator.hasNext()) {
                    ConnectionImple c = iterator.next();
                    ConnectionControl connControl = c.connectionControl();
                    TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
                    Transaction tx1, tx2 = null;

                    tx1 = connControl.transaction();
                    try {
                        tx2 = tm.getTransaction();
                    } catch (jakarta.transaction.SystemException se) {
                        /* Ignore: tx2 is null already */
                    }

                    /* Check transaction and database connection. */
                    if ((tx1 != null && tx1.equals(tx2))
                            && isSameConnection(dbUrl, user, passwd, dynamic, xaDataSource, connControl))
                    {
                        try {
                            /*
                             * Should not overload the meaning of closed. Change!
                             */

                            if (!c.isClosed()) {
                                // ConnectionImple does not actually implement Connection, but its
                                // concrete child classes do. See ConnectionImple javadoc.
                                conn = c;
                                conn.incrementUseCount();
                                break;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            SQLException sqlException = new SQLException(ex.getMessage());
                            sqlException.initCause(ex);
                            throw sqlException;
                        }
                    } else {
                        // no longer being used by a transaction, so let's discard. JBTM-764
                        //
                        // TODO
                        //
                        //                    if (tx1 == null)
                        //                        iterator.remove();
                    }
                }

                while (conn == null) {
                    if (conn == null) {
                        for (ConnectionImple con : _connections) {
                            if (!con.inUse() && isSameConnection(dbUrl, user, passwd, dynamic, xaDataSource, con.connectionControl())) {
                                conn = con;
                                conn.incrementUseCount();
                                break;
                            }
                        }
                    }
                    if (conn == null) {
                        if (_connections.size() == maxConnections) {
                            try {
                                _connections.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                }
            }


            if (conn == null) {
                conn = new ConnectionImple(dbUrl, info);
                /*
                * Will replace any old (closed) connection which had the
                * same connection information.
                */

                if (poolingEnabled) {
                    _connections.add(conn);
                }
            }

            // ConnectionImple does not actually implement Connection, but its
            // concrete child classes do. See ConnectionImple javadoc.
            return conn;
        }
    }

    public static void remove(ConnectionImple conn) {
        synchronized (_connections) {
            _connections.remove(conn);
        }
    }

    public static void release(ConnectionImple conn) {
        synchronized (_connections) {
            _connections.notify();
        }
    }

    private static boolean isSameConnection(String dbUrl, String user, String passwd, String dynamic, Object xaDataSource, ConnectionControl connControl) {
        return 
            dbUrl.equals(connControl.url())
            && user.equals(connControl.user())
            && passwd.equals(connControl.password())
            && dynamic.equals(connControl.dynamicClass())
            // equal ProvidedXADataSourceConnection instances should have the same data source
            && (xaDataSource == null || xaDataSource.equals(connControl.xaDataSource()));
    }

    private static Set<ConnectionImple> _connections = new HashSet<ConnectionImple>();
}
