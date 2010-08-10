/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * (C) 2005-2010,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ProtocolManager.java,v 1.13 2005/05/19 12:13:28 nmcl Exp $
 */

package com.arjuna.mw.wscf.protocols;

import com.arjuna.mw.wscf.common.Environment;
import com.arjuna.mw.wscf.exceptions.ProtocolAlreadyRegisteredException;
import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf.logging.wscfLogger;
import com.arjuna.mwlabs.wscf.utils.ContextProvider;
import com.arjuna.mwlabs.wscf.utils.HLSProvider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * The ProtocolManager is the way in which protocol implementations may be
 * registered with the system.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id:$
 * @since 1.0.
 */

public class ProtocolManager
{

	/**
	 * Add a specific protocol implementation to the factory.
	 *
	 * @param     protocolName The name of the protocol.
	 * @param     protocolImplementor The class that implements the protocol.
	 *
	 * @exception com.arjuna.mw.wscf.exceptions.ProtocolAlreadyRegisteredException
	 *                Thrown if the exact same protocol definition has already
	 *                been registered.
	 * @exception IllegalArgumentException
	 *                Thrown if either of the parameters is invalid.
	 */

	public void addProtocol (String protocolName, Object protocolImplementor)
			throws ProtocolAlreadyRegisteredException,
			IllegalArgumentException
	{
        synchronized(this) {
            if ((protocolName == null) || (protocolImplementor == null))
            {
                throw new IllegalArgumentException();
            }

            if (_protocols.get(protocolName) != null) {
                throw new ProtocolAlreadyRegisteredException();
            } else {
                _protocols.put(protocolName, protocolImplementor);
            }
        }
	}

	/**
	 * Replace a specific protocol implementation in the factory.
	 *
	 * @param     protocolName The name of the protocol.
	 * @param     protocolImplementor The class that implements the protocol.
	 *
	 * @exception com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException
	 *                Thrown if the protocol to be removed has not been
	 *                registered.
	 * @exception IllegalArgumentException
	 *                Thrown if either of the parameters is invalid.
	 */

	public void replaceProtocol (String protocolName, Object protocolImplementor)
			throws ProtocolNotRegisteredException, IllegalArgumentException
	{
		synchronized (this)
		{
            if ((protocolName == null) || (protocolImplementor == null))
            {
                throw new IllegalArgumentException();
            }

            if (_protocols.get(protocolName) == null) {
                throw new ProtocolNotRegisteredException();
            } else {
                _protocols.put(protocolName, protocolImplementor);
            }
		}
	}

	/**
     * @param     protocolName The name of the protocol.
	 *
	 * @exception com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException
	 *                Thrown if the requested coordination protocol has not been
	 *                registered.
	 * @exception IllegalArgumentException
	 *                Thrown if the parameter is invalid.
	 *
	 * @return The class that implements the specified coordination protocol.
	 *
	 */

	public Object getProtocolImplementation (String protocolName)
			throws ProtocolNotRegisteredException, IllegalArgumentException
	{
        synchronized (this)
        {
            if (protocolName == null)
            {
                throw new IllegalArgumentException();
            }

            Object object = _protocols.get(protocolName);

            if (object == null) {
                throw new ProtocolNotRegisteredException(wscfLogger.i18NLogger.get_mw_wscf11_protocols_ProtocolManager_1()
                        + protocolName);
            }
            return object;
        }
	}

	/**
	 * Remove the specified protocol definition from the factory.
	 *
     * @param     protocolName The name of the protocol.
	 *
	 * @exception com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException
	 *                Thrown if the protocol to be removed has not been
	 *                registered.
	 * @exception IllegalArgumentException
	 *                Thrown if the paramater is invalid (e.g., null).
	 *
	 * @return the protocol implementation removed.
	 */

	public Object removeProtocol (String protocolName)
			throws ProtocolNotRegisteredException, IllegalArgumentException
	{
        synchronized (this)
        {
            if (protocolName == null)
            {
                throw new IllegalArgumentException();
            }

            Object object = _protocols.remove(protocolName);

            if (object == null) {
                throw new ProtocolNotRegisteredException(wscfLogger.i18NLogger.get_mw_wscf11_protocols_ProtocolManager_1()
                        + protocolName);
            }

            return object;
        }
	}

	/*
	 * install all registered protocol implementations which should be either context factories
	 * or high level services
	 */

	public synchronized final void initialise ()
	{
		if (_initialised)
			return;
		else
			_initialised = true;

        // TODO
        // load properties file and add to environment -- eventually this will be autorun by
        // the XTS configuration bean

        InputStream is = ProtocolManager.class.getResourceAsStream("/xts.properties");
        if (is != null) {
            Properties props = new Properties();
            try {
                props.load(is);
                Enumeration names = props.propertyNames();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    if (name.startsWith("org.jboss.jbossts.xts") ||
                            name.startsWith(("com.arjuna.mw.wscf"))) {
                        System.setProperty(name, props.getProperty(name));
                    }
                }
            } catch(IOException ioe) {
                System.out.println("cannot read xts.properties : " + ioe);
                ioe.printStackTrace();
            }
        }

		Properties props = System.getProperties();
		Enumeration names = props.propertyNames();
        List<Class<?>> contextProviderClasses =  new ArrayList<Class<?>>();
        List<Class<?>> hlsProviderClasses =  new ArrayList<Class<?>>();

        // look for protocol implementations
        
		while (names.hasMoreElements())
		{
			String name = (String) names.nextElement();


			if (name.startsWith(Environment.PROTOCOL_IMPLEMENTATION))
			{
                String className = props.getProperty(name);
                Class<?> clazz = null;
                try {
                    clazz = this.getClass().getClassLoader().loadClass(className);
                    ContextProvider contextProvider = clazz.getAnnotation(ContextProvider.class);
                    if (contextProvider !=  null) {
                        contextProviderClasses.add(clazz);
                    } else {
                        HLSProvider hlsProvider = clazz.getAnnotation(HLSProvider.class);
                        if (hlsProvider !=  null) {
                            hlsProviderClasses.add(clazz);
                        } else {
                            System.out.println("ProtocolManager : Unknown protocol implementation : " + className);
                        }
                    }
                } catch (ClassNotFoundException cnfe) {
                    // TODO -- proper log message
                    System.out.println("ProtocolManager : Unable to load protocol implementation class : " + className);
                    cnfe.printStackTrace();
                }
            }
        }

        // we need to create the high level services before context factories since the latter need to
        // cross-reference the former
        
        for (Class<?> clazz : hlsProviderClasses) {
            String className = clazz.getName();
            try
            {
                HLSProvider hlsProvider = clazz.getAnnotation(HLSProvider.class);
                String serviceType = hlsProvider.serviceType();
                System.out.println("ProtocolManager : Installing implementation class : " + className + " for service type " + serviceType);
                Object object = clazz.newInstance();
                _protocols.put(serviceType, object);
            } catch (InstantiationException ie) {
                // TODO -- proper log message
                System.out.println("ProtocolManager : Unable to instantiate protocol implementation class : " + className);
                ie.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException iae) {
                // TODO -- proper log message
                System.out.println("ProtocolManager : Unable to instantiate protocol implementation class : " + className);
                iae.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
		}

        for (Class<?> clazz : contextProviderClasses) {
            String className = clazz.getName();
            try
            {
                ContextProvider contextProvider = clazz.getAnnotation(ContextProvider.class);
                if (contextProvider !=  null) {
                    String coordinationType = contextProvider.coordinationType();
                    System.out.println("ProtocolManager : Installing implementation class : " + className + " for coordination type " + coordinationType);
                    Object object = clazz.newInstance();
                    _protocols.put(coordinationType, object);
                }
            } catch (InstantiationException ie) {
                // TODO -- proper log message
                System.out.println("ProtocolManager : Unable to instantiate protocol implementation class : " + className);
                ie.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException iae) {
                // TODO -- proper log message
                System.out.println("ProtocolManager : Unable to instantiate protocol implementation class : " + className);
                iae.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
		}
	}

	private HashMap _protocols = new HashMap();
	private boolean _initialised = false;
}