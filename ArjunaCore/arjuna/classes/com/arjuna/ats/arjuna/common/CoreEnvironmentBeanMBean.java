/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.common;

/**
 * A JMX MBean interface containing assorted configuration for the core transaction system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface CoreEnvironmentBeanMBean
{
    String getVarDir();

    String getNodeIdentifier();

    int getSocketProcessIdPort();

    int getSocketProcessIdMaxPorts();

    String getProcessImplementationClassName();

    int getPid();

    boolean isAllowMultipleLastResources();

    boolean isDisableMultipleLastResourcesWarning();

    String getBuildVersion();

    String getBuildId();
}