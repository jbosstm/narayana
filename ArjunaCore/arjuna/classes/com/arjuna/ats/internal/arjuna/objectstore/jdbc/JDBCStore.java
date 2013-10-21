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
 * $Id: JDBCStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.StringTokenizer;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreAPI;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputBuffer;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * An object store implementation which uses a JDBC database for maintaining
 * object states. All states are maintained within a single table.
 * 
 * It is assumed that only one object will use a given instance of the
 * JDBCStore. Hence, there is no need for synchronizations.
 */

public class JDBCStore implements ObjectStoreAPI {
	protected JDBCImple_driver _theImple;
	private static final String DEFAULT_TABLE_NAME = "JBossTSTxTable";
	protected String tableName;
	protected final ObjectStoreEnvironmentBean jdbcStoreEnvironmentBean;
	private String _storeName;

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	/**
	 * Does this store need to do the full write_uncommitted/commit protocol?
	 * 
	 * @return <code>true</code> if full commit is needed, <code>false</code>
	 *         otherwise.
	 */

	public boolean fullCommitNeeded() {
		return true;
	}

	/**
	 * Some object store implementations may be running with automatic sync
	 * disabled. Calling this method will ensure that any states are flushed to
	 * disk.
	 */

	public void sync() throws java.io.SyncFailedException, ObjectStoreException {
	}

	/**
	 * Is the current state of the object the same as that provided as the last
	 * parameter?
	 * 
	 * @param u
	 *            The object to work on.
	 * @param tn
	 *            The type of the object.
	 * @param st
	 *            The expected type of the object.
	 * 
	 * @return <code>true</code> if the current state is as expected,
	 *         <code>false</code> otherwise.
	 */

	public boolean isType(Uid u, String tn, int st) throws ObjectStoreException {
		return (currentState(u, tn) == st);
	}

	public String getStoreName() {
		return _storeName;
	}

	public boolean allObjUids(String s, InputObjectState buff)
			throws ObjectStoreException {
		return allObjUids(s, buff, StateStatus.OS_UNKNOWN);
	}

	public boolean commit_state(Uid objUid, String tName)
			throws ObjectStoreException {
		return _theImple.commit_state(objUid, tName);
	}

	public boolean hide_state(Uid objUid, String tName)
			throws ObjectStoreException {
		return _theImple.hide_state(objUid, tName);
	}

	public boolean reveal_state(Uid objUid, String tName)
			throws ObjectStoreException {
		return _theImple.reveal_state(objUid, tName);
	}

	public int currentState(Uid objUid, String tName)
			throws ObjectStoreException {
		return _theImple.currentState(objUid, tName);
	}

	public InputObjectState read_committed(Uid storeUid, String tName)
			throws ObjectStoreException {
		return _theImple.read_state(storeUid, tName, StateStatus.OS_COMMITTED);
	}

	public InputObjectState read_uncommitted(Uid storeUid, String tName)
			throws ObjectStoreException {
		return _theImple
				.read_state(storeUid, tName, StateStatus.OS_UNCOMMITTED);
	}

	public boolean remove_committed(Uid storeUid, String tName)
			throws ObjectStoreException {
		return _theImple
				.remove_state(storeUid, tName, StateStatus.OS_COMMITTED);
	}

	public boolean remove_uncommitted(Uid storeUid, String tName)
			throws ObjectStoreException {
		return _theImple.remove_state(storeUid, tName,
				StateStatus.OS_UNCOMMITTED);
	}

	public boolean write_committed(Uid storeUid, String tName,
			OutputObjectState state) throws ObjectStoreException {
		return _theImple.write_state(storeUid, tName, state,
				StateStatus.OS_COMMITTED);
	}

	public boolean write_uncommitted(Uid storeUid, String tName,
			OutputObjectState state) throws ObjectStoreException {
		return _theImple.write_state(storeUid, tName, state,
				StateStatus.OS_UNCOMMITTED);
	}

	public boolean allObjUids(String tName, InputObjectState state, int match)
			throws ObjectStoreException {
		return _theImple.allObjUids(tName, state, match);
	}

	public boolean allTypes(InputObjectState foundTypes)
			throws ObjectStoreException {
		return _theImple.allTypes(foundTypes);
	}

	public synchronized void packInto(OutputBuffer buff) throws IOException {
		buff.packString(tableName);
	}

	public synchronized void unpackFrom(InputBuffer buff) throws IOException {
		this.tableName = buff.unpackString();
	}

	/**
	 * Create a new JDBCStore
	 * 
	 * @param jdbcStoreEnvironmentBean The environment bean containing the configuration
	 * @throws ObjectStoreException In case the store environment bean was not correctly configured
     * @throws {@link FatalError} In case the configured store cannot be connected to
	 */
	public JDBCStore(ObjectStoreEnvironmentBean jdbcStoreEnvironmentBean)
			throws ObjectStoreException {
		this.jdbcStoreEnvironmentBean = jdbcStoreEnvironmentBean;
		String connectionDetails = jdbcStoreEnvironmentBean.getJdbcAccess();
		if (connectionDetails == null) {
			throw new ObjectStoreException(
					tsLogger.i18NLogger.get_objectstore_JDBCStore_5());
		}

		try {
			StringTokenizer stringTokenizer = new StringTokenizer(
					connectionDetails, ";");
			JDBCAccess jdbcAccess = (JDBCAccess) Class.forName(
					stringTokenizer.nextToken()).newInstance();
			jdbcAccess.initialise(stringTokenizer);

			String impleTableName = getDefaultTableName();
			final String tablePrefix = jdbcStoreEnvironmentBean
					.getTablePrefix();
			if ((tablePrefix != null) && (tablePrefix.length() > 0)) {
				impleTableName = tablePrefix + impleTableName;
			}
			tableName = impleTableName;
			_storeName = jdbcAccess.getClass().getName() + ":" + tableName;

			final com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple_driver jdbcImple;
			Connection connection = jdbcAccess.getConnection();
			String name;
			int major;
			int minor;
			try {
				DatabaseMetaData md = connection.getMetaData();
				name = md.getDriverName();
				major = md.getDriverMajorVersion();
				minor = md.getDriverMinorVersion();
			} finally {
				connection.close();
			}

			/*
			 * Check for spaces in the name - our implementation classes are
			 * always just the first part of such names.
			 */

			int index = name.indexOf(' ');

			if (index != -1)
				name = name.substring(0, index);

			name = name.replaceAll("-", "_");

			name = name.toLowerCase();

			final String packagePrefix = getClass().getName().substring(0,
					getClass().getName().lastIndexOf('.'))
					+ ".drivers.";
			Class jdbcImpleClass = null;
			try {
				jdbcImpleClass = Class.forName(packagePrefix + name + "_"
						+ major + "_" + minor + "_driver");
			} catch (final ClassNotFoundException cnfe) {
				try {
					jdbcImpleClass = Class.forName(packagePrefix + name + "_"
							+ major + "_driver");
				} catch (final ClassNotFoundException cnfe2) {
					jdbcImpleClass = Class.forName(packagePrefix + name
							+ "_driver");
				}
			}
			jdbcImple = (com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple_driver) jdbcImpleClass
					.newInstance();

			jdbcImple.initialise(jdbcAccess, impleTableName,
					jdbcStoreEnvironmentBean);
			_theImple = jdbcImple;
		} catch (Exception e) {
			tsLogger.i18NLogger.fatal_objectstore_JDBCStore_2(_storeName, e);
			throw new ObjectStoreException(e);
		}
	}

	protected String getDefaultTableName() {
		return DEFAULT_TABLE_NAME;
	}

}
