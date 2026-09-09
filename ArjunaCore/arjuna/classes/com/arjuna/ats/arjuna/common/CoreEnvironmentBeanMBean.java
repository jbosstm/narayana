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
    /**
     * Returns the 'var' directory path.
     *
     * Default: {user.dir}/var/tmp
     * Equivalent deprecated property: com.arjuna.ats.arjuna.common.varDir
     *
     * @return the 'var' directory name.
     */
    String getVarDir();

    /**
     * Returns the Node Identifier.
     *
     * Default: null
     * Equivalent deprecated property: com.arjuna.ats.arjuna.nodeIdentifier
     *
     * @return the Node Identifier.
     */
    String getNodeIdentifier();

    /**
     * Returns the port number for the Socket based process id implementation.
     *
     * Default: 0 (use any free port)
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.utils.SocketProcessIdPort
     *
     * @return the port number.
     */
    int getSocketProcessIdPort();

    /**
     * Returns the maximum number of ports to search when looking for one that is free.
     *
     * Default: 1
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.utils.SocketProcessIdMaxPorts
     *
     * @return the maximum number of ports to try.
     */
    int getSocketProcessIdMaxPorts();

    /**
     * Returns the class name of the Process implementation to use.
     *
     * Default: "com.arjuna.ats.internal.arjuna.utils.SocketProcessId"
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.utils.processImplementation
     *
     * @return the name of a class implementing Process.
     */
    String getProcessImplementationClassName();

    /**
     * Returns the process id to use if ManualProcessId is selected. Should be uniq across all instances on the same host.
     *
     * Default: -1 (invalid, must be changed if used)
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.utils.pid
     *
     * @return the process id to use.
     */
    int getPid();

    /**
     * Returns if multiple last (i.e. one-phase) resources are allowed in the same transaction or not.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.allowMultipleLastResources
     *
     * @return true if multiple last resources are permitted, false otherwise.
     */
    boolean isAllowMultipleLastResources();

    /**
     * Returns if the per-transaction warning on enlistment of multiple last resources is disabled or not.
     *
     * Default: false.
     * Equivalent deprecated property: com.arjuna.ats.arjuna.disableMultipleLastResourcesWarning
     *
     * @return true if warning is disabled, false otherwise.
     */
    boolean isDisableMultipleLastResourcesWarning();

    /**
     * @return the version control tag of the source used, or "unknown"
     */
    String getBuildVersion();

    /**
     *  @return the build identification line indicating the os name and version and build date
     */
    String getBuildId();
}