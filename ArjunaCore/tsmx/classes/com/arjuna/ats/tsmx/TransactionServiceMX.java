/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionServiceMX.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tsmx;

import com.arjuna.ats.tsmx.agent.AgentInterface;
import com.arjuna.ats.tsmx.agent.exceptions.AgentNotFoundException;
import com.arjuna.ats.tsmx.common.*;
import com.arjuna.ats.tsmx.logging.*;

import com.arjuna.common.util.logging.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Properties;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.InputStream;

public class TransactionServiceMX
{
	public final static String AGENT_IMPLEMENTATION_PROPERTY = "com.arjuna.ats.tsmx.agentimpl";
	private final static String DEFAULT_AGENT_IMPLEMENTATION = "com.arjuna.ats.internal.tsmx.agent.implementations.ri.RefAgentImpl";
	private final static String MBEAN_PROPERTY_PREFIX = "com.arjuna.ats.tsmx.mbean.";
	private final static String MBEAN_CLASSNAME_SUFFIX = ".classname";
	private final static String MBEAN_OBJECTNAME_SUFFIX = ".objectname";

	private static TransactionServiceMX _tsmx = null;

	public final static TransactionServiceMX getTransactionServiceMX()
	{
		if ( _tsmx == null )
		{
			_tsmx = new TransactionServiceMX();
		}

		return _tsmx;
	}

	private MBeanServer     _mbeanServer = null;
	private ArrayList	    _beans = null;
    private AgentInterface 	_agent = null;
    private Properties 		_tsmxProps = null;

	protected TransactionServiceMX()
	{
		if (tsmxLogger.logger.debugAllowed())
		{
			tsmxLogger.logger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC,
						 tsmxFacilityCode.FAC_TS_MX, "TransactionService Management Extentions initialising");
		}

        try
        {
            loadProperties();
        }
        catch (Exception e)
        {
            throw new Error("An error occurred while trying to load the tsmx properties: "+e);
        }

		try
		{
			_mbeanServer = _agent.getAgent();
		}
		catch (AgentNotFoundException e)
		{
			throw new Error("Failed to find agent: "+e);
		}

		_beans = new ArrayList();

		registerMBeans();
	}

    protected void loadProperties() throws Exception
    {
        if ( _agent == null )
        {
            _tsmxProps = System.getProperties();

            /** Find and load the tsmx properties file **/
            InputStream inStr = Thread.currentThread().getContextClassLoader().getResourceAsStream( Configuration.propertiesFile() );
            if ( inStr != null )
            {
                _tsmxProps.load(inStr);
            }

            /** Get the JMX agent implementation plugin, if none specified use reference implementation **/
            String agentImpl = _tsmxProps.getProperty(AGENT_IMPLEMENTATION_PROPERTY, DEFAULT_AGENT_IMPLEMENTATION);

            if ( tsmxLogger.logger.isInfoEnabled() )
            {
                tsmxLogger.logger.info("Initialising JMX agent "+agentImpl);
            }

            /** Create instance of JMX agent plugin **/
            _agent = (AgentInterface) Thread.currentThread().getContextClassLoader().loadClass(agentImpl).newInstance();
        }
    }

	/**
	 * Strip the mbean name from the configuration property.
	 *
	 * @param propertyName The full property name.
	 * @return The name of the mbean (e.g. com.arjuna.ats.tsmx.mbean.[name].classname returns [name])
	 */
	private final String stripNameFromProperty(String propertyName)
	{
		String name = null;

		if (propertyName.startsWith(MBEAN_PROPERTY_PREFIX))
		{
			if (propertyName.endsWith(MBEAN_CLASSNAME_SUFFIX))
			{
				name = propertyName.substring(MBEAN_PROPERTY_PREFIX.length());
				name = name.substring(0, name.indexOf(MBEAN_CLASSNAME_SUFFIX));
			}
			else if (propertyName.endsWith(MBEAN_OBJECTNAME_SUFFIX))
			{
				name = propertyName.substring(MBEAN_PROPERTY_PREFIX.length());
				name = name.substring(0, name.indexOf(MBEAN_OBJECTNAME_SUFFIX));
			}
		}

		return name;
	}

	private final static String getClassPropertyName(String mbeanName)
	{
		return MBEAN_PROPERTY_PREFIX + mbeanName + MBEAN_CLASSNAME_SUFFIX;
	}

	private final static String getObjectPropertyName(String mbeanName)
	{
		return MBEAN_PROPERTY_PREFIX + mbeanName + MBEAN_OBJECTNAME_SUFFIX;
	}

	public final String getObjectName(String mbeanName)
	{
		return _tsmxProps.getProperty(getObjectPropertyName(mbeanName));
	}

    public final Properties getProperties()
    {
        return _tsmxProps;
    }

    public final AgentInterface getAgentInterface()
    {
        return _agent;
    }

	/**
	 * Register all MBeans which are defined in the properties
	 * which are prefixed with <code>MBEAN_PROPERTY_PREFIX</code>
	 *
	 * @message com.arjuna.ats.tsmx.TransactionServiceMX.mbeanalreadyregistered MBean {0} already registered
	 * @message com.arjuna.ats.tsmx.TransactionServiceMX.failedtoregistermbean Failed to register MBean {0} : {1}
	 */
	private boolean registerMBeans()
	{
		boolean success = true;
		Properties props = getProperties();
		Enumeration propNames = props.propertyNames();
		HashSet foundProperties = new HashSet();

		if (tsmxLogger.logger.debugAllowed())
		{
			tsmxLogger.logger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC,
						 tsmxFacilityCode.FAC_TS_MX, "Registering transaction service mbeans");
		}


		while (propNames.hasMoreElements())
		{
			String propertyName = (String) propNames.nextElement();

			if (propertyName.startsWith(MBEAN_PROPERTY_PREFIX))
			{
				String mbeanName = stripNameFromProperty(propertyName);
				if (!foundProperties.contains(mbeanName))
				{
					String mbeanClassname = props.getProperty(getClassPropertyName(mbeanName));
					String mbeanObjectName = props.getProperty(getObjectPropertyName(mbeanName));
					try
					{
						if (tsmxLogger.logger.debugAllowed())
						{
							tsmxLogger.logger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC,
										 tsmxFacilityCode.FAC_TS_MX, "Registering mbean '"+mbeanClassname+"' against '"+mbeanObjectName+"'");
						}

						_mbeanServer.createMBean(mbeanClassname, new ObjectName(mbeanObjectName));
						_beans.add(mbeanObjectName);

						foundProperties.add(mbeanName);
					}
					catch (javax.management.InstanceAlreadyExistsException e)
					{
						if (tsmxLogger.logger.isWarnEnabled())
						{
							tsmxLogger.loggerI18N.warn("com.arjuna.ats.tsmx.TransactionServiceMX.mbeanalreadyregistered", new Object[] { mbeanObjectName } );
						}
					}
					catch (javax.management.MBeanException e)
					{
						if (tsmxLogger.logger.isErrorEnabled())
						{
							tsmxLogger.loggerI18N.error("com.arjuna.ats.tsmx.TransactionServiceMX.failedtoregistermbean", new Object[] { mbeanObjectName, e.getTargetException().toString() } );
						}
						success = false;
					}
					catch (Exception e)
					{
						if (tsmxLogger.logger.isErrorEnabled())
						{
							tsmxLogger.loggerI18N.error("com.arjuna.ats.tsmx.TransactionServiceMX.failedtoregistermbean", new Object[] { mbeanObjectName, e.toString() } );
						}
						success = false;
					}

				}
			}
		}

		return success;
	}

	/**
	 * Thie method unregisters all the mbeans registered.
	 *
	 * @return True if all beans were unregistered successfully.
	 *
	 * @message com.arjuna.ats.tsmx.TransactionServiceMX.failedtounregistermbean Failed to unregister MBean {0} : {1}
	 */
	public boolean unregisterMBeans()
	{
		boolean success = true;

    	for (int count=0;count<_beans.size();count++)
		{
			String name = (String)_beans.get(count);
			try
			{
				_mbeanServer.unregisterMBean(new ObjectName(name));

				_beans.remove(count);
			}
			catch (Exception e)
			{
				if (tsmxLogger.logger.isErrorEnabled())
				{
					tsmxLogger.loggerI18N.error("com.arjuna.ats.tsmx.TransactionServiceMX.failedtounregistermbean", new Object[] { name, e.toString() } );
				}
				success = false;
			}
		}

		return success;
	}
}
