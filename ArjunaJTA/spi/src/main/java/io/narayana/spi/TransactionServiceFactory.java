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

import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.utils.JNDIManager;
import io.narayana.spi.internal.DataSourceManagerImpl;
import io.narayana.spi.internal.DbProps;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class TransactionServiceFactory {
    static final String DB_PROPERTIES_NAME = "db.properties";
    private static RecoveryManager recoveryManager;
    private static boolean initialized = false;
    private static InitialContext initialContext;
    private static Set<String> jndiBindings = new HashSet<String>();
    private static boolean replacedJndiProperties = false;

    /**
     * Makes the transaction service available by bind various transaction related object into the default
     * JNDI tree.
     * @param startRecoveryService set to true to start the recovery service.
     * @throws InitializationException if no usable InitialContext is available
     */
    public static synchronized void start(boolean startRecoveryService) throws InitializationException {
        if (initialized)
            return;

        try {
            initialContext =  new InitialContext();

            replacedJndiProperties =  jdbcPropertyManager.getJDBCEnvironmentBean().getJndiProperties().size() == 0;

            if (replacedJndiProperties)
                jdbcPropertyManager.getJDBCEnvironmentBean().setJndiProperties(initialContext.getEnvironment());

            DriverManager.registerDriver(new TransactionalDriver());
        } catch (NamingException e) {
            if (tsLogger.logger.isInfoEnabled())
                tsLogger.logger.info("TransactionServiceFactory error:", e);

            throw new InitializationException("No suitable JNDI provider available", e);
        } catch (SQLException e) {
            if (tsLogger.logger.isInfoEnabled())
                tsLogger.logger.info("TransactionServiceFactory error:", e);

            throw new InitializationException("Cannot initialize TransactionalDriver", e);
        }

        registerJndiBindings(initialContext);

        initialized = true;

        if (startRecoveryService)
            startRecoveryService();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                TransactionServiceFactory.stop();
            }
        });
    }


    /**
     * Stop the transaction service. If the recovery manager was started previously then it to will be stopped.
     */
    public static synchronized void stop() {
        if (!initialized)
            return;

        if (recoveryManager != null) {
            recoveryManager.terminate();
            recoveryManager = null;
        }

        unregisterJndiBindings();

        try {
            DriverManager.deregisterDriver(new TransactionalDriver());
        } catch (SQLException e) {
            if (tsLogger.logger.isInfoEnabled())
                tsLogger.logger.debug("Unable to deregister TransactionalDriver: " + e.getMessage(), e);
        }

        if (replacedJndiProperties)
            jdbcPropertyManager.getJDBCEnvironmentBean().setJndiProperties(null);

        initialized = false;
    }

    private static void startRecoveryService() {
        if (recoveryManager == null) {
            final RecoveryEnvironmentBean recoveryEnvironmentBean = recoveryPropertyManager.getRecoveryEnvironmentBean();

            recoveryEnvironmentBean.setRecoveryModuleClassNames(Arrays.asList(
                    "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
                    "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule"));

            RecoveryManager.delayRecoveryManagerThread();

            recoveryManager = RecoveryManager.manager();
            recoveryManager.initialize();
        }
    }

    private static Set<String> registerJndiBindings(InitialContext initialContext) throws InitializationException {
        try {
            JNDIManager.bindJTATransactionManagerImplementation(initialContext);
            jndiBindings.add(jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerJNDIContext());
            JNDIManager.bindJTAUserTransactionImplementation(initialContext);
            jndiBindings.add(jtaPropertyManager.getJTAEnvironmentBean().getUserTransactionJNDIContext());
            JNDIManager.bindJTATransactionSynchronizationRegistryImplementation(initialContext);
            jndiBindings.add(jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistryJNDIContext());
        } catch (NamingException e) {
            if (tsLogger.logger.isInfoEnabled())
                tsLogger.logger.infof("Unable to bind TM into JNDI: %s", e.getMessage());

            throw new InitializationException("Unable to bind TM into JNDI", e);
        }

        Map<String, DbProps> dbConfigs = new DbProps().getConfig(DB_PROPERTIES_NAME);
        DataSourceManagerImpl dataSourceManager = new DataSourceManagerImpl();

        for (DbProps props : dbConfigs.values()) {
            String url = props.getDatabaseURL();

            if (url != null && url.length() > 0)
                dataSourceManager.registerDataSource(props.getBinding(), props.getDriver(), url,
                        props.getDatabaseUser(), props.getDatabasePassword());
            else
                dataSourceManager.registerDataSource(props.getBinding(), props.getDriver(), props.getDatabaseName(),
                        props.getHost(), props.getPort(), props.getDatabaseUser(),props.getDatabasePassword());

            jndiBindings.add(props.getBinding());
        }

        return jndiBindings;
    }

    private static void unregisterJndiBindings() {
        Iterator<String> bindingIterator = jndiBindings.iterator();

        while (bindingIterator.hasNext()) {
            try {
                initialContext.unbind(bindingIterator.next());
            } catch (NamingException e) {
                if (tsLogger.logger.isDebugEnabled())
                    tsLogger.logger.debugf("Unable to unregister JNDI binding: %s" + e.getMessage());
            }

            bindingIterator.remove();
        }

        jndiBindings.clear();
    }
}
