/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jdbc.common;

import java.sql.Connection;
import java.util.Hashtable;

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

/**
 * A JavaBean containing configuration properties for the JDBC subsystem.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.jdbc.")
public class JDBCEnvironmentBean implements JDBCEnvironmentBeanMBean
{
    private volatile int isolationLevel = Connection.TRANSACTION_SERIALIZABLE;

    private volatile Hashtable jndiProperties = new Hashtable();

    public boolean defaultIsSameRMOverride;

    /**
     * Returns whether to override multiple connections for all drivers.
     *
     * Equivalent deprecated property: com.arjuna.ats.jdbc.defaultIsSameRMOverride"
     *
     * @return whether to try to reuse connection for all drivers.
     */
    public boolean getDefaultIsSameRMOverride() {
        return defaultIsSameRMOverride;
    }

    /**
     * Configure so all drivers attempt to support multiple connections.
     *
     * @param defaultIsSameRMOverride whether to try to reuse connection for all drivers.
     */
    public void setDefaultIsSameRMOverride(boolean defaultIsSameRMOverride) {
        this.defaultIsSameRMOverride = defaultIsSameRMOverride;
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

    /**
     * Returns the Hashtable used for the JNDI environment in transactional driver code.
     * The returned object is a clone. May return an empty Hashtable , will not return null.
     *
     * Default: empty Hashtable.
     *
     * Note: unlike previous versions, the contents is passed to InitialContext verbatim.
     * Hence String keys should be of the form e.g. "java.naming.factory.initial", not "Context.INITIAL_CONTEXT_FACTORY"
     *
     * @return a Hashtable object containing JNDI context information.
     */
    public Hashtable getJndiProperties()
    {
        return (Hashtable)jndiProperties.clone();
    }

    /**
     * Sets the JNDI properties used by transactional driver code.
     * The provided Hashtable will be cloned, not retained.
     *
     * @param jndiProperties a Hashtable object containing JNDI context information.
     */
    public void setJndiProperties(Hashtable jndiProperties)
    {
        if(jndiProperties == null) {
            this.jndiProperties = new Hashtable();
        } else {
            this.jndiProperties = (Hashtable)jndiProperties.clone();
        }
    }
}