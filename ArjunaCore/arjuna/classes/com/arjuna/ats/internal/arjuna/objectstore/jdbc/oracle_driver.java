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
 * $Id: oracle_driver.java 2342 2006-03-30 13:06:17Z  $
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

/*
 * JDBC store implementation driver-specific code.
 * This version for Oracle 8.1/9.* JDBC Drivers (OCI or Thin) ONLY.
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import java.sql.Blob;
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

import oracle.sql.BLOB;

/**
 * @message com.arjuna.ats.internal.arjuna.objectstore.jdbc.oracle_1 [com.arjuna.ats.internal.arjuna.objectstore.jdbc.oracle_1] - oracle:read_state failed
 * @message com.arjuna.ats.internal.arjuna.objectstore.jdbc.oracle_2 [com.arjuna.ats.internal.arjuna.objectstore.jdbc.oracle_2] - oracle:write_state caught exception: {0}
 */

public class oracle_driver extends JDBCImple
{
    private static final int MAX_RETRIES = 10 ;
    
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
                    for(int count = 0 ; count < MAX_RETRIES ; count++)
                    {
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

                            return newImage;
        				}
        				catch (Throwable e)
        				{
                            if (count == MAX_RETRIES-1)
                            {
                                throw new ObjectStoreException(e.toString());
                            }
                            try
                            {
                                reconnect(pool) ;
                            }
                            catch (final Throwable th)
                            {
                                throw new ObjectStoreException(e.toString());
                            }
        				}
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
            return newImage;
		}
		else
			throw new ObjectStoreException("oracle.read_state - object with uid "+objUid+" has no TypeName");
	}

	
	public boolean write_state (Uid objUid, String tName, OutputObjectState state, int s, String tableName) throws ObjectStoreException
	{
		int imageSize = (int) state.length();

		if (imageSize > _maxStateSize)
			throw new ObjectStoreException("Object state is too large - maximum size allowed: " + _maxStateSize);

		byte[] b = state.buffer();

		if (imageSize > 0 && storeValid())
		{
			int pool = getPool();
			ResultSet rs = null;
			ResultSet rs3 = null;

            try
            {
                for(int count = 0 ; count < MAX_RETRIES ; count++)
                {
        			try
        			{
        				PreparedStatement pstmt = _preparedStatements[pool][READ_WRITE_SHORTCUT];
        
        				_theConnection[pool].setAutoCommit(false);
        
        				if (pstmt == null)
        				{
        					pstmt = _theConnection[pool].prepareStatement("SELECT ObjectState FROM "+tableName+" WHERE UidString = ? AND StateType = ? AND TypeName = ? FOR UPDATE", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        					_preparedStatements[pool][READ_WRITE_SHORTCUT] = pstmt;
        				}
        				pstmt.setString(1, objUid.stringForm());
        				pstmt.setInt(2, s);
        				pstmt.setString(3, tName);
        
        				rs = pstmt.executeQuery();
        
        				if( rs.next() ) {
        
        					BLOB myBlob = (BLOB)rs.getBlob(1);
        					myBlob.putBytes(1, b);
        
        				} else {
        					// not in database, do insert:
        					PreparedStatement pstmt2 = _preparedStatements[pool][WRITE_STATE_NEW];
        
        					if (pstmt2 == null)
        					{
        						pstmt2 = _theConnection[pool].prepareStatement("INSERT INTO "+tableName+" (StateType,TypeName,UidString,ObjectState) VALUES (?,?,?,empty_blob())");
        
        						_preparedStatements[pool][WRITE_STATE_NEW] = pstmt2;
        					}
        
        					pstmt2.setInt(1, s);
        					pstmt2.setString(2, tName);
        					pstmt2.setString(3, objUid.stringForm());
        
        					pstmt2.executeUpdate();
        					_theConnection[pool].commit();
        
        					PreparedStatement pstmt3 = _preparedStatements[pool][SELECT_FOR_WRITE_STATE];
        					if(pstmt3 == null) {
        						pstmt3 = _theConnection[pool].prepareStatement("SELECT ObjectState FROM "+tableName+" WHERE UidString = ? AND TypeName = ? AND StateType = ? FOR UPDATE");
        						_preparedStatements[pool][SELECT_FOR_WRITE_STATE] = pstmt3;
        					}
        
        					pstmt3.setString(1, objUid.stringForm());
        					pstmt3.setString(2, tName);
        					pstmt3.setInt(3, s);
        
        					rs3 = pstmt3.executeQuery();
        					rs3.next();
        					BLOB myBlob = (BLOB)rs3.getBlob(1);
        					myBlob.putBytes(1, b);
        				}
        
        				_theConnection[pool].commit();
                        return true ;
        
        			}
        			catch(Throwable e)
        			{
                        if (count == MAX_RETRIES-1)
                        {
                            if (tsLogger.arjLoggerI18N.isWarnEnabled()) {
                                tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.jdbc.oracle_2", new Object[] {e});
                            }
                            return false ;
                        }
                        try
                        {
                            reconnect(pool) ;
                        }
                        catch (final Throwable th)
                        {
                            throw new ObjectStoreException(e.toString());
                        }
        			}
                }
            }
			finally
			{
				try
				{
					_theConnection[pool].setAutoCommit(true);
				}
				catch(Exception e) {}
				try
				{
					if (rs != null)
						rs.close();
				}
				// Ignore
				catch (Exception re) {
				}
				try
				{
					if (rs3 != null)
						rs3.close();
				}
				// Ignore
				catch (Exception re3) {
				}
				freePool(pool);
			}
		}
		return false ;
	}


	protected void createTable (Statement stmt, String tableName) throws SQLException
	{
		stmt.executeUpdate("CREATE TABLE "+tableName+" (StateType INTEGER, TypeName VARCHAR(1024),UidString VARCHAR(255), ObjectState BLOB, CONSTRAINT "+tableName+"_pk PRIMARY KEY(UidString, StateType, TypeName))");
	}

	public String name ()
	{
		return "oracle";
	}

	// private static final int  _maxStateSize = 65535;
	// Oracle BLOBs should be OK up to > 4 GB, but cap @ 10 MB for testing:
	private static final int _maxStateSize = 1024 * 1024 * 10;

}
