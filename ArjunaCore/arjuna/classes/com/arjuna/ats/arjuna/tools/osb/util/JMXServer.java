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

import com.arjuna.ats.arjuna.tools.osb.mbean.common.BasicBean;
import com.arjuna.ats.arjuna.logging.tsLogger;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Simple wrapper for accessing the JMX server
 *
 * @message com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_1
 *          [com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_1] - registering bean {0}.
 * @message com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_2
 *          [com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_2] - Instance already exists: {0}.
 * @message com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_3
 *          [com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_3] - Error registrating {0} - {1}.
 * @message com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_4
 *          [com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_4] - Try to unregister mbean with invalid name {0}.
 * @message com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_5]
 *          [com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_5] - Unable to unregister bean {0} error: {1}.
 * @message com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_6
 *          [com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_6] - Unable to unregister bean {0} error: {1}.
 */

public class JMXServer
{
    private static MBeanServer server;
    private static JMXServer agent = new JMXServer();

    public static JMXServer getAgent() { return agent; }

    private List<BasicBean> registeredBeans = new ArrayList<BasicBean>();

    public JMXServer()
    {
        Class<?> c1;
        Class<?> c2;

        try {
            c1 = Class.forName("com.arjuna.ats.internal.jta.Implementations");
            c1.getMethod("initialise").invoke(null);
        } catch (Exception e) {
        }

        try {
            c1 = Class.forName("com.arjuna.ats.internal.jts.Implementations");
            c2 = Class.forName("com.arjuna.ats.internal.jta.Implementationsx"); // needed for XAResourceRecord

            c1.getMethod("initialise").invoke(null);
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

    public ObjectInstance registerMBean(BasicBean bean)
    {
        ObjectInstance oi = null;

        try {
            if (tsLogger.arjLoggerI18N.isInfoEnabled())
                tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_1",
                        new Object[] { bean.getObjectName() });
            oi = getServer().registerMBean(bean, new ObjectName(bean.getObjectName()));
            registeredBeans.add(bean);
        } catch (InstanceAlreadyExistsException e) {
            if (tsLogger.arjLoggerI18N.isInfoEnabled())
                tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_2",
                        new Object[] { bean.getObjectName() });
        } catch (javax.management.JMException e) {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_3",
                        new Object[] { bean.getObjectName(), e.getMessage() });
        }

		return oi;
    }

    public boolean unregisterMBean(String objectName)
	{
        boolean unregistered = false;
        try {
			unregistered = unregisterMBean(new ObjectName(objectName));
            Iterator<BasicBean> i = registeredBeans.iterator();

            while (i.hasNext()) {
                BasicBean bb = i.next();

                if (objectName.equals(bb.getObjectName())) {
                    i.remove();
                    break;
                }
            }
        } catch (MalformedObjectNameException e) {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_4",
                        new Object[] { e.getMessage() });
		}

        return unregistered;
    }

    public boolean unregisterMBean(ObjectName objectName)
    {
        try {
            getServer().unregisterMBean(objectName);
        	return true;
        } catch (InstanceNotFoundException e) {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_5",
                        new Object[] { objectName, e.getMessage() });
        } catch (MBeanRegistrationException e) {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.tools.osb.util.JMXServer.m_6",
                        new Object[] { objectName, e.getMessage() });
        }

        return false;
    }
}
