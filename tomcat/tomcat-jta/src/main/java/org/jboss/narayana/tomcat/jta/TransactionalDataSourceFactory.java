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

package org.jboss.narayana.tomcat.jta;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.managed.DataSourceXAConnectionFactory;
import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author <a href="mailto:zfeng@redhat.com">Zheng Feng</a>
 */
public class TransactionalDataSourceFactory implements ObjectFactory {
    private static final String PROP_USERNAME = "username";
    private static final String PROP_PASSWORD = "password";
    private static final String PROP_MAX_TOTAL = "maxTotal";
    private static final String PROP_MAX_IDLE = "maxIdle";
    private static final String PROP_MIN_IDLE = "minIdle";

    private static final String[] ALL_PROPERTIES = {
            PROP_USERNAME,
            PROP_PASSWORD,
            PROP_MAX_TOTAL,
            PROP_MAX_IDLE,
            PROP_MIN_IDLE
    };

    @Override
    public Object getObjectInstance(Object obj, Name name, Context context, Hashtable<?, ?> environment) throws Exception {

        if (obj == null || !(obj instanceof Reference)) {
            return null;
        }

        final Reference ref = (Reference) obj;
        if (!"javax.sql.DataSource".equals(ref.getClassName())) {
            return null;
        }

        final Properties properties = new Properties();
        for (final String propertyName : ALL_PROPERTIES) {
            final RefAddr ra = ref.get(propertyName);
            if (ra != null) {
                properties.setProperty(propertyName, ra.getContent().toString());
            }
        }

        TransactionManager transactionManager = (TransactionManager) getReferenceObject(ref, context, "transactionManager");
        XADataSource xaDataSource = (XADataSource) getReferenceObject(ref, context, "xaDataSource");

        XARecoveryModule xaRecoveryModule = getXARecoveryModule();
        if (xaRecoveryModule != null) {
            xaRecoveryModule.addXAResourceRecoveryHelper( new XAResourceRecoveryHelper() {
                @Override
                public boolean initialise(String p) throws Exception {
                    return true;
                }

                @Override
                public XAResource[] getXAResources() throws Exception {
                    try {
                        String user = properties.getProperty(PROP_USERNAME);
                        String password = properties.getProperty(PROP_PASSWORD);

                        if (user != null && password != null) {
                            return new XAResource[]{xaDataSource.getXAConnection(user, password).getXAResource()};
                        } else {
                            return new XAResource[]{xaDataSource.getXAConnection().getXAResource()};
                        }
                    } catch (SQLException ex) {
                        return new XAResource[0];
                    }
                }
            });
        }

        if (transactionManager != null && xaDataSource != null) {
            DataSourceXAConnectionFactory xaConnectionFactory =
                    new DataSourceXAConnectionFactory(transactionManager, xaDataSource);
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(xaConnectionFactory, null);
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            setPoolConfig(config, properties);
            GenericObjectPool<PoolableConnection> objectPool =
                    new GenericObjectPool<>(poolableConnectionFactory, config);
            poolableConnectionFactory.setPool(objectPool);
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
        for (String propertyName : ALL_PROPERTIES) {
            try {
                Method method = getSetMethod(GenericObjectPoolConfig.class, propertyName);
                Class type = GenericObjectPoolConfig.class.getDeclaredField(propertyName).getType();
                String value = properties.getProperty(propertyName);
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
            }
        }
    }

    private Method getSetMethod(Class objectClass, String fieldName) throws Exception {
        Class[] parameterTypes = new Class[1];
        Field field = objectClass.getDeclaredField(fieldName);
        parameterTypes[0] = field.getType();
        StringBuffer sb = new StringBuffer();
        sb.append("set");
        sb.append(fieldName.substring(0, 1).toUpperCase());
        sb.append(fieldName.substring(1));
        Method method = objectClass.getMethod(sb.toString(), parameterTypes);
        return method;
    }

    private XARecoveryModule getXARecoveryModule() {
        XARecoveryModule xaRecoveryModule = XARecoveryModule
                .getRegisteredXARecoveryModule();
        if (xaRecoveryModule != null) {
            return xaRecoveryModule;
        }
        throw new IllegalStateException(
                "XARecoveryModule is not registered with recovery manager");
    }
}
