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

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;
import com.arjuna.ats.arjuna.utils.Utility;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

/**
 * A JavaBean containing assorted configuration properties for the core transaction system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.")
public class CoreEnvironmentBean implements CoreEnvironmentBeanMBean
{
    @FullPropertyName(name = "com.arjuna.ats.arjuna.common.propertiesFile")
    private volatile String propertiesFile = "";

    @ConcatenationPrefix(prefix = "com.arjuna.ats.internal.arjuna.inventory.staticInventoryImple")
    private volatile List<String> staticInventoryElements = new ArrayList<String>();

    @FullPropertyName(name = "com.arjuna.ats.arjuna.common.varDir")
    private volatile String varDir = System.getProperty("user.dir") + File.separator + "var" + File.separator + "tmp";

    @FullPropertyName(name = "com.arjuna.ats.arjuna.xa.nodeIdentifier")
    private volatile String nodeIdentifier = null;

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.SocketProcessIdPort")
    private volatile int socketProcessIdPort = 0;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.SocketProcessIdMaxPorts")
    private volatile int socketProcessIdMaxPorts = 1;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.processImplementation")
    private volatile String processImplementation = Utility.defaultProcessId;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.pid")
    private volatile int pid = -1;

    private volatile boolean allowMultipleLastResources = false;
    private volatile boolean disableMultipleLastResourcesWarning = false;

//    @FullPropertyName(name = "jbossts.bind.address")
//    private String bindAddress;


    /**
     * Returns the name of the properties file.
     *
     * Default: ""
     * Equivalent deprecated property: com.arjuna.ats.arjuna.common.propertiesFile
     *
     * @return the name of the properties file.
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
     * Returns a list of names of classes that implement InventoryElement.
     * The returned list is a copy. May return an empty list, will not return null.
     *
     * Default: empty list.
     * Equivalent deprecated property prefix: com.arjuna.ats.internal.arjuna.inventory.staticInventoryImple
     *
     * @return a list of InventoryElement implementation class names.
     */
    public List<String> getStaticInventoryElements()
    {
        return new ArrayList<String>(staticInventoryElements);
    }

    /**
     * Sets the inventory extensions.
     * List elements should be names of classes that implement InventoryElement.
     * The provided list will be copied, not retained.
     *
     * @param staticInventoryElements a list of InventoryElement implementation class names.
     */
    public void setStaticInventoryElements(List<String> staticInventoryElements)
    {
        if(staticInventoryElements == null) {
            this.staticInventoryElements = new ArrayList<String>();
        } else {
            this.staticInventoryElements = new ArrayList<String>(staticInventoryElements);
        }
    }

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
     * Equivalent deprecated property: com.arjuna.ats.arjuna.xa.nodeIdentifier
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
     * @return the name of a class implemeting Process.
     */
    public String getProcessImplementation()
    {
        return processImplementation;
    }

    /**
     * Sets the class name of the Process implementation to use.
     *
     * @param processImplementation the name of a class implementing Process.
     */
    public void setProcessImplementation(String processImplementation)
    {
        this.processImplementation = processImplementation;
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
}
