/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jdbc.basic;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.jdbc.TransactionalDriver;
import org.h2.Driver;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jakarta.transaction.Synchronization;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class PoolingTest {
    private static final int POOLSIZE = 400;
    protected Properties dbProperties = null;
    protected String url = null;

    @Before
    public void setup() throws Exception {
        url = "jdbc:arjuna:";
        Properties p = System.getProperties();
        p.put("jdbc.drivers", Driver.class.getName());

        System.setProperties(p);
        DriverManager.registerDriver(new TransactionalDriver());

        dbProperties = new Properties();

        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dbProperties.put(TransactionalDriver.XADataSource, ds);
        dbProperties.setProperty(TransactionalDriver.maxConnections, ""+POOLSIZE);

        try (Connection conn = DriverManager.getConnection(url, dbProperties)) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    stmt.executeUpdate("DROP TABLE test_table");
                } catch (Exception e) {
                    // Ignore
                } finally {
                    stmt.executeUpdate("CREATE TABLE test_table (a varchar2)");
                }
            }
        }
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, dbProperties)) {
            ResultSet res = conn.prepareStatement("SELECT * FROM test_table").executeQuery();
            int rowCount1 = 0;
            while (res.next()) {
                rowCount1++;
            }
            System.out.println("Number of rows = " + rowCount1);
        }
    }

    @Test
    public void test() throws InterruptedException {
        TransactionSynchronizationRegistryImple transactionSynchronizationRegistryImple = new TransactionSynchronizationRegistryImple();
        jakarta.transaction.TransactionManager tx = com.arjuna.ats.jta.TransactionManager.transactionManager();

        List<Thread> threads = new ArrayList();
        Set<Exception> failures = new HashSet<>();
        for (int i = 0; i < 4 * POOLSIZE; i++) {
            Thread thread = new Thread(() -> {
                try {
                    tx.begin();
                    transactionSynchronizationRegistryImple.registerInterposedSynchronization(new Synchronization() {
                        private Connection conn;

                        @Override
                        public void beforeCompletion() {
                            try {
                                conn = DriverManager.getConnection(url, dbProperties);
                                conn.createStatement().execute("INSERT INTO test_table (a) VALUES ('" + Thread.currentThread().getId() + "')");
                            } catch (Exception e) {
                                failures.add(e);
                            }
                        }

                        @Override
                        public void afterCompletion(int status) {
                            try {
                                conn.close();
                            } catch (SQLException e) {
                                failures.add(e);
                            }
                        }
                    });
                    tx.commit();
                } catch (Exception e) {
                    failures.add(e);
                }
            });
            threads.add(thread);

            assertEquals(failures.size(), 0);
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }
}