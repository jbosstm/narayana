/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.tomcat.jta.integration.app;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import org.apache.tomcat.dbcp.dbcp2.DelegatingConnection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Path(TestExecutor.BASE_PATH)
public class TestExecutor {

    public static final String BASE_PATH = "executor";

    public static final String JNDI_TEST = "jndi";

    public static final String RECOVERY_TEST = "recovery";

    private static final Logger LOGGER = Logger.getLogger(TestExecutor.class.getSimpleName());

    @GET
    @Path(JNDI_TEST)
    public Response verifyJndi() throws NamingException {
        LOGGER.info("Verifying JNDI");

        if (getUserTransaction() == null) {
            return Response.serverError().entity("UserTransaction not found in JNDI").build();
        }

        if (getTransactionManager() == null) {
            return Response.serverError().entity("TransactionManager not found in JNDI").build();
        }

        if (getTransactionSynchronizationRegistry() == null) {
            return Response.serverError().entity("TransactionSynchronizationRegistry not found in JNDI").build();
        }

        if (getTransactionalDataSource() == null) {
            return Response.serverError().entity("DataSource not found in JNDI").build();
        }

        return Response.noContent().build();
    }

    @GET
    @Path(RECOVERY_TEST)
    public Response verifyRecovery() throws NamingException, HeuristicRollbackException, RollbackException,
            HeuristicMixedException, SystemException, NotSupportedException, SQLException {
        LOGGER.info("Verifying recovery");

        TestXAResource.reset();
        createTestTable();
        String testEntry = "test-entry-" + LocalTime.now();
        TestXAResource testXAResource = new TestXAResource();
        Connection connection = null;

        updateXARecoveryModule(m -> m.addXAResourceRecoveryHelper(testXAResource));

        try {
            getTransactionManager().begin();
            getTransactionManager().getTransaction().enlistResource(testXAResource);
            connection = getTransactionalDataSource().getConnection();
            writeToTheDatabase(connection, testEntry);
            try {
                getTransactionManager().commit();
                return Response.serverError().entity("Commit failure was expected").build();
            } catch (Throwable ignored) {
                RecoveryManager.manager().scan();
                RecoveryManager.manager().scan();
            }

            return getRecoveryTestResponse(connection, testEntry);
        } finally {
            if (connection != null) {
                connection.close();

                // This is necessary for H2 to fully reset the connection
                try {
                    Field _connField = DelegatingConnection.class.getDeclaredField("_conn");
                    _connField.setAccessible(true);
                    Object o = _connField.get(connection);
                    o = _connField.get(o);
                    Field currentTransactionField = o.getClass().getEnclosingClass().getDeclaredField("currentTransaction");
                    currentTransactionField.setAccessible(true);
                    Field enclosing = o.getClass().getDeclaredField("this$0");
                    enclosing.setAccessible(true);
                    o = enclosing.get(o);
                    currentTransactionField.set(o, null);
                } catch (NullPointerException e) {
                    // This should happen for non-H2
                } catch (NoSuchFieldException e) {
                } catch (IllegalAccessException e) {
                }
            }
            updateXARecoveryModule(m -> m.removeXAResourceRecoveryHelper(testXAResource));
        }
    }

    private void updateXARecoveryModule(Consumer<XARecoveryModule> action) {
        RecoveryManager.manager().getModules().stream().filter(m -> m instanceof XARecoveryModule)
                .forEach(m -> action.accept((XARecoveryModule) m));
    }

    private Response getRecoveryTestResponse(Connection connection, String testEntry) throws SQLException, NamingException {
        if (didRecoveryHappen(connection, testEntry)) {
            return Response.noContent().build();
        }

        return Response.serverError().entity("Recovery failed").build();
    }

    private boolean didRecoveryHappen(Connection connection, String entry) throws SQLException, NamingException {
        List<String> expectedMethods = Arrays.asList("start", "end", "prepare", "commit");
        List<String> actualMethods = TestXAResource.getMethodCalls();
        LOGGER.info("Verifying TestXAResource methods. Expected=" + expectedMethods + ", actual=" + actualMethods);

        boolean entryExists = doesEntryExist(connection, entry);
        LOGGER.info("Verifying if database entry exists:" + entryExists);

        return expectedMethods.equals(actualMethods) && entryExists;
    }

    private boolean doesEntryExist(Connection connection, String entry) throws SQLException, NamingException {
        String query = "SELECT COUNT(*) FROM test WHERE value='" + entry + "'";
        try{
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(query);
            return result.next() && result.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, e, () -> String.format("Cannot get result when querying entry '%s'", entry));
            return false;
        }
    }

    private void writeToTheDatabase(Connection connection, String entry) throws NamingException, SQLException {
        String query = "INSERT INTO test VALUES ('" + entry + "')";
        Statement statement = connection.createStatement();
        statement.execute(query);
    }

    private UserTransaction getUserTransaction() throws NamingException {
        return InitialContext.doLookup("java:comp/UserTransaction");
    }

    private TransactionManager getTransactionManager() throws NamingException {
        return InitialContext.doLookup("java:comp/env/TransactionManager");
    }

    private TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() throws NamingException {
        return InitialContext.doLookup("java:comp/env/TransactionSynchronizationRegistry");
    }

    private DataSource getTransactionalDataSource() throws NamingException {
        return InitialContext.doLookup("java:comp/env/transactionalDataSource");
    }

    private void createTestTable() throws SQLException, NamingException {
        String query = "CREATE TABLE IF NOT EXISTS test (value VARCHAR(100))";
        Connection connection = null;

        try {
            connection = getTransactionalDataSource().getConnection();
            Statement statement = connection.createStatement();
            statement.execute(query);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
