/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.distributed;


import org.jnp.server.NamingBeanImpl;

import javax.naming.Context;
import java.util.Hashtable;

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

}