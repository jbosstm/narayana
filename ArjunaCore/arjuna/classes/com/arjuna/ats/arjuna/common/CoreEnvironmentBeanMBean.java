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

/**
 * A JMX MBean interface containing assorted configuration for the core transaction system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface CoreEnvironmentBeanMBean
{
    String getPropertiesFile();

    void setPropertiesFile(String propertiesFile);

    String getVarDir();

    void setVarDir(String varDir);

    String getNodeIdentifier();

    void setNodeIdentifier(String nodeIdentifier);

    int getSocketProcessIdPort();

    void setSocketProcessIdPort(int socketProcessIdPort);

    int getSocketProcessIdMaxPorts();

    void setSocketProcessIdMaxPorts(int socketProcessIdMaxPorts);

    String getProcessImplementation();

    void setProcessImplementation(String processImplementation);

    int getPid();

    void setPid(int pid);

    boolean isAllowMultipleLastResources();

    void setAllowMultipleLastResources(boolean allowMultipleLastResources);

    boolean isDisableMultipleLastResourcesWarning();

    void setDisableMultipleLastResourcesWarning(boolean disableMultipleLastResourcesWarning);
}
