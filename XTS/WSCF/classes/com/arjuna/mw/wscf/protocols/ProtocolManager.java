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

import org.jboss.jbossts.xts.environment.WSCFEnvironmentBean;
import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf.logging.wscfLogger;
import com.arjuna.mwlabs.wscf.utils.ContextProvider;
import com.arjuna.mwlabs.wscf.utils.HLSProvider;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

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

        WSCFEnvironmentBean wscfEnvironmentBean = XTSPropertyManager.getWSCFEnvironmentBean();
        List<String> protocolImplementations = wscfEnvironmentBean.getProtocolImplementations();
        if (protocolImplementations == null) {
            wscfLogger.i18NLogger.info_protocols_ProtocolManager_1();
            return;
        }
        ListIterator<String> iterator = protocolImplementations.listIterator();
        List<Class<?>> contextProviderClasses =  new ArrayList<Class<?>>();
        List<Class<?>> hlsProviderClasses =  new ArrayList<Class<?>>();

        // look for protocol implementations
        
		while (iterator.hasNext())
		{
			String className = (String) iterator.next();
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
                        wscfLogger.i18NLogger.error_protocols_ProtocolManager_2(className);
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                wscfLogger.i18NLogger.error_protocols_ProtocolManager_3(className, cnfe);
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
                wscfLogger.i18NLogger.info_protocols_ProtocolManager_4(className, serviceType);
                Object object = clazz.newInstance();
                _protocols.put(serviceType, object);
            } catch (InstantiationException ie) {
                wscfLogger.i18NLogger.error_protocols_ProtocolManager_5(className, ie);
            } catch (IllegalAccessException iae) {
                wscfLogger.i18NLogger.error_protocols_ProtocolManager_5(className, iae);
            }
		}

        for (Class<?> clazz : contextProviderClasses) {
            String className = clazz.getName();
            try
            {
                ContextProvider contextProvider = clazz.getAnnotation(ContextProvider.class);
                if (contextProvider !=  null) {
                    String coordinationType = contextProvider.coordinationType();
                    wscfLogger.i18NLogger.info_protocols_ProtocolManager_4(className, coordinationType);
                    Object object = clazz.newInstance();
                    _protocols.put(coordinationType, object);
                }
            } catch (InstantiationException ie) {
                wscfLogger.i18NLogger.error_protocols_ProtocolManager_5(className, ie);
                ie.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException iae) {
                wscfLogger.i18NLogger.error_protocols_ProtocolManager_5(className, iae);
                iae.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
		}
	}

	private HashMap _protocols = new HashMap();
	private boolean _initialised = false;
}