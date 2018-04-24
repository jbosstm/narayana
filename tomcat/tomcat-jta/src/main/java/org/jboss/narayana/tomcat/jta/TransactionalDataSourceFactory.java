/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.narayana.tomcat.jta;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.dbcp.dbcp2.PoolableConnection;
import org.apache.tomcat.dbcp.dbcp2.PoolableConnectionFactory;
import org.apache.tomcat.dbcp.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.tomcat.dbcp.dbcp2.managed.ManagedDataSource;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPool;
import org.apache.tomcat.dbcp.pool2.impl.GenericObjectPoolConfig;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:zfeng@redhat.com">Zheng Feng</a>
 */
public class TransactionalDataSourceFactory implements ObjectFactory {

    private static final Log log = LogFactory.getLog(TransactionalDataSourceFactory.class);

    private static final String PROP_TRANSACTION_MANAGER = "transactionManager";
    private static final String PROP_XA_DATASOURCE = "xaDataSource";
    private static final String PROP_USERNAME = "username";
    private static final String PROP_PASSWORD = "password";

    private static final List<String> DBCP2_POOLING_PROPERTIES = Arrays.asList(
            "maxTotal",
            "minIdle",
            "maxIdle",
            "lifo",
            "fairness",
            "maxWaitMillis",
            "minEvictableIdleTimeMillis",
            "evictorShutdownTimeoutMillis",
            "softMinEvictableIdleTimeMillis",
            "numTestsPerEvictionRun",
            "evictionPolicyClassName",
            "testOnCreate",
            "testOnBorrow",
            "testOnReturn",
            "testWhileIdle",
            "timeBetweenEvictionRunsMillis",
            "blockWhenExhausted",
            "jmxEnabled",
            "jmxNamePrefix",
            "jmxNameBase"
    );

    @Override
    public Object getObjectInstance(Object obj, Name name, Context context, Hashtable<?, ?> environment) throws Exception {
        if (obj == null || !(obj instanceof Reference)) {
            return null;
        }

        final Reference ref = (Reference) obj;
        if (!"javax.sql.XADataSource".equals(ref.getClassName())) {
            log.fatal(String.format("The expected type of datasource was javax.sql.XADataSource and not %s.", ref.getClassName()));
            return null;
        }

        final Properties properties = new Properties();
        Enumeration<RefAddr> iter = ref.getAll();

        while (iter.hasMoreElements()) {
            RefAddr ra = iter.nextElement();
            if (DBCP2_POOLING_PROPERTIES.contains(ra.getType().toString())) {
                properties.setProperty(ra.getType().toString(), ra.getContent().toString());
            }
        }

        final TransactionManager transactionManager = (TransactionManager) getReferenceObject(ref, context, PROP_TRANSACTION_MANAGER);
        final XADataSource xaDataSource = (XADataSource) getReferenceObject(ref, context, PROP_XA_DATASOURCE);

        if (transactionManager != null && xaDataSource != null) {
            final DataSourceXAConnectionFactory xaConnectionFactory =
                    new DataSourceXAConnectionFactory(transactionManager, xaDataSource);
            final PoolableConnectionFactory poolableConnectionFactory =
                    new PoolableConnectionFactory(xaConnectionFactory, null);
            final GenericObjectPoolConfig config =
                    new GenericObjectPoolConfig();
            setPoolConfig(config, properties);
            final GenericObjectPool<PoolableConnection> objectPool =
                    new GenericObjectPool<>(poolableConnectionFactory, config);
            poolableConnectionFactory.setPool(objectPool);

            // Register for recovery
            XARecoveryModule xaRecoveryModule = getXARecoveryModule();
            if (xaRecoveryModule != null) {
                xaRecoveryModule.addXAResourceRecoveryHelper(new XAResourceRecoveryHelper() {
                    private final Object lock = new Object();
                    private XAConnection connection;

                    @Override
                    public boolean initialise(String p) throws Exception {
                        return true;
                    }

                    @Override
                    public synchronized XAResource[] getXAResources() throws Exception {
                        synchronized (lock) {
                            initialiseConnection();
                            try {
                                return new XAResource[]{connection.getXAResource()};
                            } catch (SQLException ex) {
                                return new XAResource[0];
                            }
                        }
                    }

                    private void initialiseConnection() throws SQLException {
                        // This will allow us to ensure that each recovery cycle gets a fresh connection
                        // It might be better to close at the end of the recovery pass to free up the connection but
                        // we don't have a hook
                        if (connection == null) {
                            String user = properties.getProperty(PROP_USERNAME);
                            String password = properties.getProperty(PROP_PASSWORD);
                            if (user != null && password != null) {
                                connection = xaDataSource.getXAConnection(user, password);
                            } else {
                                connection = xaDataSource.getXAConnection();
                            }
                            connection.addConnectionEventListener(new ConnectionEventListener() {
                                @Override
                                public void connectionClosed(ConnectionEvent event) {
                                    log.warn("The connection was closed: " + connection);
                                    synchronized (lock) {
                                        connection = null;
                                    }
                                }

                                @Override
                                public void connectionErrorOccurred(ConnectionEvent event) {
                                    log.warn("A connection error occurred: " + connection);
                                    synchronized (lock) {
                                        try {
                                            connection.close();
                                        } catch (SQLException e) {
                                            // Ignore
                                            log.warn("Could not close failing connection: " + connection);
                                        }
                                        connection = null;
                                    }
                                }
                            });
                        }
                    }
                });
            }

            return new ManagedDataSource<>(objectPool, xaConnectionFactory.getTransactionRegistry());
        } else {
            return null;
        }
    }

    private Object getReferenceObject(Reference ref, Context context, String prop) throws Exception {
        final RefAddr ra = ref.get(prop);
        if (ra != null) {
            return context.lookup(ra.getContent().toString());
        } else {
            return null;
        }
    }

    private void setPoolConfig(GenericObjectPoolConfig config, Properties properties) {
        for (String propertyName : properties.stringPropertyNames()) {
            try {
                final Method method = getSetMethod(GenericObjectPoolConfig.class, propertyName);
                final Class type = GenericObjectPoolConfig.class.getDeclaredField(propertyName).getType();
                final String value = properties.getProperty(propertyName);
                if (value != null) {
                    if (type == int.class) {
                        method.invoke(config, Integer.parseInt(value));
                    } else if (type == long.class) {
                        method.invoke(config, Long.parseLong(value));
                    } else if (type == boolean.class) {
                        method.invoke(config, Boolean.parseBoolean(value));
                    } else if (type == String.class) {
                        method.invoke(config, value);
                    }
                }
            } catch (Exception e) {
                log.warn("There was an error processing pool config properties.", e);
            }
        }
    }

    private Method getSetMethod(Class objectClass, String fieldName) throws Exception {
        final Class[] parameterTypes = new Class[1];
        final Field field = objectClass.getDeclaredField(fieldName);
        parameterTypes[0] = field.getType();
        final StringBuffer sb = new StringBuffer();
        sb.append("set");
        sb.append(fieldName.substring(0, 1).toUpperCase());
        sb.append(fieldName.substring(1));
        @SuppressWarnings("unchecked")
        Method method = objectClass.getMethod(sb.toString(), parameterTypes);
        return method;
    }

    private XARecoveryModule getXARecoveryModule() {
        final XARecoveryModule xaRecoveryModule = XARecoveryModule.getRegisteredXARecoveryModule();
        if (xaRecoveryModule != null) {
            return xaRecoveryModule;
        }
        throw new IllegalStateException("XARecoveryModule is not registered with recovery manager");
    }
}
