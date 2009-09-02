/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.ats.jdbc.common;

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;

import java.sql.Connection;

/**
 * A JavaBean containing configuration properties for the JDBC subsystem.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.jdbc.")
public class JDBCEnvironmentBean implements JDBCEnvironmentBeanMBean
{
    @FullPropertyName(name = "com.arjuna.ats.jdbc.common.propertiesFile")
    private String propertiesFile = "";

    private int isolationLevel = Connection.TRANSACTION_SERIALIZABLE;

    /**
     * Returns the name of the properties file.
     *
     * Default: ""
     * Equivalent deprecated property: com.arjuna.ats.jdbc.common.propertiesFile
     *
     * @return the name of the properties file
     */
    public String getPropertiesFile()
    {
        return propertiesFile;
    }

    /**
     * Sets the name of the properties file.
     *
     * @param propertiesFile the name of the properties file.
     */
    public void setPropertiesFile(String propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

    /**
     * Returns the default isolation level for tansactional database operations.
     *
     * Default: Connection.TRANSACTION_SERIALIZABLE
     * Equivalent deprecated property: com.arjuna.ats.jdbc.isolationLevel"
     *
     * @return the default connection isolation level.
     */
    public int getIsolationLevel()
    {
        return isolationLevel;
    }

    /**
     * Sets the default transaction isolation level for database operations.
     *
     * @param isolationLevel the default connection isolation level.
     */
    public void setIsolationLevel(int isolationLevel)
    {
        if(! (isolationLevel == Connection.TRANSACTION_READ_COMMITTED ||
                isolationLevel == Connection.TRANSACTION_READ_UNCOMMITTED ||
                isolationLevel == Connection.TRANSACTION_REPEATABLE_READ ||
                isolationLevel == Connection.TRANSACTION_SERIALIZABLE) ) {
            throw new IllegalArgumentException("invalid isolation level "+isolationLevel);
        }

        this.isolationLevel = isolationLevel;
    }
}
