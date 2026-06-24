/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.jboss.transaction.txinterop.webservices.atinterop.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices11.wsaddr.processor.BaseWSAddrResponseProcessor;
import org.jboss.ws.api.addressing.MAP;

/**
 * The Initiator processor.
 * @author kevin
 */
public class ATInitiatorProcessor extends BaseWSAddrResponseProcessor
{
    /**
     * The initiator singleton.
     */
    private static final ATInitiatorProcessor INITIATOR = new ATInitiatorProcessor() ;
    
    /**
     * Get the initiator singleton.
     * @return The singleton.
     */
    public static ATInitiatorProcessor getInitiator()
    {
        return INITIATOR ;
    }

    /**
     * Handle a response response.
     * @param map The current addressing context.
     */
    public void handleResponse(final MAP map)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((ATInitiatorCallback)callback).response(map) ;
            }
        }, getIDs(map)) ;
    }

    /**
     * Register a SOAP fault response.
     * @param soapFault The SOAP fault response.
     * @param map The current addressing context.
     */
    public void handleSoapFault(final SoapFault soapFault, final MAP map)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((ATInitiatorCallback)callback).soapFault(soapFault, map) ;
            }
        }, getIDs(map)) ;
    }

    /**
     * Register a callback for the specific message id.
     * @param messageID The message ID.
     * @param callback The callback for the response.
     */
    public void registerCallback(final String messageID, final ATInitiatorCallback callback)
    {
        register(messageID, callback) ;
    }
}
