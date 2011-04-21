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
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.arjuna.tools.osb.util;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreItemMBean;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

/**
 * Simple wrapper for accessing the JMX server
 */

public class JMXServer
{
	public static String JTS_INITIALISER_CNAME = "com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ToolsInitialiser";
	public static String AJT_RECORD_TYPE = "CosTransactions/XAResourceRecord";
	public static String AJT_WRAPPER_TYPE = "com.arjuna.ats.internal.jta.tools.osb.mbean.jts.ArjunaTransactionImpleWrapper";
	public static String AJT_XAREC_TYPE = "com.arjuna.ats.internal.jta.tools.osb.mbean.jts.XAResourceRecordBean";

	private static MBeanServer server;
	private static JMXServer agent = new JMXServer();
	public static JMXServer getAgent() { return agent; }

	public static boolean isJTS() {return getAgent().isJTS;}

	private boolean isJTS;

	public JMXServer()
	{
		Class<?> c1;
		Class<?> c2;

		try {
			Class cl = Class.forName(JTS_INITIALISER_CNAME);
			Constructor constructor = cl.getConstructor();
			constructor.newInstance();
			isJTS = true;
		} catch (Exception e) { // ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException
			if (tsLogger.logger.isTraceEnabled())
				tsLogger.logger.trace("JTS not available: " + e);
		}


		try {
			c1 = Class.forName("com.arjuna.ats.internal.jta.Implementations");
			c1.getMethod("initialise").invoke(null);
		} catch (Exception e) {
		}

		try {
			c2 = Class.forName("com.arjuna.ats.internal.jta.Implementationsx"); // needed for XAResourceRecord

			c2.getMethod("initialise").invoke(null);
		} catch (Exception e) {
		}
	}

	public MBeanServer getServer()
	{
		if (server == null)
		{
			List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

			if (servers != null && servers.size() > 0)
				server = servers.get(0);
			else
				server = ManagementFactory.getPlatformMBeanServer();

			if (server == null)
				server = MBeanServerFactory.createMBeanServer();
		}

		return server;
	}

	public ObjectInstance registerMBean(String name, ObjStoreItemMBean bean)
	{
		ObjectInstance oi = null;

		try {
			if (tsLogger.logger.isDebugEnabled())
				tsLogger.logger.debug("registering bean " + name);
			//tsLogger.i18NLogger.info_tools_osb_util_JMXServer_m_1(name);
			oi = getServer().registerMBean(bean, new ObjectName(name));
		} catch (InstanceAlreadyExistsException e) {
			tsLogger.i18NLogger.info_tools_osb_util_JMXServer_m_2(name);
		} catch (javax.management.JMException e) {
            tsLogger.i18NLogger.warn_tools_osb_util_JMXServer_m_3(name, e);
        }

		return oi;
	}

	public boolean unregisterMBean(String name)
	{
		try {
			getServer().unregisterMBean(new ObjectName(name));
			return true;
		} catch (MalformedObjectNameException e) {
            tsLogger.i18NLogger.warn_tools_osb_util_JMXServer_m_5(name, e);
        } catch (InstanceNotFoundException e) {
            tsLogger.i18NLogger.warn_tools_osb_util_JMXServer_m_5(name, e);
        } catch (MBeanRegistrationException e) {
            // can't happen - none of our beans implement the MBeanRegistration interface
            tsLogger.i18NLogger.warn_tools_osb_util_JMXServer_m_6(name, e);
        }

		return false;
	}

	public Set<ObjectName> queryNames(String name, QueryExp query) throws MalformedObjectNameException {
		return getServer().queryNames(new ObjectName(name), query);
	}
}
