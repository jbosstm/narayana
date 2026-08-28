
/*
 * Copyright (C) 2000, 2001,
 *
 * Hewlett-Packard,
 * Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 */
package com.arjuna.ats.internal.jdbc.recovery;

import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;
import com.arjuna.ats.jdbc.logging.jdbcLogger;

import com.arjuna.ats.internal.jdbc.*;
import com.arjuna.ats.jta.recovery.XAConnectionRecovery;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.common.util.logging.*;

import java.sql.*;
import javax.sql.*;
import javax.transaction.*;
import javax.transaction.xa.*;
import java.util.*;

import java.lang.NumberFormatException;

/**
 * This class implements the XAConnectionRecovery interface for XAResources.
 * The parameter supplied in setParameters can contain arbitrary information
 * necessary to initialise the class once created. In this instance it contains
 * the name of the property file in which the db connection information is
 * specified, as well as the number of connections that this file contains
 * information on (separated by ;).
 *
 * IMPORTANT: this is only an *example* of the sorts of things an
 * XAConnectionRecovery implementor could do. This implementation uses
 * a property file which is assumed to contain sufficient information to
 * recreate connections used during the normal run of an application so that
 * we can perform recovery on them. It is not recommended that information such
 * as user name and password appear in such a raw text format as it opens up
 * a potential security hole.
 *
 * The db parameters specified in the property file are assumed to be
 * in the format:
 *
 * DB_x_DatabaseURL=
 * DB_x_DatabaseUser=
 * DB_x_DatabasePassword=
 * DB_x_DatabaseDynamicClass=
 *
 * DB_JNDI_x_DatabaseURL= 
 * DB_JNDI_x_DatabaseUser= 
 * DB_JNDI_x_DatabasePassword= 
 *
 * where x is the number of the connection information.
 *
 * @since JTS 2.1.
 */

public class BasicXARecovery implements XAConnectionRecovery
{    
    /*
     * Some XAConnectionRecovery implementations will do their startup work
     * here, and then do little or nothing in setDetails. Since this one needs
     * to know dynamic class name, the constructor does nothing.
     */
    public BasicXARecovery () throws SQLException
    {
        numberOfConnections = 1;
        connectionIndex = 0;
        props = null;
    }

    /**
     * The recovery module will have chopped off this class name already.
     * The parameter should specify a property file from which the url,
     * user name, password, etc. can be read.
     */

    public boolean initialise (String parameter) throws SQLException
    {
        int breakPosition = parameter.indexOf(BREAKCHARACTER);
        String fileName = parameter;

        if (breakPosition != -1)
            {
                fileName = parameter.substring(0, breakPosition -1);

                try
                    {
                        numberOfConnections = Integer.parseInt(parameter.substring(breakPosition +1));
                    }
                catch (NumberFormatException e)
                    {
                        //Produce a Warning Message
                        return false;
                    }
            }

        PropertyManager.addPropertiesFile(fileName);

        try
            {
                PropertyManager.loadProperties(true);

                props = PropertyManager.getProperties();
            }
        catch (Exception e)
            {
                //Produce a Warning Message 

                return false;
            }  

        return true;
    }    

    public synchronized XAConnection getConnection () throws SQLException
    {
        JDBC2RecoveryConnection conn = null;

        if (hasMoreConnections())
            {
                connectionIndex++;

                conn = getStandardConnection();

                if (conn == null)
                    conn = getJNDIConnection();

                if (conn == null)
                    //Produce a Warning message
                    }

        return conn;
    }

    public synchronized boolean hasMoreConnections ()
    {
        if (connectionIndex == numberOfConnections)
            return false;
        else
            return true;
    }

    private final JDBC2RecoveryConnection getStandardConnection () throws SQLException
    {
        String number = new String(""+connectionIndex);
        String url = new String(dbTag+number+urlTag);
        String password = new String(dbTag+number+passwordTag);
        String user = new String(dbTag+number+userTag);
        String dynamicClass = new String(dbTag+number+dynamicClassTag);
        Properties dbProperties = new Properties();
        String theUser = props.getProperty(user);
        String thePassword = props.getProperty(password);

        if (theUser != null)
            {
                dbProperties.put(ArjunaJDBC2Driver.userName, theUser);
                dbProperties.put(ArjunaJDBC2Driver.password, thePassword);

                String dc = props.getProperty(dynamicClass);

                if (dc != null)
                    dbProperties.put(ArjunaJDBC2Driver.dynamicClass, dc);

                return new JDBC2RecoveryConnection(url, dbProperties);
            }
        else
            return null;
    }

    private final JDBC2RecoveryConnection getJNDIConnection () throws SQLException
    {
        String number = new String(""+connectionIndex);
        String url = new String(dbTag+jndiTag+number+urlTag);
        String password = new String(dbTag+jndiTag+number+passwordTag);
        String user = new String(dbTag+jndiTag+number+userTag);
        Properties dbProperties = new Properties();
        String theUser = props.getProperty(user);
        String thePassword = props.getProperty(password);

        if (theUser != null)
            {
                dbProperties.put(ArjunaJDBC2Driver.userName, theUser);
                dbProperties.put(ArjunaJDBC2Driver.password, thePassword);    
                return new JDBC2RecoveryConnection(url, dbProperties);
            }
        else
            return null;
    }
    private int        numberOfConnections;
    private int        connectionIndex;
    private Properties props;   
    private static final String dbTag = "DB_";
    private static final String urlTag = "_DatabaseURL";
    private static final String passwordTag = "_DatabasePassword";
    private static final String userTag = "_DatabaseUser";
    private static final String dynamicClassTag = "_DatabaseDynamicClass";
    private static final String jndiTag = "JNDI_";

    /*
     * Example:
     *
     * DB2_DatabaseURL=jdbc\:arjuna\:sequelink\://qa02\:20001
     * DB2_DatabaseUser=tester2
     * DB2_DatabasePassword=tester
     * DB2_DatabaseDynamicClass=
     *      com.arjuna.ats.internal.jdbc.drivers.sequelink_5_1 
     *
     * DB_JNDI_DatabaseURL=jdbc\:arjuna\:jndi
     * DB_JNDI_DatabaseUser=tester1
     * DB_JNDI_DatabasePassword=tester
     * DB_JNDI_DatabaseName=empay
     * DB_JNDI_Host=qa02
     * DB_JNDI_Port=20000
     */

    private static final char BREAKCHARACTER = ';';  // delimiter for parameters
}