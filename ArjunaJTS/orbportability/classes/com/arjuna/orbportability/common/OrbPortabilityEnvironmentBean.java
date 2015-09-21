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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arjuna.common.internal.util.ClassloadingUtility;
import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.orbportability.ORBData;
import com.arjuna.orbportability.event.EventHandler;
import com.arjuna.orbportability.oa.core.POAImple;
import com.arjuna.orbportability.orb.core.ORBImple;

/**
 * A JavaBean containing assorted configuration properties for the Orb Portability layer.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.orbportability.")
public class OrbPortabilityEnvironmentBean implements OrbPortabilityEnvironmentBeanMBean
{
    private volatile String initialReferencesRoot = System.getProperty("user.dir");
    private volatile String initialReferencesFile = "CosServices.cfg";
    private volatile String fileDir = null;
    private volatile String resolveService = "CONFIGURATION_FILE";

    @ConcatenationPrefix(prefix = "com.arjuna.orbportability.eventHandler")
    private volatile List<String> eventHandlerClassNames = new ArrayList<String>();
    private volatile List<EventHandler> eventHandlers = null;

    // alternative: com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4
    // alternative: com.arjuna.orbportability.internal.orbspecific.ibmorb.orb.implementations.ibmorb_7_1
    private volatile String orbImpleClassName = "com.arjuna.orbportability.internal.orbspecific.jacorb.orb.implementations.jacorb_2_0";
    private volatile Class<? extends ORBImple> orbImpleClass = null;

    // alternative: com.arjuna.orbportability.internal.orbspecific.javaidl.oa.implementations.javaidl_1_4
    private volatile String poaImpleClassName = "com.arjuna.orbportability.internal.orbspecific.jacorb.oa.implementations.jacorb_2_0";
    private volatile Class<? extends POAImple> poaImpleClass = null;

    // alternative: com.arjuna.orbportability.internal.orbspecific.versions.javaidl_1_4
    private volatile String orbDataClassName = "com.arjuna.orbportability.internal.orbspecific.versions.jacorb_2_0";
    private volatile ORBData orbData = null;

    private volatile String bindMechanism = "CONFIGURATION_FILE";

    private volatile Map<String,String> orbInitializationProperties = new HashMap<String, String>();

    private volatile boolean shutdownWrappedOrb = true;


    /**
     * Returns the name of the directory in which the initial reference file is stored.
     *
     * Default: value of System.getProperty("user.dir")
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
     * Sets the name of the directory to store reference files in.
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
     * Returns the class names for the ORB object connect/disconnect event handlers.
     * The returned list is a copy. May return an empty list, will not return null.
     *
     * Default: empty list.
     * Equivalent deprecated property prefix: com.arjuna.orbportability.eventHandler
     *
     * @return a list of names of classes, being implementations of the EventHandler interface.
     */
    public List<String> getEventHandlerClassNames()
    {
        synchronized(this) {
            return new ArrayList<String>(eventHandlerClassNames);
        }
    }

    /**
     * Sets the class names of the ORB object connect/disconnect event handlers.
     * List elements should be names of classes that implement EventHandler.
     * The provided list will be copied, not retained.
     *
     * @param eventHandlerClassNames a list of EventHandler implementation classnames.
     */
    public void setEventHandlerClassNames(List<String> eventHandlerClassNames)
    {
        synchronized(this)
        {
            if(eventHandlerClassNames == null)
            {
                this.eventHandlers = new ArrayList<EventHandler>();
                this.eventHandlerClassNames = new ArrayList<String>();
            }
            else if(!eventHandlerClassNames.equals(this.eventHandlerClassNames))
            {
                this.eventHandlers = null;
                this.eventHandlerClassNames = new ArrayList<String>(eventHandlerClassNames);
            }
        }
    }

    /**
     * Returns the set of EventHandler instances.
     * The returned list is a copy. May return an empty list, will not return null.
     *
     * If there is no pre-instantiated instance set and classloading or instantiation of one or more
     * elements fails, this method will log an appropriate warning and return a non-null set with
     * fewer elements.
     *
     * @return the set of EventHandler instances.
     */
    public List<EventHandler> getEventHandlers()
    {
        synchronized(this)
        {
            if(eventHandlers == null) {
                List<EventHandler> instances = ClassloadingUtility.loadAndInstantiateClassesWithInit(EventHandler.class, eventHandlerClassNames);
                eventHandlers = instances;
            }
            return new ArrayList<EventHandler>(eventHandlers);
        }
    }

    /**
     * Sets the instances of EventHandler.
     * The provided list will be copied, not retained.
     *
     * @param eventHandlers the set of EventHandler instances.
     */
    public void setEventHandlers(List<EventHandler> eventHandlers)
    {
        synchronized(this)
        {
            if(eventHandlers == null)
            {
                this.eventHandlers = new ArrayList<EventHandler>();
                this.eventHandlerClassNames = new ArrayList<String>();
            }
            else
            {
                this.eventHandlers = new ArrayList<EventHandler>(eventHandlers);
                List<String> names = ClassloadingUtility.getNamesForClasses(this.eventHandlers);
                this.eventHandlerClassNames = names;
            }
        }
    }

    /**
     * Returns the class name of the ORBImple implementation.
     *
     * Default: com.arjuna.orbportability.internal.orbspecific.jacorb.orb.implementations.jacorb_2_0
     * Equivalent deprecated property: com.arjuna.orbportability.orbImplementation
     *
     * @return the name of the class implementing ORBImple.
     */
    public String getOrbImpleClassName()
    {
        return orbImpleClassName;
    }

    /**
     * Sets the class name of the ORBImple implementation. The class should have a public default constructor.
     *
     * @param orbImpleClassName the name of the class implementing ORBImple.
     */
    public void setOrbImpleClassName(String orbImpleClassName)
    {
        synchronized(this)
        {
            if(orbImpleClassName == null)
            {
                this.orbImpleClass = null;
            }
            else if(!orbImpleClassName.equals(this.orbImpleClassName))
            {
                this.orbImpleClass = null;
            }
            this.orbImpleClassName = orbImpleClassName;
        }
    }

    /**
     * Returns a class implementing ORBImple.
     *
     * If classloading fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return an ORBImple implementation instance, or null.
     */
    public Class<? extends ORBImple> getOrbImpleClass()
    {
        if(orbImpleClass == null && orbImpleClassName != null)
        {
            synchronized(this) {
                if(orbImpleClass == null && orbImpleClassName != null) {
                    Class<? extends ORBImple> clazz = ClassloadingUtility.loadClass(ORBImple.class, orbImpleClassName);
                    orbImpleClass = clazz;
                }
            }
        }

        return orbImpleClass;
    }

    /**
     * Sets the ORBImple implementation class. The class should have a public default constructor.
     *
     * @param orbImpleClass a Class that implements ORBImple
     */
    public void setOrbImpleClass(Class<? extends ORBImple> orbImpleClass)
    {
        synchronized(this)
        {
            Class<? extends ORBImple> oldClass = this.orbImpleClass;
            this.orbImpleClass = orbImpleClass;

            if(orbImpleClass == null)
            {
                this.orbImpleClassName = null;
            }
            else if(orbImpleClass != oldClass)
            {
                String name = orbImpleClass.getName();
                this.orbImpleClassName = name;
            }
        }
    }

    /**
     * Returns the class name of the POAImple implementation.
     *
     * Default: com.arjuna.orbportability.internal.orbspecific.jacorb.oa.implementations.jacorb_2_0
     * Equivalent deprecated property: com.arjuna.orbportability.oaImplementation
     *
     * @return the name of the class implementing POAImple.
     */
    public String getPoaImpleClassName()
    {
        return poaImpleClassName;
    }

    /**
     * Sets the class name of the POAImple implementation. The class should have a public default constructor.
     *
     * @param poaImpleClassName the name of the class implementing POAImple.
     */
    public void setPoaImpleClassName(String poaImpleClassName)
    {
        synchronized(this)
        {
            if(poaImpleClassName == null)
            {
                this.poaImpleClass = null;
            }
            else if(!poaImpleClassName.equals(this.poaImpleClassName))
            {
                this.poaImpleClass = null;
            }
            this.poaImpleClassName = poaImpleClassName;
        }
    }

    /**
     * Returns a class implementing POAImple.
     *
     * If classloading fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return an POAImple implementation instance, or null.
     */
    public Class<? extends POAImple> getPoaImpleClass()
    {
        if(poaImpleClass == null && poaImpleClassName != null)
        {
            synchronized(this) {
                if(poaImpleClass == null && poaImpleClassName != null) {
                    Class<? extends POAImple> clazz = ClassloadingUtility.loadClass(POAImple.class, poaImpleClassName);
                    poaImpleClass = clazz;
                }
            }
        }

        return poaImpleClass;
    }

    /**
     * Sets the POAImple implementation class. The class should have a public default constructor.
     *
     * @param poaImpleClass a Class that implements POAImple
     */
    public void setPoaImpleClass(Class<? extends POAImple> poaImpleClass)
    {
        synchronized(this)
        {
            Class<? extends POAImple> oldClass = this.poaImpleClass;
            this.poaImpleClass = poaImpleClass;

            if(poaImpleClass == null)
            {
                this.poaImpleClassName = null;
            }
            else if(poaImpleClass != oldClass)
            {
                String name = poaImpleClass.getName();
                this.poaImpleClassName = name;
            }
        }
    }

    /**
     * Returns the class name of the ORBData implementation.
     *
     * Default: com.arjuna.orbportability.internal.orbspecific.versions.jacorb_2_0
     *
     * @return the name of the class implementing ORBData.
     */
    public String getOrbDataClassName()
    {
        return orbDataClassName;
    }

    /**
     * Sets the class name of the ORBData implementation. The class should have a public default constructor.
     *
     * @param orbDataClassName the name of the class implementing ORBData.
     */
    public void setOrbDataClassName(String orbDataClassName)
    {
        synchronized(this)
        {
            if(orbDataClassName == null)
            {
                this.orbData = null;
            }
            else if(!orbDataClassName.equals(this.orbDataClassName))
            {
                this.orbData = null;
            }
            this.orbDataClassName = orbDataClassName;
        }
    }

    /**
     * Returns an instance of a class implementing ORBData.
     *
     * If there is no pre-instantiated instance set and classloading or instantiation fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return an ORBData implementation instance, or null.
     */
    public ORBData getOrbData()
    {
        if(orbData == null && orbDataClassName != null)
        {
            synchronized(this) {
                if(orbData == null && orbDataClassName != null) {
                    ORBData instance = ClassloadingUtility.loadAndInstantiateClass(ORBData.class,  orbDataClassName, null);
                    orbData = instance;
                }
            }
        }

        return orbData;
    }

    /**
     * Sets the instance of ORBData
     *
     * @param instance an Object that implements ORBData, or null.
     */
    public void setOrbData(ORBData instance)
    {
        synchronized(this)
        {
            ORBData oldInstance = this.orbData;
            orbData = instance;

            if(instance == null)
            {
                this.orbDataClassName = null;
            }
            else if(instance != oldInstance)
            {
                String name = ClassloadingUtility.getNameForClass(instance);
                this.orbDataClassName = name;
            }
        }
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
     * Returns the Map used for the orb initialization parameters. As there are potentially
     * an arbitrary number of ORBs, each with an arbitrary set of initialization classes, it's not
     * well suited to bean based properties :-(
     * The returned object is a clone. May return an empty Map, will not return null.
     *
     * Default: empty Map.
     *
     * @return a Map containing ORB initialization information.
     */
    public Map<String, String> getOrbInitializationProperties()
    {
        return new HashMap<String, String>(orbInitializationProperties);
    }

    /**
     * Sets the Map of properties used for ORB initialization.
     * The provided Map will be copied, not retained.
     *
     * @param orbInitializationProperties a Map containing ORB initialization information.
     */
    public void setOrbInitializationProperties(Map<String, String> orbInitializationProperties)
    {
        if(orbInitializationProperties == null) {
            this.orbInitializationProperties = new HashMap<String, String>();
        } else {
            this.orbInitializationProperties = new HashMap<String, String>(orbInitializationProperties);
        }
    }

    /**
     * The orb portability layer wraps the actual orb implementation. This property determines whether or
     * shutting down the orb portability layer will also shutdown the actual orb. This method is useful for
     * externally supplied orbs that should not be shutdown when the TransactionService is stopped
     * @param shutdownWrappedOrb
     */
    public void setShutdownWrappedOrb(boolean shutdownWrappedOrb) {
        this.shutdownWrappedOrb = shutdownWrappedOrb;
    }

    /**
     * Indicates whether the orb wrapped by the orb portability layer will be shutdown when the orb portability layer
     * is shutdown
     *
     * @return true if the wrapped orb will be shutdown
     */
    public boolean isShutdownWrappedOrb() {
        return shutdownWrappedOrb;
    }
}
