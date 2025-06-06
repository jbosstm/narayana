/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.common;

import java.io.File;
import java.nio.charset.StandardCharsets;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.utils.Process;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.common.internal.util.ClassloadingUtility;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.util.ConfigurationInfo;

/**
 * A JavaBean containing assorted configuration properties for the core transaction system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.")
public class CoreEnvironmentBean implements CoreEnvironmentBeanMBean
{
    public static final int NODE_NAME_SIZE = 64;

    @FullPropertyName(name = "com.arjuna.ats.arjuna.common.varDir")
    private volatile String varDir = System.getProperty("user.dir") + File.separator + "var" + File.separator + "tmp";

    @FullPropertyName(name = "com.arjuna.ats.arjuna.nodeIdentifier")
    private volatile String nodeIdentifier = null;

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.SocketProcessIdPort")
    private volatile int socketProcessIdPort = 0;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.SocketProcessIdMaxPorts")
    private volatile int socketProcessIdMaxPorts = 1;

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.processImplementation")
    private volatile String processImplementationClassName = Utility.getDefaultProcessId();
    private volatile Process processImplementation = null;

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.pid")
    private volatile int pid = -1;

    private volatile boolean allowMultipleLastResources = false;
    private volatile boolean disableMultipleLastResourcesWarning = false;

    @FullPropertyName(name = "timeout.factor")
    private volatile int timeoutFactor = 1;

    private boolean logAndRethrow = true;

    /**
     * Returns the 'var' directory path.
     *
     * Default: {user.dir}/var/tmp
     * Equivalent deprecated property: com.arjuna.ats.arjuna.common.varDir
     *
     * @return the 'var' directory name.
     */
    public String getVarDir()
    {
        return varDir;
    }

    /**
     * Sets the 'var' directory path
     *
     * @param varDir the path to the 'var' directory.
     */
    public void setVarDir(String varDir)
    {
        this.varDir = varDir;
    }

    /**
     * Returns the Node Identifier.
     *
     * Default: null
     * Equivalent deprecated property: com.arjuna.ats.arjuna.nodeIdentifier
     *
     * @return the Node Identifier.
     */
    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    /**
     * Sets the node identifier. Should be uniq amongst all instances that share resource managers or an objectstore.
     *
     * @param nodeIdentifier the Node Identifier.
     * @throws CoreEnvironmentBeanException if node identifier is null or too long.
     */
    public void setNodeIdentifier(String nodeIdentifier) throws CoreEnvironmentBeanException
    {
        if (nodeIdentifier == null)
        {
            tsLogger.i18NLogger.fatal_nodename_null();
            throw new CoreEnvironmentBeanException(tsLogger.i18NLogger.get_fatal_nodename_null());
        }

        if (nodeIdentifier.getBytes(StandardCharsets.UTF_8).length > NODE_NAME_SIZE)
        {
            tsLogger.i18NLogger.fatal_nodename_too_long(nodeIdentifier, NODE_NAME_SIZE);
            throw new CoreEnvironmentBeanException(tsLogger.i18NLogger.get_fatal_nodename_too_long(nodeIdentifier, NODE_NAME_SIZE));
        }

    	this.nodeIdentifier = nodeIdentifier;
    }

    /**
     * Returns the port number for the Socket based process id implementation.
     *
     * Default: 0 (use any free port)
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.utils.SocketProcessIdPort
     *
     * @return the port number.
     */
    public int getSocketProcessIdPort()
    {
        return socketProcessIdPort;
    }

    /**
     * Sets the port on which the socket based process id implementation will listen.
     * Should be uniq amongst all instances on the same host.
     * A value of 0 will result in a random port.
     *
     * @param socketProcessIdPort the port number to bind to.
     */
    public void setSocketProcessIdPort(int socketProcessIdPort)
    {
        Utility.validatePortRange(socketProcessIdPort);
        this.socketProcessIdPort = socketProcessIdPort;
    }

    public int getTimeoutFactor()
    {
        return timeoutFactor;
    }

    public void setTimeoutFactor(int timeoutFactor)
    {
        this.timeoutFactor = timeoutFactor;
    }

    /**
     * Returns the maximum number of ports to search when looking for one that is free.
     *
     * Default: 1
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.utils.SocketProcessIdMaxPorts
     *
     * @return the maximum number of ports to try.
     */
    public int getSocketProcessIdMaxPorts()
    {
        return socketProcessIdMaxPorts;
    }

    /**
     * Sets the maximum number of ports the socket process id implemention will try when searching to find one that is free.
     *
     * @param socketProcessIdMaxPorts the maximum number of ports to try.
     */
    public void setSocketProcessIdMaxPorts(int socketProcessIdMaxPorts)
    {
        this.socketProcessIdMaxPorts = socketProcessIdMaxPorts;
    }

    /**
     * Returns the class name of the Process implementation to use.
     *
     * Default: "com.arjuna.ats.internal.arjuna.utils.SocketProcessId"
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.utils.processImplementation
     *
     * @return the name of a class implementing Process.
     */
    public String getProcessImplementationClassName()
    {
        return processImplementationClassName;
    }

    /**
     * Sets the class name of the Process implementation to use.
     *
     * @param processImplementationClassName the name of a class implementing Process.
     */
    public void setProcessImplementationClassName(String processImplementationClassName)
    {
        synchronized(this) {
            if(processImplementationClassName == null)
            {
                this.processImplementation = null;
            }
            else if(!processImplementationClassName.equals(this.processImplementationClassName))
            {
                this.processImplementation = null;
            }
            this.processImplementationClassName = processImplementationClassName;
        }
    }

    /**
     * Returns an instance of a class implementing com.arjuna.ats.arjuna.utils.Process.
     *
     * If there is no pre-instantiated instance set and classloading or instantiation fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return a Process implementation instance, or null.
     */
    public Process getProcessImplementation()
    {
        if(processImplementation == null && processImplementationClassName != null)
        {
            synchronized(this) {
                if(processImplementation == null && processImplementationClassName != null) {
                    processImplementation = ClassloadingUtility.loadAndInstantiateClass(Process.class, processImplementationClassName, null);
                }
            }
        }

        return processImplementation;
    }

    /**
     * Sets the instance of com.arjuna.ats.arjuna.utils.Process
     *
     * @param instance an Object that implements Process, or null.
     */
    public void setProcessImplementation(Process instance)
    {
        synchronized(this)
        {
            Process oldInstance = this.processImplementation;
            processImplementation = instance;

            if(instance == null)
            {
                this.processImplementationClassName = null;
            }
            else if(instance != oldInstance)
            {
                String name = ClassloadingUtility.getNameForClass(instance);
                this.processImplementationClassName = name;
            }
        }
    }

    /**
     * Returns the process id to use if ManualProcessId is selected. Should be uniq across all instances on the same host.
     *
     * Default: -1 (invalid, must be changed if used)
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.utils.pid
     *
     * @return the process id to use.
     */
    public int getPid()
    {
        return pid;
    }

    /**
     * Sets the process id to use if ManualProcessId is selected.
     * Should be on the range 1-65535 and uniq across all instances on the same host.
     *
     * @param pid the process id to use.
     */
    public void setPid(int pid)
    {
        this.pid = pid;
    }

    /**
     * Returns if multiple last (i.e. one-phase) resources are allowed in the same transaction or not.
     *
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.arjuna.allowMultipleLastResources
     *
     * @return true if multiple last resources are permitted, false otherwise.
     */
    public boolean isAllowMultipleLastResources()
    {
        return allowMultipleLastResources;
    }

    /**
     * Sets if multiple last (i.e. one-phase) resources are allowed in the same transaction or not.
     * Caution: setting a value of true weakens transactional (ACID) guarantees and is not recommended.
     *
     * @param allowMultipleLastResources true if multiple 1PC resource should be permitted, false otherwise.
     */
    public void setAllowMultipleLastResources(boolean allowMultipleLastResources)
    {
        this.allowMultipleLastResources = allowMultipleLastResources;
    }

    /**
     * Returns if the per-transaction warning on enlistment of multiple last resources is disabled or not.
     *
     * Default: false.
     * Equivalent deprecated property: com.arjuna.ats.arjuna.disableMultipleLastResourcesWarning
     *
     * @return true if warning is disabled, false otherwise.
     */
    public boolean isDisableMultipleLastResourcesWarning()
    {
        return disableMultipleLastResourcesWarning;
    }

    /**
     * Sets if the per-transaction warning on enlistment of multiple last resource is disabled or not.
     *
     * @param disableMultipleLastResourcesWarning true to disable the warning, false otherwise.
     */
    public void setDisableMultipleLastResourcesWarning(boolean disableMultipleLastResourcesWarning)
    {
        this.disableMultipleLastResourcesWarning = disableMultipleLastResourcesWarning;
    }

    /**
     * @return the version control tag of the source used, or "unknown"
     */
    public String getBuildVersion()
    {
        return ConfigurationInfo.getVersion();
    }

    /**
     *  @return the build identification line indicating the os name and version and build date
     */
    public String getBuildId()
    {
        return ConfigurationInfo.getBuildId();
    }

    /**
     * Due to historical reasons some exceptions (a small number) are logged just before the exception is thrown.
     * Such behavior is considered to be an antipattern. To avoid this behaviour call this setter with the value
     * false (the default is true). Note that there are only a few places in the code where this antipattern is
     * followed. The behaviour is now configurable because some of the log messages contain more information than
     * is available in the exception or the caller that receives the exception does not log the problem, or the
     * problem is reported by an internal thread, or the problem is on the server side of a client server interaction.
     *
     * @param logAndRethrow when false avoid, where possible, the log and rethrow antipattern
     */
    public void setLogAndRethrow(boolean logAndRethrow) {
        this.logAndRethrow = logAndRethrow;
    }

    /**
     * @link setLogAndRethrow
     */
    public boolean isLogAndRethrow() {
        return logAndRethrow;
    }
}