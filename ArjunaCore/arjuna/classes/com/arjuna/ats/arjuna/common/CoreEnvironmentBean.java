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
package com.arjuna.ats.arjuna.common;

import com.arjuna.ats.internal.arjuna.common.ClassloadingUtility;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.common.util.ConfigurationInfo;
import com.arjuna.ats.arjuna.utils.Process;

import java.io.File;

/**
 * A JavaBean containing assorted configuration properties for the core transaction system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.")
public class CoreEnvironmentBean implements CoreEnvironmentBeanMBean
{
    @FullPropertyName(name = "com.arjuna.ats.arjuna.common.varDir")
    private volatile String varDir = System.getProperty("user.dir") + File.separator + "var" + File.separator + "tmp";

    @FullPropertyName(name = "com.arjuna.ats.arjuna.nodeIdentifier")
    private volatile String nodeIdentifier = null;

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.SocketProcessIdPort")
    private volatile int socketProcessIdPort = 0;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.SocketProcessIdMaxPorts")
    private volatile int socketProcessIdMaxPorts = 1;

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.processImplementation")
    private volatile String processImplementationClassName = Utility.defaultProcessId;
    private volatile Process processImplementation = null;

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.pid")
    private volatile int pid = -1;

    private volatile boolean allowMultipleLastResources = false;
    private volatile boolean disableMultipleLastResourcesWarning = false;


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
     */
    public void setNodeIdentifier(String nodeIdentifier)
    {
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

}
