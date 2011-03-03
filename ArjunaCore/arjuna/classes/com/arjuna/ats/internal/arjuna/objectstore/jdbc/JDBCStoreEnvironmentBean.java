/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.internal.arjuna.common.ClassloadingUtility;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;

/**
 * A JavaBean containing configuration properties for the JDBC based objectstore implementation.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public class JDBCStoreEnvironmentBean
{
    private volatile String jdbcUserDbAccessClassName = null;
    private volatile JDBCAccess jdbcUserDbAccess;
    private volatile String jdbcTxDbAccessClassName = null;
    private volatile JDBCAccess jdbcTxDbAccess;

    private volatile int jdbcPoolSizeInitial = 1;
    private volatile int jdbcPoolSizeMaximum = 1;
    private volatile boolean jdbcPoolPutConnections = false;

    private volatile int share = StateType.OS_UNSHARED;

    /**
     * Returns the class name of the JDBCAccess implementation used for the ObjectStore.
     *
     * Default: null
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.jdbcUserDbAccessClassName
     *
     * @return the name of a class implementing JDBCAccess.
     */
    public String getJdbcUserDbAccessClassName()
    {
        return jdbcUserDbAccessClassName;
    }

    /**
     * Sets the class name of the JDBCAccess implementation used for the ObjectStore.
     *
     * @param jdbcUserDbAccessClassName the name of the class implementing JDBCAccess.
     */
    public void setJdbcUserDbAccessClassName(String jdbcUserDbAccessClassName)
    {
        synchronized(this)
        {
            if(jdbcUserDbAccessClassName == null)
            {
                this.jdbcUserDbAccess = null;
            }
            else if(!jdbcUserDbAccessClassName.equals(this.jdbcUserDbAccessClassName))
            {
                this.jdbcUserDbAccess = null;
            }
            this.jdbcUserDbAccessClassName = jdbcUserDbAccessClassName;
        }
    }

    /**
     * Returns an instance of a class implementing JDBCAccess.
     *
     * If there is no pre-instantiated instance set and classloading or instantiation fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return a JDBCAccess implementation instance, or null.
     */
    public JDBCAccess getJdbcUserDbAccess()
    {
        if(jdbcUserDbAccess == null && jdbcUserDbAccessClassName != null)
        {
            synchronized (this) {
                if(jdbcUserDbAccess == null && jdbcUserDbAccessClassName != null) {
                    JDBCAccess instance = ClassloadingUtility.loadAndInstantiateClass(JDBCAccess.class, jdbcUserDbAccessClassName, null);
                    jdbcUserDbAccess = instance;
                }
            }
        }

        return jdbcUserDbAccess;
    }

    /**
     * Sets the instance of JDBCAccess
     *
     * @param instance an Object that implements JDBCAccess, or null.
     */
    public void setJdbcUserDbAccess(JDBCAccess instance)
    {
        synchronized(this)
        {
            JDBCAccess oldInstance = this.jdbcUserDbAccess;
            jdbcUserDbAccess = instance;

            if(instance == null)
            {
                this.jdbcUserDbAccessClassName = null;
            }
            else if(instance != oldInstance)
            {
                String name = ClassloadingUtility.getNameForClass(instance);
                this.jdbcUserDbAccessClassName = name;
            }
        }
    }


    /**
     * Returns the class name of the JDBCAccess implementation used for the ActionStore.
     *
     * Default: null
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.jdbcTxDbAccessClassName
     *
     * @return the name of a class implementing JDBCAccess.
     */
    public String getJdbcTxDbAccessClassName()
    {
        return jdbcTxDbAccessClassName;
    }

    /**
     * Sets the class name of the JDBCAccess implementation used for the ActionStore.
     *
     * @param jdbcTxDbAccessClassName the name of the class implementing JDBCAccess.
     */
    public void setJdbcTxDbAccessClassName(String jdbcTxDbAccessClassName)
    {
        synchronized(this)
        {
            if(jdbcTxDbAccessClassName == null)
            {
                this.jdbcTxDbAccess = null;
            }
            else if(!jdbcTxDbAccessClassName.equals(this.jdbcTxDbAccessClassName))
            {
                this.jdbcTxDbAccess = null;
            }
            this.jdbcTxDbAccessClassName = jdbcTxDbAccessClassName;
        }
    }

   /**
     * Returns an instance of a class implementing JDBCAccess.
     *
     * If there is no pre-instantiated instance set and classloading or instantiation fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return a JDBCAccess implementation instance, or null.
     */
    public JDBCAccess getJdbcTxDbAccess()
    {
          if(jdbcTxDbAccess == null && jdbcTxDbAccessClassName != null)
        {
            synchronized (this) {
                if(jdbcTxDbAccess == null && jdbcTxDbAccessClassName != null) {
                    JDBCAccess instance = ClassloadingUtility.loadAndInstantiateClass(JDBCAccess.class, jdbcTxDbAccessClassName, null);
                    jdbcTxDbAccess = instance;
                }
            }
        }

        return jdbcTxDbAccess;
    }

    /**
     * Sets the instance of JDBCAccess
     *
     * @param instance an Object that implements JDBCAccess, or null.
     */
    public void setJdbcTxDbAccess(JDBCAccess instance)
    {
        synchronized(this)
        {
            JDBCAccess oldInstance = this.jdbcTxDbAccess;
            jdbcTxDbAccess = instance;

            if(instance == null)
            {
                this.jdbcTxDbAccessClassName = null;
            }
            else if(instance != oldInstance)
            {
                String name = ClassloadingUtility.getNameForClass(instance);
                this.jdbcTxDbAccessClassName = name;
            }
        }
    }


    /**
     * Returns the number of connections to initialize in the pool at startup.
     *
     * Default: 1
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.jdbcPoolSizeInitial
     *
     * @return the initial size of the connection pool.
     */
    public int getJdbcPoolSizeInitial()
    {
        return jdbcPoolSizeInitial;
    }

    /**
     * Sets the number of the connection to initialize in the pool at startup.
     *
     * @param jdbcPoolSizeInitial the initial size of the connection pool.
     */
    public void setJdbcPoolSizeInitial(int jdbcPoolSizeInitial)
    {
        this.jdbcPoolSizeInitial = jdbcPoolSizeInitial;
    }

    /**
     * Returns the maximum number of connections to hold in the pool.
     *
     * Default: 1
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.jdbcPoolSizeMaximum"
     *
     * @return the maximum size of the connection pool.
     */
    public int getJdbcPoolSizeMaximum()
    {
        return jdbcPoolSizeMaximum;
    }

    /**
     * Sets the maximum number of connections to hold in the pool.
     *
     * @param jdbcPoolSizeMaximum the maximum size of the connection pool.
     */
    public void setJdbcPoolSizeMaximum(int jdbcPoolSizeMaximum)
    {
        this.jdbcPoolSizeMaximum = jdbcPoolSizeMaximum;
    }

    /**
     * Returns if connections should be returned to the pool after use.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.jdbcPoolPutConnections
     *
     * @deprecated I'm unused and should be removed.
     * @return true if connections should be reused, false otherwise.
     */
    public boolean isJdbcPoolPutConnections()
    {
        return jdbcPoolPutConnections;
    }

    /**
     * Sets if connections should be returned to the pool after use.
     *
     * @param jdbcPoolPutConnections true to enable connection reuse, false to disable.
     */
    public void setJdbcPoolPutConnections(boolean jdbcPoolPutConnections)
    {
        this.jdbcPoolPutConnections = jdbcPoolPutConnections;
    }

    /**
     * Returns the share mode for the ObjectStore, i.e., is this being shared
     * between VMs?
     *
     * Default: ObjectStore.OS_UNKNOWN
     * Equivalent deprecated property: com.arjuna.ats.arjuna.objectstore.share
     *
     * @return the default share mode.
     */
    public int getShare()
    {
        return share;
    }

    /**
     * Sets the share mode of the ObjectStore
     *
     * @param share a valid share mode.
     */
    public void setShare(int share)
    {
        this.share = share;
    }
}
