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
import com.arjuna.ats.arjuna.utils.Utility;

/**
 * A JavaBean containing assorted configuration properties for the core transaction system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.")
public class CoreEnvironmentBean implements CoreEnvironmentBeanMBean
{
    @FullPropertyName(name = "com.arjuna.ats.arjuna.common.propertiesFile")
    private String propertiesFile = "";

//    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.inventory.staticInventoryImple")
//    private String staticInventoryImple;

    @FullPropertyName(name = "com.arjuna.ats.arjuna.common.varDir")
    private String varDir = null;

    @FullPropertyName(name = "com.arjuna.ats.arjuna.xa.nodeIdentifier")
    private String nodeIdentifier = null;

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.SocketProcessIdPort")
    private int socketProcessIdPort = 0;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.SocketProcessIdMaxPorts")
    private int socketProcessIdMaxPorts = 1;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.processImplementation")
    private String processImplementation = Utility.defaultProcessId;
    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.utils.pid")
    private int pid = -1;

    private boolean allowMultipleLastResources = false;
    private boolean disableMultipleLastResourcesWarning = false;

//    @FullPropertyName(name = "jbossts.bind.address")
//    private String bindAddress;


    public String getPropertiesFile()
    {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

    public String getVarDir()
    {
        return varDir;
    }

    public void setVarDir(String varDir)
    {
        this.varDir = varDir;
    }

    public String getNodeIdentifier()
    {
        return nodeIdentifier;
    }

    public void setNodeIdentifier(String nodeIdentifier)
    {
        this.nodeIdentifier = nodeIdentifier;
    }

    public int getSocketProcessIdPort()
    {
        return socketProcessIdPort;
    }

    public void setSocketProcessIdPort(int socketProcessIdPort)
    {
        Utility.validatePortRange(socketProcessIdPort);
        this.socketProcessIdPort = socketProcessIdPort;
    }

    public int getSocketProcessIdMaxPorts()
    {
        return socketProcessIdMaxPorts;
    }

    public void setSocketProcessIdMaxPorts(int socketProcessIdMaxPorts)
    {
        this.socketProcessIdMaxPorts = socketProcessIdMaxPorts;
    }

    public String getProcessImplementation()
    {
        return processImplementation;
    }

    public void setProcessImplementation(String processImplementation)
    {
        this.processImplementation = processImplementation;
    }

    public int getPid()
    {
        return pid;
    }

    public void setPid(int pid)
    {
        this.pid = pid;
    }

    public boolean isAllowMultipleLastResources()
    {
        return allowMultipleLastResources;
    }

    public void setAllowMultipleLastResources(boolean allowMultipleLastResources)
    {
        this.allowMultipleLastResources = allowMultipleLastResources;
    }

    public boolean isDisableMultipleLastResourcesWarning()
    {
        return disableMultipleLastResourcesWarning;
    }

    public void setDisableMultipleLastResourcesWarning(boolean disableMultipleLastResourcesWarning)
    {
        this.disableMultipleLastResourcesWarning = disableMultipleLastResourcesWarning;
    }
}
