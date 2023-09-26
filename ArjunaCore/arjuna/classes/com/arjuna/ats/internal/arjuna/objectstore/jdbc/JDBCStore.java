/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreAPI;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputBuffer;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors.DirectDataSourceJDBCAccess;

import javax.sql.DataSource;

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
    private static final Map<String, JDBCImple_driver> imples = new HashMap<String, JDBCImple_driver>();
    private static final Map<String, String> storeNames = new HashMap<String, String>();

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

    public boolean allObjUids(String s, InputObjectState buff) throws ObjectStoreException {
        return allObjUids(s, buff, StateStatus.OS_UNKNOWN);
    }

    public boolean commit_state(Uid objUid, String tName) throws ObjectStoreException {
        return _theImple.commit_state(objUid, tName);
    }

    public boolean hide_state(Uid objUid, String tName) throws ObjectStoreException {
        return _theImple.hide_state(objUid, tName);
    }

    public boolean reveal_state(Uid objUid, String tName) throws ObjectStoreException {
        return _theImple.reveal_state(objUid, tName);
    }

    public int currentState(Uid objUid, String tName) throws ObjectStoreException {
        return _theImple.currentState(objUid, tName);
    }

    public InputObjectState read_committed(Uid storeUid, String tName) throws ObjectStoreException {
        return _theImple.read_state(storeUid, tName, StateStatus.OS_COMMITTED);
    }

    public InputObjectState read_uncommitted(Uid storeUid, String tName) throws ObjectStoreException {
        return _theImple.read_state(storeUid, tName, StateStatus.OS_UNCOMMITTED);
    }

    public boolean remove_committed(Uid storeUid, String tName) throws ObjectStoreException {
        return _theImple.remove_state(storeUid, tName, StateStatus.OS_COMMITTED);
    }

    public boolean remove_uncommitted(Uid storeUid, String tName) throws ObjectStoreException {
        return _theImple.remove_state(storeUid, tName, StateStatus.OS_UNCOMMITTED);
    }

    public boolean write_committed(Uid storeUid, String tName, OutputObjectState state) throws ObjectStoreException {
        return _theImple.write_state(storeUid, tName, state, StateStatus.OS_COMMITTED);
    }

    public boolean write_uncommitted(Uid storeUid, String tName, OutputObjectState state) throws ObjectStoreException {
        return _theImple.write_state(storeUid, tName, state, StateStatus.OS_UNCOMMITTED);
    }

    public boolean allObjUids(String tName, InputObjectState state, int match) throws ObjectStoreException {
        return _theImple.allObjUids(tName, state, match);
    }

    public boolean allTypes(InputObjectState foundTypes) throws ObjectStoreException {
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
     * @param jdbcStoreEnvironmentBean
     *            The environment bean containing the configuration
     * @throws ObjectStoreException
     *            In case the store environment bean was not correctly
     *            configured
     */
    public JDBCStore(ObjectStoreEnvironmentBean jdbcStoreEnvironmentBean) throws ObjectStoreException {
        this.jdbcStoreEnvironmentBean = jdbcStoreEnvironmentBean;
        DataSource ds = jdbcStoreEnvironmentBean.getJdbcDataSource();
        String connectionDetails = jdbcStoreEnvironmentBean.getJdbcAccess();
        String key;

        if (ds == null && connectionDetails == null) {
            throw new ObjectStoreException(tsLogger.i18NLogger.get_objectstore_JDBCStore_5());
        }
        String impleTableName = DEFAULT_TABLE_NAME;
        final String tablePrefix = jdbcStoreEnvironmentBean.getTablePrefix();
        if ((tablePrefix != null) && (tablePrefix.length() > 0)) {
            impleTableName = tablePrefix + impleTableName;
        }
        tableName = impleTableName;
        key = ds == null ? connectionDetails + tableName : ds + tableName;
        _theImple = imples.get(key);
        _storeName = storeNames.get(key);

        if (_theImple == null) {
            initImple(ds, connectionDetails, key);
        }
    }

    private void initImple(DataSource dataSource, String connectionDetails, String key) throws ObjectStoreException {
        try {
            JDBCAccess jdbcAccess;

            if (dataSource != null) {
                // dataSource takes priority over connectionDetails
                jdbcAccess = new DirectDataSourceJDBCAccess(dataSource);
                jdbcAccess.initialise(null);
            } else if (connectionDetails != null) {
                StringTokenizer stringTokenizer = new StringTokenizer(connectionDetails, ";");
                jdbcAccess = (JDBCAccess) Class.forName(stringTokenizer.nextToken()).newInstance();
                jdbcAccess.initialise(stringTokenizer);
            } else {
                throw new ObjectStoreException("Invalid store configuration");
            }

            _storeName = jdbcAccess.getClass().getName() + ":" + tableName;

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

            final String packagePrefix = JDBCStore.class.getName().substring(0, JDBCStore.class.getName().lastIndexOf('.')) + ".drivers.";
            Class jdbcImpleClass;
            try {
                jdbcImpleClass = Class.forName(packagePrefix + name + "_" + major + "_" + minor + "_driver");
            } catch (final ClassNotFoundException cnfe) {
                try {
                    jdbcImpleClass = Class.forName(packagePrefix + name + "_" + major + "_driver");
                } catch (final ClassNotFoundException cnfe2) {
                    jdbcImpleClass = Class.forName(packagePrefix + name + "_driver");
                }
            }

            _theImple = (com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple_driver) jdbcImpleClass.newInstance();

            _theImple.initialise(jdbcAccess, tableName, jdbcStoreEnvironmentBean);
            imples.put(key, _theImple);
            storeNames.put(key, _storeName);
        } catch (Exception e) {
            tsLogger.i18NLogger.fatal_objectstore_JDBCStore_2(_storeName, e);
            throw new ObjectStoreException(e);
        }
    }
}
