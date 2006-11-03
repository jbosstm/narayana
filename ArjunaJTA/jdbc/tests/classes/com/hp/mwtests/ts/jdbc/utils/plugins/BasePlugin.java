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
package com.hp.mwtests.ts.jdbc.utils.plugins;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BasePlugin.java 2342 2006-03-30 13:06:17Z  $
 */

import com.hp.mwtests.ts.jdbc.utils.DBPlugin;

import javax.sql.DataSource;

public abstract class BasePlugin implements DBPlugin
{
    private String  _userName = null;
    private String  _password = null;
    private String  _database = null;
    private String  _url = null;
    private String  _server = null;
    private int     _port = -1;

    public DataSource getDataSource(String[] args) throws java.sql.SQLException
    {
        for (int count=0;count<args.length;count++)
        {
            if ( args[count].equalsIgnoreCase("-username") )
            {
                setUsername(args[count+1]);
            }
            else
            if ( args[count].equalsIgnoreCase("-password") )
            {
                setPassword(args[count+1]);
            }
            else
            if ( args[count].equalsIgnoreCase("-database") )
            {
                setDatabase(args[count+1]);
            }
            else
            if ( args[count].equalsIgnoreCase("-port") )
            {
                setPort(Integer.parseInt(args[count+1]));
            }
            else
            if ( args[count].equalsIgnoreCase("-server") )
            {
                setServer(args[count+1]);
            }
            else
            if ( args[count].equalsIgnoreCase("-url") )
            {
                setUrl(args[count+1]);
            }
        }

        return processParameters(args);
    }

    public final void setUsername(String username)
    {
        _userName = username;
    }

    public final String getUsername()
    {
        return _userName;
    }

    public final boolean isUsernameDefined()
    {
        return _userName != null;
    }

    public final void setPassword(String password)
    {
        _password = password;
    }

    public final String getPassword()
    {
        return _password;
    }

    public final boolean isPasswordDefined()
    {
        return _password != null;
    }

    public final void setDatabase(String db)
    {
        _database = db;
    }

    public final String getDatabase()
    {
        return _database;
    }

    public final boolean isDatabaseDefined()
    {
        return _database != null;
    }

    public final void setPort(int port)
    {
        _port = port;
    }

    public final int getPort()
    {
        return _port;
    }

    public final boolean isPortDefined()
    {
        return _port != -1;
    }

    public final void setUrl(String url)
    {
        _url = url;
    }

    public final String getUrl()
    {
        return _url;
    }

    public boolean isUrlDefined()
    {
        return _url != null;
    }

    public final void setServer(String server)
    {
        _server = server;
    }

    public final String getServer()
    {
        return _server;
    }

    public final boolean isServerDefined()
    {
        return _server != null;
    }

    public abstract DataSource processParameters(String[] args) throws java.sql.SQLException;
}
