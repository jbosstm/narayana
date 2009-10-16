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
package com.arjuna.orbportability.common;

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;

import java.util.List;
import java.util.ArrayList;

/**
 * A JavaBean containing assorted configuration properties for the Orb Portability layer.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.orbportability.")
public class OrbPortabilityEnvironmentBean implements OrbPortabilityEnvironmentBeanMBean
{
    private volatile String propertiesFile = "";

    private volatile String corbaDiagnostics = null; // key only
    private volatile String initialReferencesRoot = com.arjuna.orbportability.common.Configuration.configFileRoot();
    private volatile String initialReferencesFile = com.arjuna.orbportability.common.Configuration.configFile();
    private volatile String fileDir = null;
    private volatile String resolveService = "CONFIGURATION_FILE";

    @ConcatenationPrefix(prefix = "com.arjuna.orbportability.eventHandler")
    private volatile List<String> eventHandlers = new ArrayList<String>();

    private volatile String orbImplementation = null;
    private volatile String oaImplementation = null;
    private volatile String bindMechanism = "CONFIGURATION_FILE";
    private volatile String defaultConfigurationFilename = null;

    /**
     * Returns the name of the properties file.
     *
     * Default: ""
     * Equivalent deprecated property: com.arjuna.orbportability.propertiesFile
     *
     * @return the name of the properties file
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
     * Unused.
     *
     * Default: null
     * Equivalent deprecated property: com.arjuna.orbportability.corbaDiagnostics
     *
     * @deprecated I'm not used - remove me
     * @return unused.
     */
    public String getCorbaDiagnostics()
    {
        return corbaDiagnostics;
    }

    /**
     * Unused.
     *
     * @param corbaDiagnostics unused.
     */
    public void setCorbaDiagnostics(String corbaDiagnostics)
    {
        this.corbaDiagnostics = corbaDiagnostics;
    }

    /**
     * Returns the name of the directory in which the initial reference file is stored.
     *
     * Default: "."
     * Equivalent deprecated property: com.arjuna.orbportability.initialReferencesRoot
     *
     * @return the path to the directory in which initial references are stored.
     */
    public String getInitialReferencesRoot()
    {
        return initialReferencesRoot;
    }

    /**
     * Sets the name of the directory in which the initial reference file is stored.
     *
     * @param initialReferencesRoot the path to the directory.
     */
    public void setInitialReferencesRoot(String initialReferencesRoot)
    {
        this.initialReferencesRoot = initialReferencesRoot;
    }

    /**
     * Returns the relative name of the file in which initial references are stored.
     *
     * Default: "CosServices.cfg"
     * Equivalent deprecated property: com.arjuna.orbportability.initialReferencesFile
     *
     * @return the name of the initial references file.
     */
    public String getInitialReferencesFile()
    {
        return initialReferencesFile;
    }

    /**
     * Sets the name of the initial references file.
     *
     * @param initialReferencesFile the file name, without directory path.
     */
    public void setInitialReferencesFile(String initialReferencesFile)
    {
        this.initialReferencesFile = initialReferencesFile;
    }

    /**
     * Returns the name of the directory to store reference files in.
     *
     * Default: null
     * Equivalent deprecated property: com.arjuna.orbportability.fileDir
     *
     * @return the path to the reference file directory.
     */
    public String getFileDir()
    {
        return fileDir;
    }

    /**
     * Sets the name of the direcory to store reference files in.
     *
     * @param fileDir the path to the reference file directory.
     */
    public void setFileDir(String fileDir)
    {
        this.fileDir = fileDir;
    }

    /**
     * Returns the symbolic name of the configuration mechanism for resolving service references.
     *
     * Default: "CONFIGURATION_FILE"
     * Equivalent deprecated property: com.arjuna.orbportability.resolveService
     *
     * @return the name of the configuration mechanism for service references.
     */
    public String getResolveService()
    {
        return resolveService;
    }

    /**
     * Sets the symbolic name of the configuration mechanism for resolving service references.
     *
     * @param resolveService the name of the service resolution configuration mechanism.
     */
    public void setResolveService(String resolveService)
    {
        this.resolveService = resolveService;
    }

    /**
     * Returns the classnames for the ORB object connect/disconnect event handlers.
     * The returned list is a copy. May return an empty list, will not return null.
     *
     * Default: empty list.
     * Equivalent deprecated property prefix: com.arjuna.orbportability.eventHandler
     *
     * @return a list of names of classes, being implementations of the EventHandler interface.
     */
    public List<String> getEventHandlers()
    {
        return new ArrayList<String>(eventHandlers);
    }

    /**
     * Sets the classnames of the ORB object connect/disconnect event handlers.
     * List elements should be names of classes that implement EventHandler.
     * The provided list will be copied, not retained.
     *
     * @param eventHandlers a list of EventHandler implementation classnames.
     */
    public void setEventHandlers(List<String> eventHandlers)
    {
        if(eventHandlers == null) {
            this.eventHandlers = new ArrayList<String>();
        } else {
            this.eventHandlers = new ArrayList<String>(eventHandlers);
        }
    }

    /**
     * Returns the classname of the ORBImple implementation.
     *
     * Default: null (i.e. use classpath based selection)
     * Equivalent deprecated property: com.arjuna.orbportability.orbImplementation
     *
     * @return the name of the class implementing ORBImple.
     */
    public String getOrbImplementation()
    {
        return orbImplementation;
    }

    /**
     * Sets the classname of the ORBImple implementation.
     *
     * @param orbImplementation the name of the class implementing ORBImple.
     */
    public void setOrbImplementation(String orbImplementation)
    {
        this.orbImplementation = orbImplementation;
    }

    /**
     * Returns the classname of the POAImple implementation.
     *
     * Default: null (i.e. user classpath based selection)
     * Equivalent deprecated property: com.arjuna.orbportability.oaImplementation
     *
     * @return the name of the class implementing POAImple.
     */
    public String getOaImplementation()
    {
        return oaImplementation;
    }

    /**
     * Sets the classname of the POAImple implementation.
     *
     * @param oaImplementation the name of the class implementing POAImple.
     */
    public void setOaImplementation(String oaImplementation)
    {
        this.oaImplementation = oaImplementation;
    }

    /**
     * Returns the symbolic name of the configuration mechanism used for service bindings.
     *
     * Default: "CONFIGURATION_FILE"
     * Equivalent deprecated property: com.arjuna.orbportability.bindMechanism
     *
     * @return the name of the service binding mechanism.
     */
    public String getBindMechanism()
    {
        return bindMechanism;
    }

    /**
     * Sets the symbolic name of the configuration mechanism used for service bindings.
     *
     * @param bindMechanism the name of the service binding mechanism.
     */
    public void setBindMechanism(String bindMechanism)
    {
        this.bindMechanism = bindMechanism;
    }

    /**
     * Returns the default name for the configuration file.
     *
     * Default: null
     * Equivalent deprecated property: com.arjuna.orbportability.defaultConfigurationFilename
     *
     * @deprecated I'm unused, remove me.
     * @return the default name of the configuration file.
     */
    public String getDefaultConfigurationFilename()
    {
        return defaultConfigurationFilename;
    }

    /**
     * Sets the default name for the configuration file.
     *
     * @param defaultConfigurationFilename the default name for the configuration file.
     */
    public void setDefaultConfigurationFilename(String defaultConfigurationFilename)
    {
        this.defaultConfigurationFilename = defaultConfigurationFilename;
    }
}
