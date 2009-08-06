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

/**
 * A JavaBean containing assorted configuration properties for the Orb Portability layer.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.orbportability.")
public class OrbPortabilityEnvironmentBean implements OrbPortabilityEnvironmentBeanMBean
{
    private String propertiesFile;

    private String corbaDiagnostics; // key only
    private String initialReferencesRoot = com.arjuna.orbportability.common.Configuration.configFileRoot();
    private String initialReferencesFile = com.arjuna.orbportability.common.Configuration.configFile();
    private String fileDir = null;
    private String resolveService = "CONFIGURATION_FILE";
    private String eventHandler;
    private String orbImplementation = null;
    private String oaImplementation = null;
    private String bindMechanism = "CONFIGURATION_FILE";
    private String defaultConfigurationFilename;

//    public static final String PROPERTIES_FILE = "com.arjuna.orbportability.propertiesFile";
    public String getPropertiesFile()
    {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

//    public static final String CORBA_DIAGNOSTICS = "com.arjuna.orbportability.corbaDiagnostics";
    public String getCorbaDiagnostics()
    {
        return corbaDiagnostics;
    }

    public void setCorbaDiagnostics(String corbaDiagnostics)
    {
        this.corbaDiagnostics = corbaDiagnostics;
    }

//    public static final String INITIAL_REFERENCES_ROOT = "com.arjuna.orbportability.initialReferencesRoot";
    public String getInitialReferencesRoot()
    {
        return initialReferencesRoot;
    }

    public void setInitialReferencesRoot(String initialReferencesRoot)
    {
        this.initialReferencesRoot = initialReferencesRoot;
    }

//    public static final String INITIAL_REFERENCES_FILE = "com.arjuna.orbportability.initialReferencesFile";
    public String getInitialReferencesFile()
    {
        return initialReferencesFile;
    }

    public void setInitialReferencesFile(String initialReferencesFile)
    {
        this.initialReferencesFile = initialReferencesFile;
    }

//    public static final String FILE_DIR = "com.arjuna.orbportability.fileDir";
    public String getFileDir()
    {
        return fileDir;
    }

    public void setFileDir(String fileDir)
    {
        this.fileDir = fileDir;
    }

//    public static final String RESOLVE_SERVICE = "com.arjuna.orbportability.resolveService";
    public String getResolveService()
    {
        return resolveService;
    }

    public void setResolveService(String resolveService)
    {
        this.resolveService = resolveService;
    }

//    public static final String EVENT_HANDLER = "com.arjuna.orbportability.eventHandler";
    public String getEventHandler()
    {
        return eventHandler;
    }

    public void setEventHandler(String eventHandler)
    {
        this.eventHandler = eventHandler;
    }

//    public static final String ORB_IMPLEMENTATION = "com.arjuna.orbportability.orbImplementation";
    public String getOrbImplementation()
    {
        return orbImplementation;
    }

    public void setOrbImplementation(String orbImplementation)
    {
        this.orbImplementation = orbImplementation;
    }

//    public static final String OA_IMPLEMENTATION = "com.arjuna.orbportability.oaImplementation";
    public String getOaImplementation()
    {
        return oaImplementation;
    }

    public void setOaImplementation(String oaImplementation)
    {
        this.oaImplementation = oaImplementation;
    }

//    public static final String BIND_MECHANISM= "com.arjuna.orbportability.bindMechanism";
    public String getBindMechanism()
    {
        return bindMechanism;
    }

    public void setBindMechanism(String bindMechanism)
    {
        this.bindMechanism = bindMechanism;
    }

//    public static final String DEFAULT_ORB_CONFIGURATION = "com.arjuna.orbportability.defaultConfigurationFilename";    
    public String getDefaultConfigurationFilename()
    {
        return defaultConfigurationFilename;
    }

    public void setDefaultConfigurationFilename(String defaultConfigurationFilename)
    {
        this.defaultConfigurationFilename = defaultConfigurationFilename;
    }
}
