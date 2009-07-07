/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jdbc.utils;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JNDISetup.java 2342 2006-03-30 13:06:17Z  $
 */

import javax.sql.*;
import javax.naming.*;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

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
