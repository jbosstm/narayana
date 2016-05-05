package com.hp.mwtests.ts.jdbc.basic;

import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class StableConnections {
    private static final String DB_USER1 = "postgres";
    private static final String DB_HOST = "127.0.0.1";
    private static final String DB_SID = "postgres";

    @Before
    public void setup() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, NamingException, SQLException {
        System.setProperty("java.naming.factory.initial",
            "org.apache.naming.java.javaURLContextFactory");
        System.setProperty("java.naming.factory.url.pkgs", "org.apache.naming");
        getDataSource(DB_USER1);
    }

    @Test
    public void test() throws SQLException {
        for (int i = 0; i < 2; i++) {
            try (Connection connection = DriverManager.getConnection("jdbc:arjuna:java:/comp/env/jdbc/" + DB_USER1, DB_USER1, DB_USER1)) {

                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM pg_stat_activity");
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next() || resultSet.getLong(1) != 1) {
                    fail();
                }
            }
        }
    }

    private static DataSource getDataSource(String user)
        throws NamingException, SQLException, InstantiationException,
        IllegalAccessException, IllegalArgumentException,
        InvocationTargetException, NoSuchMethodException,
        SecurityException, ClassNotFoundException {
        InitialContext initialContext = prepareInitialContext();

        Class clazz = Class.forName("org.postgresql.xa.PGXADataSource");
        XADataSource xaDataSource = (XADataSource) clazz.newInstance();
        clazz.getMethod("setServerName", new Class[] { String.class }).invoke(
            xaDataSource, new Object[] { DB_HOST });
        clazz.getMethod("setDatabaseName", new Class[] { String.class })
            .invoke(xaDataSource, new Object[] { DB_SID });
        clazz.getMethod("setUser", new Class[] { String.class }).invoke(
            xaDataSource, new Object[] { user });
        clazz.getMethod("setPassword", new Class[] { String.class }).invoke(
            xaDataSource, new Object[] { user });
        clazz.getMethod("setPortNumber", new Class[] { int.class }).invoke(
            xaDataSource, new Object[] { 5432 });

        final String name = "java:/comp/env/jdbc/" + user;
        initialContext.bind(name, xaDataSource);

        DriverManagerDataSource dataSource = new DriverManagerDataSource(
            "jdbc:arjuna:" + name);
        dataSource
            .setDriverClassName("com.arjuna.ats.jdbc.TransactionalDriver");

        return dataSource;
    }

    private static InitialContext prepareInitialContext()
        throws NamingException {
        final InitialContext initialContext = new InitialContext();

        try {
            initialContext.lookup("java:/comp/env/jdbc");
        } catch (NamingException ne) {
            initialContext.createSubcontext("java:");
            initialContext.createSubcontext("java:/comp");
            initialContext.createSubcontext("java:/comp/env");
            initialContext.createSubcontext("java:/comp/env/jdbc");
        }

        return initialContext;
    }
}
