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
* apache_driver.java
*
* Copyright (c) 2004 Arjuna Technologies Ltd.
* Arjuna Technologies Ltd. Confidential
*
* Created on 25-Feb-2005,13:02:10 by Arnaud Simon
*
* $Id: apache_driver.java 2342 2006-03-30 13:06:17Z  $
*/
package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import com.arjuna.ats.internal.arjuna.objectstore.JDBCImple;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.logging.tsLogger;

import java.sql.*;
/**
 * An object store implementation for Derby which uses a JDBC database for
 * maintaining object states. All states are maintained within a
 * single table.
 *
 * @author Arnaud Simon <Arnaud.Simon@arjuna.com>
 * @version $Id: apache_driver.java 2342 2006-03-30 13:06:17Z  $
 */

public class apache_driver extends JDBCImple
{
    public InputObjectState read_state (Uid objUid, String tName, int ft, String tableName) throws ObjectStoreException
    {
        InputObjectState newImage = null;

        if (!storeValid())
            return newImage;

        if (tName != null)
        {
            if ((ft == ObjectStore.OS_COMMITTED) || (ft == ObjectStore.OS_UNCOMMITTED))
            {
                int pool = getPool();
                ResultSet rs = null;
                try
                {
                    PreparedStatement pstmt = _preparedStatements[pool][READ_STATE];
                    if (pstmt == null)
                    {
                        pstmt = _theConnection[pool].prepareStatement("SELECT ObjectState FROM "+tableName+" WHERE UidString = ? AND TypeName = ? AND StateType = ?");
                        _preparedStatements[pool][READ_STATE] = pstmt;
                    }

                    pstmt.setString(1, objUid.stringForm());
                    pstmt.setString(2, tName);
                    pstmt.setInt(3, ft);

                    rs = pstmt.executeQuery();

                    if(! rs.next()) {
                        return null; // no matching state in db
                    }
                    Blob myBlob = (Blob)rs.getBlob(1);
                    byte[] buffer = myBlob.getBytes(1, (int)myBlob.length());

                    if (buffer != null)
                    {
                        newImage = new InputObjectState(objUid, tName, buffer);
                    }
                    else {
                        if (tsLogger.arjLoggerI18N.isWarnEnabled())
                            tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.jdbc.oracle_1");
                    }
                }
                catch (Throwable e)
                {
                    if(retryConnection(e, pool)) {
                        return read_state(objUid, tName, ft, tableName);
                    } else {
                        throw new ObjectStoreException(e.toString());
                    }
                }
                finally
                {
                    try
                    {
                        if (rs != null)
                            rs.close();
                    }
                            // Ignore
                    catch (Exception re) {}
                    freePool(pool);
                }
            }
        }
        else
            throw new ObjectStoreException("oracle.read_state - object with uid "+objUid+" has no TypeName");

        return newImage;
    }


    public boolean write_state (Uid objUid, String tName, OutputObjectState state, int s, String tableName) throws ObjectStoreException
    {
        boolean result = false;
        int imageSize = (int) state.length();
        if (imageSize > _maxStateSize)
            throw new ObjectStoreException("Object state is too large - maximum size allowed: " + _maxStateSize);
        byte[] b = state.buffer();
        if (imageSize > 0 && storeValid())
        {
            int pool = getPool();
            try
            {
                PreparedStatement pstmt = _preparedStatements[pool][READ_WRITE_SHORTCUT];
                if (pstmt == null)
                {
                    pstmt = _theConnection[pool].prepareStatement("UPDATE "+tableName+" SET ObjectState = ? WHERE UidString = ? AND StateType = ?");
                    _preparedStatements[pool][READ_WRITE_SHORTCUT] = pstmt;
                }
                pstmt.setBytes(1, b);
                pstmt.setString(2, objUid.stringForm());
                pstmt.setInt(3, s);
                int rowcount =  pstmt.executeUpdate();
                if( rowcount > 0 )
                {
                    // update worked, we are done.
                }
                else
                {
                    // not in database, do insert:
                    PreparedStatement pstmt2 = _preparedStatements[pool][WRITE_STATE_NEW];
                    if (pstmt2 == null)
                    {
                        pstmt2 = _theConnection[pool].prepareStatement("INSERT INTO "+tableName+" (StateType,TypeName,UidString,ObjectState) VALUES (?,?,?,?)");
                        _preparedStatements[pool][WRITE_STATE_NEW] = pstmt2;
                    }
                    pstmt2.setInt(1, s);
                    pstmt2.setString(2, tName);
                    pstmt2.setString(3, objUid.stringForm());
                    pstmt2.setBytes(4, b);
                    pstmt2.executeUpdate();
                }
                result = true;
            }
            catch(Throwable e)
            {
                if(retryConnection(e, pool))
                {
                    return write_state(objUid, tName, state, s, tableName);
                } else
                {
                    if (tsLogger.arjLoggerI18N.isWarnEnabled())
                        tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.jdbc.oracle_2", new Object[] {e});
                }
            }
            finally
            {
                try
                {
                    _theConnection[pool].setAutoCommit(true);
                }
                catch(Exception e) {}
                freePool(pool);
            }
        }
        return result;
    }


    protected void createTable (Statement stmt, String tableName) throws SQLException
    {
        //stmt.executeUpdate("CREATE TABLE "+tableName+" (StateType INTEGER, TypeName VARCHAR(1024),UidString VARCHAR(255), ObjectState BLOB, CONSTRAINT "+tableName+"_pk PRIMARY KEY(UidString, StateType, TypeName))");
        stmt.executeUpdate("CREATE TABLE " + tableName + " (StateType INTEGER NOT NULL, TypeName VARCHAR(1024), UidString VARCHAR(255) NOT NULL, ObjectState BLOB, PRIMARY KEY(UidString, StateType))");

    }

    public String name ()
    {
        return "apache";
    }

    // private static final int  _maxStateSize = 65535;
    // Oracle BLOBs should be OK up to > 4 GB, but cap @ 10 MB for testing:
    private static final int _maxStateSize = 1024 * 1024 * 10;

}
