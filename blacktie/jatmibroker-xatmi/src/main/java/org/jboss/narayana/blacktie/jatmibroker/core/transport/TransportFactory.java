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
package org.jboss.narayana.blacktie.jatmibroker.core.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.server.SocketServer;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.TransportImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;

public class TransportFactory {

    private static final Logger log = LogManager.getLogger(TransportFactory.class);
    private Properties properties;
    private SocketServer socketserver;
    private List<Transport> transports = new ArrayList<Transport>();

    private boolean closed;

    public TransportFactory(Properties properties) throws ConfigurationException {
        log.debug("Creating Transportfactory: " + this);
        this.properties = properties;

        try{
            socketserver = SocketServer.getInstance(properties);
        } catch (IOException e) {
            throw new ConfigurationException("Could not create socket server", e);
        }
        
        log.debug("Created SocketServer");
    }

    public synchronized Transport createTransport() {
        log.debug("Creating transport from factory: " + this);
        TransportImpl instance = new TransportImpl(socketserver, properties, this);
        transports.add(instance);
        log.debug("Created transport from factory: " + this + " transport: " + instance);
        return instance;
    }

    public void removeTransport(TransportImpl transportImpl) {
        boolean remove = transports.remove(transportImpl);
        log.debug("Transport was removed: " + transportImpl + " from: " + this + " result: " + remove);
    }

    /**
     * Make sure that the
     */
    public synchronized final void close() {
        log.debug("Close called: " + this);
        if (!closed) {
            log.debug("Going into shutdown");
            log.debug("Closing factory: " + this);
            Transport[] transport = new Transport[transports.size()];
            transport = transports.toArray(transport);
            for (int i = 0; i < transport.length; i++) {
                try {
                    log.debug("Closing transport: " + transport[i] + " from factory: " + this);
                    transport[i].close();
                } catch (ConnectionException e) {
                    log.warn("Transport could not be closed: " + e.getMessage(), e);
                }
            }
            transports.clear();
            closed = true;
        }
        if(socketserver != null) {
            SocketServer.discardInstance();
        }
        log.debug("Closed factory: " + getClass().getName());
    }
}
