/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jdbc.utils;



import static org.junit.Assert.assertNotNull;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.junit.Test;

public class JNDISetup
{
    @Test
    public void test() throws Exception
    {
        DBPlugin plugin = (DBPlugin)Thread.currentThread().getContextClassLoader().loadClass("TODO").newInstance();

        String jndiName = "jdbc/DB";
        DataSource ds = plugin.getDataSource(new String[] {"TODO"});

        assertNotNull(ds);

        System.out.println("Binding datasource to '"+jndiName+"'");
        InitialContext ctx = new InitialContext();
        ctx.rebind(jndiName, ds);

    }
}