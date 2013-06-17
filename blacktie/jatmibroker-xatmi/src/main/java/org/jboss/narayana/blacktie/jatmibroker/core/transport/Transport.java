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

import org.jboss.narayana.blacktie.jatmibroker.core.ResponseMonitor;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;

public interface Transport {

    /**
     * Get the sender
     * 
     * @param serviceName
     * @return
     * @throws ConfigurationException
     */
    public Sender getSender(String serviceName, boolean conversational) throws ConnectionException;

    /**
     * Create a sender to a service queue
     * 
     * @param replyTo
     * @return
     * @throws ConfigurationException
     */
    public Sender createSender(Object replyTo) throws ConnectionException;
    
    public Sender createSender(Receiver receiver) throws ConnectionException;

    /**
     * Create a receiver on a service queue
     * 
     * @param serviceName
     * @return
     * @throws ConfigurationException
     */
    public Receiver getReceiver(String serviceName, boolean conversational) throws ConnectionException;

    /**
     * Create a receiver on a temporary queue.
     * 
     * @return The receiver
     * @throws ConfigurationException
     */
    public Receiver createReceiver(int cd, ResponseMonitor responseMonitor, EventListener eventListener) throws ConnectionException;

    /**
     * Create a receiver giving it the session to receive events upon.
     * 
     * @param session The session to receive events
     * @return The receiver
     * @throws ConnectionException Incase the received cannot be created
     */
    public Receiver createReceiver(EventListener session) throws ConnectionException;
    
    /**
     * Create a receiver on a socket sender
     * @throws ConnectionException
     */
    public Receiver createReceiver(Sender sender) throws ConnectionException ;

    public void close() throws ConnectionException;

}
