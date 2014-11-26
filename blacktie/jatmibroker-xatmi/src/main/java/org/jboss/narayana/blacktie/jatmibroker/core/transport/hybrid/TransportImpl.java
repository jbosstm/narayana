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
package org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid;

import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.ResponseMonitor;
import org.jboss.narayana.blacktie.jatmibroker.core.server.SocketServer;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.EventListener;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.OrbManagement;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Receiver;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Sender;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Transport;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.TransportFactory;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.StompReceiverImpl;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.StompSenderImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;

public class TransportImpl implements Transport {

    private static final Logger log = LogManager.getLogger(TransportImpl.class);
    private OrbManagement orbManagement;
    private SocketServer socketserver;
    private Properties properties;
    private TransportFactory transportFactoryImpl;
    private boolean closed;

    private Map<Boolean, Map<String, Sender>> senders = new HashMap<Boolean, Map<String, Sender>>();
    private Map<Boolean, Map<String, Receiver>> receivers = new HashMap<Boolean, Map<String, Receiver>>();

    public TransportImpl(OrbManagement orbManagement, SocketServer socketserver, Properties properties, TransportFactory transportFactoryImpl) {
        log.debug("Creating transport");
        this.orbManagement = orbManagement;
        this.socketserver = socketserver;
        this.properties = properties;
        this.transportFactoryImpl = transportFactoryImpl;
        log.debug("Created transport");
    }

    public void close() throws ConnectionException {
        log.debug("Close called: " + this);
        if (!closed) {
            // SENDERS
            {
                Collection<Map<String, Sender>> values = senders.values();
                Iterator<Map<String, Sender>> iterator = values.iterator();
                while (iterator.hasNext()) {
                    Collection<Sender> next = iterator.next().values();
                    Iterator<Sender> iterator2 = next.iterator();
                    while (iterator2.hasNext()) {
                        iterator2.next().close();
                    }
                }
            }
            // RECEIVERS
            {
                Collection<Map<String, Receiver>> values = receivers.values();
                Iterator<Map<String, Receiver>> iterator = values.iterator();
                while (iterator.hasNext()) {
                    Collection<Receiver> next = iterator.next().values();
                    Iterator<Receiver> iterator2 = next.iterator();
                    while (iterator2.hasNext()) {
                        iterator2.next().close();
                    }
                }
            }
            transportFactoryImpl.removeTransport(this);
            closed = true;
        }
        log.debug("Closed: " + this);
    }

    public Sender getSender(String serviceName, boolean conversational) throws ConnectionException {
        if (closed) {
            log.error("Already closed");
            throw new ConnectionException(Connection.TPEPROTO, "Already closed");
        }
        log.debug("Get sender: " + serviceName);
        Map<String, Sender> conversationalMap = senders.get(conversational);
        if (conversationalMap == null) {
            conversationalMap = new HashMap<String, Sender>();
            senders.put(conversational, conversationalMap);
        }

        Sender toReturn = conversationalMap.get(serviceName);
        if (toReturn == null) {
            try {
                String type = (String) properties.get("blacktie." + serviceName + ".type");
                toReturn = new StompSenderImpl(serviceName, conversational, type, conversationalMap, properties);
                conversationalMap.put(serviceName, toReturn);
            } catch (ConnectionException e) {
                throw e;
            } catch (Throwable t) {
                throw new ConnectionException(org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection.TPESYSTEM,
                        "Could not create a service sender: " + t.getMessage(), t);
            }
        }
        return toReturn;
    }

    public Sender createSender(Object destination) throws ConnectionException {
        if (closed) {
            log.error("Already closed");
            throw new ConnectionException(Connection.TPEPROTO, "Already closed");
        }
        Sender sender;
        String callback_ior = (String) destination;
        log.debug("Creating a sender for: " + callback_ior);
        if(callback_ior.contains("IOR:")) {
            log.debug(callback_ior + " is for corba");
            //org.omg.CORBA.Object serviceFactoryObject = orbManagement.getOrb().string_to_object(callback_ior);
            //sender = new CorbaSenderImpl(serviceFactoryObject, callback_ior);         
            throw new ConnectionException(org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection.TPESYSTEM,
                    "Could not create a Corba sender");
        } else {
            log.debug(callback_ior + " is for socket");
            sender = new SocketSenderImpl(callback_ior);
        }
        log.debug("Created sender");
        return sender;
    }
    
    public Sender createSender(Receiver receiver) throws ConnectionException {
        return new SocketSenderImpl((Socket)receiver.getEndpoint(), (String)receiver.getReplyTo());
    }

    public Receiver getReceiver(String serviceName, boolean conversational) throws ConnectionException {
        if (closed) {
            log.error("Already closed");
            throw new ConnectionException(Connection.TPEPROTO, "Already closed");
        }
        log.debug("Creating a receiver: " + serviceName);
        Map<String, Receiver> conversationalMap = receivers.get(conversational);
        if (conversationalMap == null) {
            conversationalMap = new HashMap<String, Receiver>();
            receivers.put(conversational, conversationalMap);
        }

        Receiver toReturn = conversationalMap.get(serviceName);
        if (toReturn == null) {
            try {
                log.debug("Resolved destination");
                String type = (String) properties.get("blacktie." + serviceName + ".type");
                return new StompReceiverImpl(serviceName, conversational, type, properties);
            } catch (ConnectionException e) {
                throw e;
            } catch (Throwable t) {
                throw new ConnectionException(org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection.TPESYSTEM,
                        "Could not create the receiver on: " + serviceName, t);
            }
        }
        return toReturn;
    }

    public Receiver createReceiver(int cd, ResponseMonitor responseMonitor, EventListener eventListener) throws ConnectionException {
        if (closed) {
            log.error("Already closed");
            throw new ConnectionException(Connection.TPEPROTO, "Already closed");
        }
        log.debug("Creating a receiver");
        //return new CorbaReceiverImpl(orbManagement, properties, cd, responseMonitor);
        return new SocketReceiverImpl(socketserver, properties, cd, responseMonitor, eventListener);
    }

    public Receiver createReceiver(EventListener eventListener) throws ConnectionException {
        if (closed) {
            log.error("Already closed");
            throw new ConnectionException(Connection.TPEPROTO, "Already closed");
        }
        //log.debug("Creating a receiver with event listener");
        //return new CorbaReceiverImpl(eventListener, orbManagement, properties);
	return null;
    }
    
    public Receiver createReceiver(Sender sender) throws ConnectionException {
        if(sender == null) {
            log.debug("no need to create on empty sender");
            return null;
        } 
        return new SocketReceiverImpl((Socket)sender.getEndpoint(), (String)sender.getSendTo(), properties);
    }
}
