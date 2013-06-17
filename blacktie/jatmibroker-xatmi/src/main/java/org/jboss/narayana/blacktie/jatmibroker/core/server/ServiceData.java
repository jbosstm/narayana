/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
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
 */
package org.jboss.narayana.blacktie.jatmibroker.core.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Transport;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.TransportFactory;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Service;

public class ServiceData {

    private static final Logger log = LogManager.getLogger(ServiceData.class);
    private static final String DEFAULT_POOL_SIZE = "2";
    private List<ServiceDispatcher> dispatchers = new ArrayList<ServiceDispatcher>();
    private Transport connection;
    private String serviceClassName;
    private String serviceName;

    public ServiceData(TransportFactory transportFactory, Properties properties, String serviceName, String serviceClassName)
            throws ConnectionException, InstantiationException, IllegalAccessException, ClassNotFoundException,
            ConfigurationException {
        this.serviceName = serviceName;
        this.serviceClassName = serviceClassName;

        String sizeS = properties.getProperty("blacktie." + serviceName + ".size", DEFAULT_POOL_SIZE);
        int size = Integer.parseInt(sizeS);

        connection = transportFactory.createTransport();
        Boolean conversational = (Boolean) properties.get("blacktie." + serviceName + ".conversational");

        Class callback = Class.forName(serviceClassName);
        for (int i = 0; i < size; i++) {
            dispatchers.add(new ServiceDispatcher(serviceName, (Service) callback.newInstance(), connection.getReceiver(
                    serviceName, conversational), i));
        }
    }

    public void close() throws ConnectionException {
        log.debug("Unadvertising: " + serviceName);

        // Clean up the consumers
        Iterator<ServiceDispatcher> iterator = dispatchers.iterator();
        while (iterator.hasNext()) {
            iterator.next().startClose();
        }

        // Disconnect the transport
        connection.close();
        connection = null;

        // Clean up the consumers
        iterator = dispatchers.iterator();
        while (iterator.hasNext()) {
            iterator.next().close();
        }
        dispatchers.clear();
        log.info("Unadvertised: " + serviceName);
    }

    public String getServiceClassName() {
        return serviceClassName;
    }
}
