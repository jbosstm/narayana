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
