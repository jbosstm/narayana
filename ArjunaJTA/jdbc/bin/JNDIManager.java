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
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 * 
 * $Id: JNDIManager.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ArjunaCommon.Common.*;
import com.arjuna.JDBC2.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import javax.transaction.xa.*;
import javax.naming.*;

public class JNDIManager
{

public static void main (String[] args)
    {
	String url = "jdbc:arjuna:oracle:thin:@reshend.ncl.ac.uk:1521:JDBCTest";
	String dynamicClass = "com.arjuna.JDBC2.drivers.oracle_8_1_6";
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].equals("-help"))
	    {
		System.out.println("Usage: JNDIManager [-url <url>] [-dynamic <class>] [-help]");
		System.exit(0);
	    }
	    if (args[i].equals("-url"))
		url = args[i+1];
	    if (args[i].equals("-dynamic"))
		dynamicClass = args[i+1];
	}

	try
	{
	    Class c = Thread.currentThread().getContextClassLoader().loadClass(dynamicClass);
	    ArjunaJDBC2DynamicClass dc = (ArjunaJDBC2DynamicClass) c.newInstance();
	    XADataSource ds = (XADataSource) dc.getDataSource(url);

	    Hashtable env = new Hashtable();
	    String initialCtx = PropertyManager.getProperty("Context.INITIAL_CONTEXT_FACTORY");
	    
	    env.put(Context.INITIAL_CONTEXT_FACTORY, initialCtx);

	    InitialContext ctx = new InitialContext(env);
	    
	    ctx.bind("/tmp/foo", ds);

	    System.out.println("Ready");

	    for (;;);
	}
	catch (Exception e)
	{
	    System.err.println(e);
	}
    }

};
