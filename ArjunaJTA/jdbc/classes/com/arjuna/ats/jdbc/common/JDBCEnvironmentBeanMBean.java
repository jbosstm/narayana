/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jdbc.common;

import java.util.Hashtable;

/**
 * A JMX MBean interface containing configuration for the JDBC subsystem.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface JDBCEnvironmentBeanMBean
{
    int getIsolationLevel();

    public Hashtable getJndiProperties();
}