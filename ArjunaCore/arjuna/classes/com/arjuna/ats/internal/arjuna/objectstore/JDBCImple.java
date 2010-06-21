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
 * $Id: JDBCImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;

import java.sql.*;
import java.util.Hashtable;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

import java.io.IOException;

/**
 * An object store implementation which uses a JDBC database for maintaining
 * object states. All states are maintained within a single table.
 */

public abstract class JDBCImple
{

	public final boolean storeValid()
	{
		return _isValid;
	}

	public boolean commit_state(Uid objUid, String typeName, String tableName)
			throws ObjectStoreException
	{
		boolean result = false;
		boolean cleanup = true;

		/* Bail out if the object store is not set up */

		if (!storeValid())
			return false;

		if (typeName != null)
		{
			int currState = currentState(objUid, typeName, tableName);
			int pool = getPool();

			try
			{
				// remove the old committed state, if any:
				PreparedStatement pstmt = _preparedStatements[pool][PRECOMMIT_CLEANUP];
				if (pstmt == null)
				{
					pstmt = _theConnection[pool]
							.prepareStatement("DELETE FROM "
									+ tableName
									+ " WHERE UidString = ? AND TypeName = ? AND StateType = "
									+ StateStatus.OS_COMMITTED);
					_preparedStatements[pool][PRECOMMIT_CLEANUP] = pstmt;
				}
				pstmt.setString(1, objUid.stringForm());
				pstmt.setString(2, typeName);
				pstmt.executeUpdate();
				// done cleanup

				// now do the commit itself:
				pstmt = _preparedStatements[pool][COMMIT_STATE];
				if (pstmt == null)
				{
					pstmt = _theConnection[pool]
							.prepareStatement("UPDATE "
									+ tableName
									+ " SET StateType = ? WHERE UidString = ? AND TypeName = ? AND StateType = ?");
					_preparedStatements[pool][COMMIT_STATE] = pstmt;
				}

				if (currState == StateStatus.OS_UNCOMMITTED)
				{
					pstmt.setInt(1, StateStatus.OS_COMMITTED);
				}
				else if (currState == StateStatus.OS_UNCOMMITTED_HIDDEN)
				{
					pstmt.setInt(1, StateStatus.OS_COMMITTED_HIDDEN);
				}
				else
				{
					throw new ObjectStoreException("can't commit object "
							+ objUid + " in state " + currState);
				}

				pstmt.setString(2, objUid.stringForm());
				pstmt.setString(3, typeName);
				pstmt.setInt(4, currState);

				int rowcount = pstmt.executeUpdate();

				if (rowcount > 0)
				{
					result = true;
				}
			}
			catch (Throwable e)
			{
			    e.printStackTrace();
			    
			    if (true)
			        return false;
			    
				cleanup = false;
				if (retryConnection(e, pool))
				{
					return commit_state(objUid, typeName, tableName);
				}
				else
				{
					throw new ObjectStoreException(e.toString(), e);
				}
			}
			finally
			{
				if (cleanup)
					freePool(pool);
			}
		}
		else
			throw new ObjectStoreException("commit_state - object with uid "
					+ objUid + " has no TypeName");

		return result;
	}

	/**
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_1
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_1] -
	 *          hide_state caught exception: {0}
	 */
	public boolean hide_state(Uid objUid, String typeName, String tableName)
			throws ObjectStoreException
	{
		boolean hiddenOk = true;
		boolean cleanup = true;

		/* Bail out if the object store is not set up */

		if (storeValid())
		{
			int state = currentState(objUid, typeName, tableName);
			int pool = getPool();
			PreparedStatement pstmt = null;

			try
			{
				pstmt = _preparedStatements[pool][HIDE_STATE];

				if (pstmt == null)
				{
					pstmt = _theConnection[pool]
							.prepareStatement("UPDATE "
									+ tableName
									+ " SET StateType = ? WHERE UidString = ? AND TypeName = ? AND StateType = ?");

					_preparedStatements[pool][HIDE_STATE] = pstmt;
				}
			}
			catch (Exception e) {
                tsLogger.i18NLogger.warn_objectstore_JDBCImple_1(e);

                freePool(pool);
                return false;
            }

			try
			{
				switch (state)
				{
				case StateStatus.OS_UNCOMMITTED_HIDDEN:
				case StateStatus.OS_COMMITTED_HIDDEN:
					break;
				case StateStatus.OS_UNCOMMITTED:
				{
					pstmt.setInt(1, StateStatus.OS_UNCOMMITTED_HIDDEN);
					pstmt.setString(2, objUid.stringForm());
					pstmt.setString(3, typeName);
					pstmt.setInt(4, state);
					pstmt.executeUpdate();
				}
					break;
				case StateStatus.OS_COMMITTED:
				{
					pstmt.setInt(1, StateStatus.OS_COMMITTED_HIDDEN);
					pstmt.setString(2, objUid.stringForm());
					pstmt.setString(3, typeName);
					pstmt.setInt(4, state);
					pstmt.executeUpdate();
				}
					break;
				default:
					hiddenOk = false;
				}
			}
			catch (Throwable e)
			{
				cleanup = false;
				if (retryConnection(e, pool))
				{
					hide_state(objUid, typeName, tableName);
				}
				else
				{
					throw new ObjectStoreException(e.toString(), e);
				}
			}
			finally
			{
				if (cleanup)
					freePool(pool);
			}
		}
		else
		{
			hiddenOk = false;
		}

		return hiddenOk;
	}

	/**
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_2
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_2] -
	 *          reveal_state caught exception: {0}
	 */
	public boolean reveal_state(Uid objUid, String typeName, String tableName)
			throws ObjectStoreException
	{
		boolean revealedOk = true;
		boolean cleanup = true;

		if (storeValid())
		{
			int state = currentState(objUid, typeName, tableName);

			int pool = getPool();

			PreparedStatement pstmt = null;
			try
			{
				pstmt = _preparedStatements[pool][REVEAL_STATE];

				if (pstmt == null)
				{
					pstmt = _theConnection[pool]
							.prepareStatement("UPDATE "
									+ tableName
									+ " SET StateType = ? WHERE UidString = ? AND AND TypeName = ? StateType = ?");
					_preparedStatements[pool][REVEAL_STATE] = pstmt;
				}
			}
			catch (Exception e) {
                tsLogger.i18NLogger.warn_objectstore_JDBCImple_2(e);

                freePool(pool);
                return false;
            }

			try
			{
				switch (state)
				{
				case StateStatus.OS_UNCOMMITTED_HIDDEN:
				{
					pstmt.setInt(1, StateStatus.OS_UNCOMMITTED);
					pstmt.setString(2, objUid.stringForm());
					pstmt.setString(3, typeName);
					pstmt.setInt(4, state);
					pstmt.executeUpdate();
				}
					break;
				case StateStatus.OS_COMMITTED_HIDDEN:
				{
					pstmt.setInt(1, StateStatus.OS_COMMITTED);
					pstmt.setString(2, objUid.stringForm());
					pstmt.setString(3, typeName);
					pstmt.setInt(4, state);
					pstmt.executeUpdate();
				}
					break;
				case StateStatus.OS_COMMITTED:
				case StateStatus.OS_UNCOMMITTED:
					break;
				default:
					revealedOk = false;
				}
			}
			catch (Throwable e)
			{
				cleanup = false;
				if (retryConnection(e, pool))
				{
					reveal_state(objUid, typeName, tableName);
				}
				else
				{
					throw new ObjectStoreException(e.toString(), e);
				}
			}
			finally
			{
				if (cleanup)
					freePool(pool);
			}
		}
		else
		{
			revealedOk = false;
		}

		return revealedOk;
	}

	/**
	 * currentState - determine the current state of an object. State search is
	 * ordered OS_UNCOMMITTED, OS_UNCOMMITTED_HIDDEN, OS_COMMITTED,
	 * OS_COMMITTED_HIDDEN
	 *
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_3
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_3] -
	 *          currentState caught exception: {0}
	 */
	public int currentState(Uid objUid, String typeName, String tableName)
			throws ObjectStoreException
	{
		int theState = StateStatus.OS_UNKNOWN;
		ResultSet rs = null;
		boolean cleanup = true;

		if (storeValid())
		{
			int pool = getPool();
			try
			{
				PreparedStatement pstmt = _preparedStatements[pool][CURRENT_STATE];

				if (pstmt == null)
				{
					pstmt = _theConnection[pool]
							.prepareStatement("SELECT StateType, UidString FROM "
									+ tableName
									+ " WHERE UidString = ? AND TypeName = ?");
					_preparedStatements[pool][CURRENT_STATE] = pstmt;
				}
				
				pstmt.setString(1, objUid.stringForm());
				pstmt.setString(2, typeName);

				rs = pstmt.executeQuery();

				// we may have multiple states. need to sort out the order of
				// precedence
				// without making multiple round trips out to the db. this gets
				// a bit messy:

				boolean have_OS_UNCOMMITTED = false;
				boolean have_OS_UNCOMMITTED_HIDDEN = false;
				boolean have_OS_COMMITTED = false;
				boolean have_OS_COMMITTED_HIDDEN = false;

				while (rs.next())
				{
					int stateStatus = rs.getInt(1);

					switch (stateStatus)
					{
					case StateStatus.OS_UNCOMMITTED:
						have_OS_UNCOMMITTED = true;
						break;
					case StateStatus.OS_COMMITTED:
						have_OS_COMMITTED = true;
						break;
					case StateStatus.OS_COMMITTED_HIDDEN:
						have_OS_COMMITTED_HIDDEN = true;
						break;
					case StateStatus.OS_UNCOMMITTED_HIDDEN:
						have_OS_UNCOMMITTED_HIDDEN = true;
						break;
					}
				}

				// examine in reverse order:
				if (have_OS_COMMITTED_HIDDEN)
				{
					theState = StateStatus.OS_COMMITTED_HIDDEN;
				}
				if (have_OS_COMMITTED)
				{
					theState = StateStatus.OS_COMMITTED;
				}
				if (have_OS_UNCOMMITTED_HIDDEN)
				{
					theState = StateStatus.OS_UNCOMMITTED_HIDDEN;
				}
				if (have_OS_UNCOMMITTED)
				{
					theState = StateStatus.OS_UNCOMMITTED;
				}
			}
			catch (Throwable e)
			{
				cleanup = false;
				try
				{
					if (rs != null)
						rs.close();
				}
				catch (SQLException re)
				{
					// Just in case it's already closed
				}
				if (retryConnection(e, pool))
				{
					return currentState(objUid, typeName, tableName);
				}
				else
				{
                    tsLogger.i18NLogger.warn_objectstore_JDBCImple_3(e);

					return StateStatus.OS_UNKNOWN;
				}
			}
			finally
			{
				if (cleanup)
				{
					try
					{
						if (rs != null)
							rs.close();
					}
					catch (SQLException e)
					{
						// Just in case it's already closed
					}
					freePool(pool);
				}
			}
		}

		return theState;
	}

	/**
	 * allObjUids - Given a type name, return an ObjectState that contains all
	 * of the uids of objects of that type.
	 *
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_4
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_4] -
	 *          allObjUids caught exception: {0}
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_5
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_5] -
	 *          allObjUids - pack of Uid failed: {0}
	 */
	public boolean allObjUids(String typeName, InputObjectState state,
			int match, String tableName) throws ObjectStoreException
	{
		int pool = getPool();

		try
		{
			OutputObjectState store = new OutputObjectState();
			Statement stmt = _theConnection[pool].createStatement();
			ResultSet rs = null;

			try
			{
				/*
				 * Not used enough to warrant a PreparedStatement.
				 */
				rs = stmt.executeQuery("SELECT DISTINCT UidString FROM "
						+ tableName + " WHERE TypeName = '" + typeName + "'");

				boolean finished = false;

				while (!finished && rs.next())
				{
					Uid theUid = null;

					try
					{
						theUid = new Uid(rs.getString(1));
						UidHelper.packInto(theUid, store);
					}
					catch (IOException ex) {
                        tsLogger.i18NLogger.warn_objectstore_JDBCImple_5(ex);

                        return false;
                    }
					catch (Exception e) {
                        tsLogger.i18NLogger.warn_objectstore_JDBCImple_4(e);

                        finished = true;
                    }
				}
			}
			catch (Exception e) {
                tsLogger.i18NLogger.warn_objectstore_JDBCImple_4(e);
            }
			finally
			{
				try
				{
					if (rs != null)
						rs.close();
				}
				catch (SQLException e)
				{
					// Just in case it's already closed
				}
				try
				{
					if (stmt != null)
						stmt.close();
				}
				catch (SQLException e)
				{
					// Just in case it's already closed
				}
			}

			try
			{
			    UidHelper.packInto(Uid.nullUid(), store);
			}
			catch (IOException e)
			{
				throw new ObjectStoreException("allObjUids - could not pack end of list Uid.", e);
			}

			state.setBuffer(store.buffer());

			store = null;

			return true;
		}
		catch (Exception e) {
            tsLogger.i18NLogger.warn_objectstore_JDBCImple_4(e);
        }
		finally
		{
			freePool(pool);
		}

		return false;
	}

	/**
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_6
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_6] -
	 *          allTypes caught exception: {0}
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_7
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_7] -
	 *          allTypes - pack of Uid failed:{0}
	 * @message com.arjuna.ats.internal.arjuna.objectstore.packProblem
	 *          [com.arjuna.ats.internal.arjuna.objectstore.packProblem] -
	 *          allTypes - could not pack end of list string.
	 */
	public boolean allTypes(InputObjectState foundTypes, String tableName)
			throws ObjectStoreException
	{
		int pool = getPool();

		try
		{
			OutputObjectState store = new OutputObjectState();
			Statement stmt = _theConnection[pool].createStatement();
			ResultSet rs = null;

			try
			{
				/*
				 * Not used enough to warrant a PreparedStatement.
				 */
				rs = stmt.executeQuery("SELECT DISTINCT TypeName FROM "
						+ tableName);

				boolean finished = false;

				while (!finished && rs.next())
				{
					try
					{
						String type = rs.getString(1);
						store.packString(type);
					}
					catch (IOException ex) {
                        tsLogger.i18NLogger.warn_objectstore_JDBCImple_7(ex);

                        return false;
                    }
					catch (Exception e) {
                        tsLogger.i18NLogger.warn_objectstore_JDBCImple_6(e);

                        finished = true;
                    }
				}
			}
			catch (Exception e) {
                tsLogger.i18NLogger.warn_objectstore_JDBCImple_6(e);
            }
			finally
			{
				try
				{
					if (rs != null)
						rs.close();
				}
				catch (SQLException e)
				{
					// Just in case it's already closed
				}
				try
				{
					if (stmt != null)
						stmt.close();
				}
				catch (SQLException e)
				{
					// Just in case it's already closed
				}
			}

			try
			{
				store.packString("");
			}
			catch (IOException e)
			{
				throw new ObjectStoreException(tsLogger.i18NLogger.get_objectstore_packProblem(), e);
			}

			foundTypes.setBuffer(store.buffer());

			return true;
		}
		catch (Exception e) {
            tsLogger.i18NLogger.warn_objectstore_JDBCImple_6(e);
        }
		finally
		{
			freePool(pool);
		}

		return false;
	}

	/**
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_8
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_8] -
	 *          remove_state caught exception: {0}
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_9
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_9] -
	 *          remove_state() attempted removal of {0} state for object with
	 *          uid {1}
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_10
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_10] -
	 *          remove_state - type() operation of object with uid {0} returns
	 *          NULL
	 */
	public boolean remove_state(Uid objUid, String name, int ft,
			String tableName) throws ObjectStoreException
	{
		boolean removeOk = false;
		boolean cleanup = true;

		if (!storeValid())
			return false;

		if (name != null)
		{
			if ((ft == StateStatus.OS_COMMITTED)
					|| (ft == StateStatus.OS_UNCOMMITTED))
			{
				int pool = getPool();

				try
				{
					PreparedStatement pstmt = _preparedStatements[pool][REMOVE_STATE];

					if (pstmt == null)
					{
						pstmt = _theConnection[pool]
								.prepareStatement("DELETE FROM "
										+ tableName
										+ " WHERE UidString = ? AND TypeName = ? AND StateType = ?");

						_preparedStatements[pool][REMOVE_STATE] = pstmt;
					}

					pstmt.setString(1, objUid.stringForm());
					pstmt.setString(2, name);
					pstmt.setInt(3, ft);
					if (pstmt.executeUpdate() > 0)
					{
						removeOk = true;
					}
				}
				catch (Throwable e)
				{
					cleanup = false;
					if (retryConnection(e, pool))
					{
						return remove_state(objUid, name, ft, tableName);
					}
					else {
                        tsLogger.i18NLogger.warn_objectstore_JDBCImple_8(e);

                        removeOk = false;
                    }
				}
				finally
				{
					if (cleanup)
						freePool(pool);
				}
			}
			else {
                removeOk = false;
                // can only remove (UN)COMMITTED objs
                tsLogger.i18NLogger.warn_objectstore_JDBCImple_9(Integer.toString(ft), objUid);
            }
		}
		else {
            removeOk = false;

            tsLogger.i18NLogger.warn_objectstore_JDBCImple_10(objUid);
        }

		return removeOk;
	}

    /**
     * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_readfailed [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_readfailed] - JDBCImple:read_state failed
     */
	public InputObjectState read_state (Uid objUid, String tName, int ft, String tableName) throws ObjectStoreException
	{
		InputObjectState newImage = null;

		if (!storeValid())
			return newImage;

		if (tName != null)
		{
			if ((ft == StateStatus.OS_COMMITTED) || (ft == StateStatus.OS_UNCOMMITTED))
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

					byte[] buffer = rs.getBytes(1);

					if (buffer != null)
					{
						newImage = new InputObjectState(objUid, tName, buffer);
					}
					else {
                        tsLogger.i18NLogger.warn_objectstore_JDBCImple_readfailed();
                    }
				}
				catch (Throwable e)
				{
				    e.printStackTrace();
				    
					if(retryConnection(e, pool)) {
						return read_state(objUid, tName, ft, tableName);
					} else {
						throw new ObjectStoreException(e.toString(), e);
					}
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
			throw new ObjectStoreException("JDBCImple.read_state - object with uid "+objUid+" has no TypeName");

		return newImage;
	}


    /**
     * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_writefailed [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_writefailed] - JDBCImple:write_state caught exception: {0}
     */
    public boolean write_state (Uid objUid, String tName, OutputObjectState state, int s, String tableName) throws ObjectStoreException
	{
		boolean result = false;

		int imageSize = (int) state.length();

		if (imageSize > getMaxStateSize())
			throw new ObjectStoreException("Object state is too large - maximum size allowed: " + getMaxStateSize());

		byte[] b = state.buffer();

		if (imageSize > 0 && storeValid())
		{
			int pool = getPool();
			ResultSet rs = null;

			try
			{
				PreparedStatement pstmt = _preparedStatements[pool][READ_WRITE_SHORTCUT];

				if (pstmt == null)
				{
					pstmt = _theConnection[pool].prepareStatement("SELECT ObjectState FROM "+tableName+" WHERE UidString = ? AND StateType = ? AND TypeName = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

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
				}

				result = true;
			}
			catch(Throwable e)
			{
				if(retryConnection(e, pool)) {
					return write_state(objUid, tName, state, s, tableName);
				} else {
                    tsLogger.i18NLogger.warn_objectstore_JDBCImple_writefailed(e);
                }
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
		return result;
	}

	/**
	 * Set up the store for use.
	 *
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_11
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_11] -
	 *          invalid initial pool size: {0}
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_12
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_12] -
	 *          invalid maximum pool size: {0}
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_13
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_13] -
	 *          initialise caught exception: {0}
	 */
	public boolean initialise(Connection conn, JDBCAccess jdbcAccess,
			String tableName) throws SQLException
	{
        _poolSizeInit = arjPropertyManager.getObjectStoreEnvironmentBean().getJdbcPoolSizeInitial();
        if (_poolSizeInit < 1) {
            tsLogger.i18NLogger.warn_objectstore_JDBCImple_11(Integer.toString(_poolSizeInit));
            _poolSizeInit = 1;
        }

        _poolSizeMax = arjPropertyManager.getObjectStoreEnvironmentBean().getJdbcPoolSizeMaximum();
        if (_poolSizeMax < _poolSizeInit) {
            tsLogger.i18NLogger.warn_objectstore_JDBCImple_12(Integer.toString(_poolSizeMax));
            _poolSizeMax = _poolSizeInit;
        }

		_poolPutConn = arjPropertyManager.getObjectStoreEnvironmentBean().isJdbcPoolPutConnections();
        
		_jdbcAccess = jdbcAccess;
		_theConnection = new Connection[_poolSizeMax];
		_theConnection[0] = conn;
        conn.setAutoCommit(true);

		try
		{
			for (int i = 1; i < _poolSizeInit; i++)
			{
				_theConnection[i] = _jdbcAccess.getConnection();
				_theConnection[i].setAutoCommit(true);
			}
		}
		catch (Exception e) {
            tsLogger.i18NLogger.warn_objectstore_JDBCImple_13(e);

            _isValid = false;
            return _isValid;
        }

		for (int i = _poolSizeInit; i < _poolSizeMax; i++)
		{
			_theConnection[i] = null;
		}
		if (_inUse == null)
		{
			_inUse = new boolean[_poolSizeMax];
		}

		_preparedStatements = new PreparedStatement[_poolSizeMax][];

		for (int i = 0; i < _poolSizeMax; i++)
		{
			_preparedStatements[i] = new PreparedStatement[STATEMENT_SIZE];

			for (int j = 0; j < STATEMENT_SIZE; j++)
			{
				_preparedStatements[i][j] = null;
			}
		}

		try
		{
			Statement stmt = _theConnection[0].createStatement();

			// table [type, object UID, format, blob]

			// Need some management interface to delete the table!

			if (jdbcAccess.dropTable())
			{
				try
				{
					stmt.executeUpdate("DROP TABLE " + tableName);
				}
				catch (SQLException ex)
				{
					// don't want to print error - chances are it
					// just reports that the table does not exist
					// ex.printStackTrace();
				}
			}

			try
			{
				createTable(stmt, tableName);
			}
			catch (SQLException ex)
			{
				// assume this is reporting that the table already exists:
			}

			_isValid = true;
		}
		catch (Exception e) {
            tsLogger.i18NLogger.warn_objectstore_JDBCImple_13(e);

            _isValid = false;
        }

		return _isValid;
	}

	/**
	 * Add a new table to an existing implementation.
	 *
	 */
	protected void addTable(String tableName) throws Exception
	{
		int pool = getPool();
		Statement stmt = _theConnection[pool].createStatement();

		try
		{
			createTable(stmt, tableName);
		}
		catch (SQLException ex)
		{
			// assume this is reporting that the table already exists:
		}
		finally
		{
			freePool(pool);
		}

	}

	/**
	 * Use implementation-specific code to create the store table. Called from
	 * initialise() and addTable(), above.
	 */
	protected abstract void createTable(Statement stmt, String tableName)
			throws SQLException;

	public abstract String name();

    protected abstract int getMaxStateSize();

	// protected abstract boolean exists (String state);

	/**
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_14
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_14] -
	 *          getState caught exception: {0}
	 */
    
	protected final int getState(String state)
	{
		try
		{
			Integer st = (Integer) stateCache.get(state);

			if (st != null)
			{
				return st.intValue();
			}
		}
		catch (Exception ex) {
            tsLogger.i18NLogger.warn_objectstore_JDBCImple_14(ex);
        }

		return StateStatus.OS_UNKNOWN;
	}

	protected final void addToCache(Uid state, int status)
	{
		if (shareStatus == StateType.OS_UNSHARED)
		{
			stateCache.put(state, new Integer(status));
		}
	}

	protected final void removeFromCache(String state)
	{
		removeFromCache(state, true);
	}

	/**
	 * Print a warning if the file to be removed is not in the cache.
	 *
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_15
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_15] -
	 *          removeFromCache - no entry for {0}
	 */
	protected final void removeFromCache(String state, boolean warn)
	{
		if (shareStatus == StateType.OS_UNSHARED)
		{
			if ((stateCache.remove(state) == null) && warn) {
                tsLogger.i18NLogger.warn_objectstore_JDBCImple_15(state);
            }
		}
	}

	final void setShareStatus(int status)
	{
		shareStatus = status;
	}

	/**
	 * retryConnection. Called in exeption handlers where the problem may be due
	 * to use of a stale (broken) cached connection. If this is the case, we
	 * re-establish the connection before returning.
	 *
	 * @param e
	 *            The exception, which may be due to a bad connection.
	 * @param pool
	 *            The pooled connection which was in use when the exception was
	 *            thrown and which is therfore suspect.
	 * @return true if the connection was reestablished (in which case it is
	 *         worth retrying the calling function), false is a broken
	 *         connection was unlikely to be the problem.
	 */
	protected boolean retryConnection(Throwable e, int pool)
	{
		if (e instanceof SQLException)
		{
			// To do: Look for specific driver error codes here...
			try
			{
				reconnect(pool);
			}
			catch (Exception e1)
			{
				return false;
			}
			synchronized (_inUse)
			{
				_inUse[pool] = true;
			}
			freePool(pool);
			return true;
		}

		return false;
	}

	/**
	 * reconnect(int pool): re-establish a potentially failed cached connection.
	 */
	protected void reconnect(int pool) throws SQLException
	{
		Connection newConnection = _theConnection[pool];
		_theConnection[pool] = null;

		// just in case the connection is still live,
		// attempt to clean it up nicely:
		try
		{
			newConnection.close();
		}
		catch (SQLException e)
		{
		}
		_jdbcAccess.putConnection(newConnection);

		// release the statements associated with the closed
		// connection so they dont get used by mistake...
		for (int i = 0; i < STATEMENT_SIZE; i++)
		{
			_preparedStatements[pool][i] = null;
		}

		// re-establish the connection:
		newConnection = _jdbcAccess.getConnection();
		try
		{
			newConnection.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			newConnection = null;
			throw e;
		}
		_theConnection[pool] = newConnection;
	}

	/**
	 * Allocate a database connection from the pool: Walks the array and
	 * allocates the first available connection. If non are free, waits before
	 * trying again.
	 *
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_16
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_16] -
	 *          getPool caught exception: {0}
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_17
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_17] -
	 *          getPool - interrupted while waiting for a free connection
	 */
	protected int getPool()
	{
		int i = 0;
		int rtn = -1;

		synchronized (_inUse)
		{
			while (true)
			{
				if (!_inUse[i])
				{
					if (_theConnection[i] == null) /*
													 * i >= _poolSizeInit && i <
													 * _poolSizeMax
													 */
					{
						try
						{
							_theConnection[i] = _jdbcAccess.getConnection();
							_inUse[i] = true;
							rtn = i;
							break;
						}
						catch (Exception e) {
                            tsLogger.i18NLogger.warn_objectstore_JDBCImple_16(e);
                        }
					}
					else
					{
						_inUse[i] = true;
						rtn = i;
						break;
					}
				}

				i++;
				if (i == _poolSizeMax)
				{
					i = 0;
					try
					{
						_inUse.wait();
					}
					catch (InterruptedException ie)
					{
						tsLogger.i18NLogger.info_objectstore_JDBCImple_17();
					}
				}
			}
		}

		return rtn;
	}

	/**
	 * return a connection to the free pool, optionally closing it.
	 *
	 * @message com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_18
	 *          [com.arjuna.ats.internal.arjuna.objectstore.JDBCImple_18] -
	 *          freePool - freeing a connection which is already free!
	 */
	protected void freePool(int pool)
	{
		/*
		 * try { _theConnection[pool].commit(); } catch(Exception e) {}
		 */

		synchronized (_inUse)
		{
			if (_inUse[pool] == false) {
                tsLogger.i18NLogger.warn_objectstore_JDBCImple_18();
            }
			_inUse[pool] = false;
			_inUse.notifyAll();
		}
	}

	private int shareStatus;

	private Hashtable stateCache = new Hashtable();

	protected JDBCAccess _jdbcAccess = null;

	protected Connection[] _theConnection = null;

	protected boolean _isValid = false;

	protected PreparedStatement[][] _preparedStatements = null;

	/*
	 * IMPORTANT: remember to update if we add more prepared statements!
	 */
	protected static final int COMMIT_STATE = 0;

	protected static final int HIDE_STATE = 1;

	protected static final int REVEAL_STATE = 2;

	protected static final int CURRENT_STATE = 3;

	protected static final int READ_STATE = 4;

	protected static final int REMOVE_STATE = 5;

	protected static final int WRITE_STATE_NEW = 6;

	protected static final int WRITE_STATE_EXISTING = 7;

	protected static final int SELECT_FOR_WRITE_STATE = 8;

	protected static final int READ_WRITE_SHORTCUT = 9;

	protected static final int PRECOMMIT_CLEANUP = 10;

	// size:
	protected static final int STATEMENT_SIZE = 11;

	// record the status of each connection in the pool:
	protected boolean[] _inUse = null;

	protected int _poolSizeInit = 1; /* Initial pool size */

	protected int _poolSizeMax = 1; /* Maximum pool size */

	protected boolean _poolPutConn = false; /*
											 * Return (putConnection()) after
											 * use?
											 */
}
