/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.spi.util;

import io.narayana.spi.InitializationException;
import org.jnp.server.NamingBeanImpl;

import javax.naming.Context;
import java.util.Hashtable;
import java.util.Map;


public class JndiProvider {

    private static NamingBeanImpl jnpServer = new NamingBeanImpl();

    public static Hashtable start() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

        jnpServer.start();

        Hashtable props = new Hashtable();

        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

        return props;
    }

    public static void stop() {
        jnpServer.stop();
    }

    public static void initBindings() throws InitializationException {
        Map<String, DbProps> dbConfigs = new DbProps().getConfig(DbProps.DB_PROPERTIES_NAME);
        DataSourceManagerImpl dataSourceManager = new DataSourceManagerImpl();

        for (DbProps props : dbConfigs.values()) {
            String url = props.getDatabaseURL();

            if (url != null && url.length() > 0)
                dataSourceManager.registerDataSource(props.getBinding(), props.getDriver(), url,
                        props.getDatabaseUser(), props.getDatabasePassword());
            else
                dataSourceManager.registerDataSource(props.getBinding(), props.getDriver(), props.getDatabaseName(),
                        props.getHost(), props.getPort(), props.getDatabaseUser(),props.getDatabasePassword());
        }
    }
}