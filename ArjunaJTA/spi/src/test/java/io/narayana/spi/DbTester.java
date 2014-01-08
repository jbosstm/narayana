/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package io.narayana.spi;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import io.narayana.spi.util.DbProps;
import io.narayana.spi.util.*;
import org.junit.Assert;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class DbTester {
    private final int NROWS = 3;

    private Map<String, Connection> connections;
    private Map<String, Integer> counts;
    private String fault;

    public DbTester(boolean clearTables) throws SQLException, InitializationException {
        Map<String, DbProps> dbConfigs = new DbProps().getConfig(DbProps.DB_PROPERTIES_NAME);
        connections = new HashMap<String, Connection>();

        for (DbProps props : dbConfigs.values())
            connections.put(props.getBinding(),  getDataSource(props.getBinding()).getConnection());

        createTables(clearTables);
        fault = System.getProperty("spitest.fault", "");

        counts = new HashMap<String, Integer>(connections.size());

        for (Map.Entry<String, Connection> entry : connections.entrySet())
            counts.put(entry.getKey(), countRows(entry.getValue(), "CEYLONKV"));
    }

    public DbTester() throws SQLException, InitializationException {
        this(true);
    }

    public void clearCounts() {
        for (String db : counts.keySet())
            counts.put(db, 0);
    }

    public void doInserts() throws SQLException, NamingException, RollbackException, SystemException {
        /* for testing recovery use a sorted set to ensure that postresql is enlisted into any transaction after h2
         * and if fault injection is required then the dummy resource is enlisted before postresql
         */
        SortedSet<String> keys = new TreeSet<String>(connections.keySet());

        if ("XA_RBROLLBACK".equalsIgnoreCase(fault)) {
            // the first participant will throw a rollback exception during the commit phase resulting in a transaction rollback
            injectFault(ASFailureType.XARES_COMMIT, ASFailureMode.XAEXCEPTION, "XA_RBROLLBACK");
        }

        for (String key : keys) {
            Connection connection = connections.get(key);

            if ("HALT".equalsIgnoreCase(fault) && "postgresql".equals(key)) {
                System.out.println("testXADSWithFaults: halting during phase 2");
                injectFault(ASFailureType.XARES_COMMIT, ASFailureMode.HALT, "");
            }

            for (int i = 0; i < NROWS; i++)
                insertTable(connection, "k" + i, "v" + i);

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM CEYLONKV");
            ResultSet rs = statement.executeQuery();

            printResultSet(key, rs, "key", "val");

            rs.close();
            statement.close();
        }
    }

    public String getFault() {
        return fault;
    }

    public void assertRowCounts(boolean committed) throws SQLException {
        assertRowCounts(committed ? NROWS : 0);
    }

    public void assertRowCounts(int extraRows) throws SQLException {
        for (Map.Entry<String, Connection> entry : connections.entrySet()) {
            Assert.assertEquals(entry.getKey(), counts.get(entry.getKey()) + extraRows, countRows(connections.get(entry.getKey()), "CEYLONKV"));
        }
    }

    public void dropTables() throws SQLException {
        dropTables(connections);
    }

    public void createTables(boolean clearTables) throws SQLException {
        String sql = "CREATE TABLE CEYLONKV " +
                "(key VARCHAR(255) not NULL, " +
                " val VARCHAR(255), " +
                " PRIMARY KEY ( key ))";

        for (Connection connection : connections.values()) {
            Statement statement = connection.createStatement();

            try {
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                // ignore
            }

            if (clearTables)
                statement.executeUpdate("delete from CEYLONKV");

            statement.close();
        }
    }

    private DataSource getDataSource(String bindingName) throws InitializationException, SQLException {
        try {
            return (DataSource) new InitialContext().lookup(bindingName);
        } catch (NamingException e) {
            throw new InitializationException(e.getMessage(), e);
        }
    }


    private int countRows(Connection connection, String tableName) throws SQLException {
        int count = 0;
        Statement s1 = connection.createStatement();
        ResultSet rs = s1.executeQuery("select count(*) from " + tableName);

        while (rs.next()){
            count = rs.getInt(1);
        }

        rs.close();
        s1.close();

        return count;
    }

    private void injectFault(ASFailureType type, ASFailureMode mode, String modeArg) throws RollbackException, SystemException, NamingException {
        ASFailureSpec fault = new ASFailureSpec("fault", mode, modeArg, type);
        TransactionManager transactionManager =
                (TransactionManager) new InitialContext().lookup(jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerJNDIContext());
        transactionManager.getTransaction().enlistResource(new DummyXAResource(fault));
    }

    public void dropTables(Map<String, Connection> connections) throws SQLException {
        for (Connection connection : connections.values()) {
            Statement statement = connection.createStatement();

            statement.executeUpdate("DROP TABLE CEYLONKV");

            statement.close();
        }
    }

    public void printResultSet(String title, ResultSet rs, String ... colNames) throws SQLException {
        System.out.println(title);

        while(rs.next()) {
            for (String colName : colNames) {
                String colVal = rs.getString(colName);
                System.out.printf("%s: %s ", colName, colVal);
            }

            System.out.println();
        }
    }

    public static void insertTable(Connection connection, String key, String val) throws SQLException {
        String insert = "INSERT INTO CEYLONKV(key ,val) values (?,?)";
        PreparedStatement statement = connection.prepareStatement(insert);

        statement.setString(1, key);
        statement.setString(2, val);

        statement.executeUpdate();

        statement.close();
    }

}