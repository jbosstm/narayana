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
 * $Id: oracle_8_0_driver.java 2342 2006-03-30 13:06:17Z  $
 *
 * Copyright (c) 2001 Hewlett-Packard Company
 * Hewlett-Packard Company Confidential
 *
 * $Project: ArjunaCore$
 * $Revision: 2342 $
 * $Date: 2006-03-30 14:06:17 +0100 (Thu, 30 Mar 2006) $
 * $Author: $
 */

/*
 * JDBC store implementations are driver specific.
 * This version for Oracle 8.0 JDBC Drivers (OCI or Thin) ONLY.
 */

package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputBuffer;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreImple;
import com.arjuna.ats.internal.arjuna.objectstore.JDBCImple;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/**
 * An object store implementation which uses a JDBC database for
 * maintaining object states. All states are maintained within a
 * single table.
 *
 * @message com.arjuna.ats.internal.arjuna.objectstore.drivers.oracle_8_0_1 [com.arjuna.ats.internal.arjuna.objectstore.drivers.oracle_8_0_8] - oracle_8_0.read_state failed
 * @message com.arjuna.ats.internal.arjuna.objectstore.drivers.oracle_8_0_2 [com.arjuna.ats.internal.arjuna.objectstore.drivers.oracle_8_0_2] - oracle_8_0.write_state caught exception: {0}
 */

public class oracle_8_0_driver extends JDBCImple
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

					if( ! rs.next()) {
						return null; // no matching state in db
					}

					byte[] buffer = rs.getBytes(1);

					rs.close();

					if (buffer != null)
					{
						newImage = new InputObjectState(objUid, tName, buffer);
					}
					else {
					    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.drivers.oracle_8_0_1");
                    }
				}
				catch (Exception e)
				{
					throw new ObjectStoreException(e.toString());
				}
				finally
				{
					try
					{
						rs.close();
					}
					// Ignore
					catch (Exception re) {}
					freePool(pool);
				}
			}
		}
		else
			throw new ObjectStoreException("oracle_8_0.read_state - object with uid "+objUid+" has no TypeName");

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
			ResultSet rs = null;

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

					rs.updateBytes(1, b);
					rs.updateRow();

				} else {

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
					_theConnection[pool].commit();
				}

				rs.close();
				_theConnection[pool].commit();
				result = true;

			}
			catch(Exception e)
			{
			    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.drivers.oracle_8_0_2", new Object[] {e});
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
					rs.close();
				}
				// Ignore
				catch (Exception re) {}
				freePool(pool);
			}
		}
		return result;
	}

	protected void createTable (Statement stmt, String tableName) throws SQLException
	{
		stmt.executeUpdate("CREATE TABLE "+tableName+" (StateType INTEGER, TypeName VARCHAR(1024), UidString VARCHAR(255), ObjectState "+blobName+", PRIMARY KEY(UidString, StateType, TypeName))");
	}

	public String name ()
	{
		return "oracle_8_0";
	}

	private static final int  _maxStateSize = 65535;
	private String blobName = "BLOB";


}
