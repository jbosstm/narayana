/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices.wscoor.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.processor.BaseWSAddrResponseProcessor;
import com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType;

/**
 * The Activation Requester processor.
 * @author kevin
 */
public class ActivationRequesterProcessor extends BaseWSAddrResponseProcessor
{
    /**
     * The requester singleton.
     */
    private static final ActivationRequesterProcessor REQUESTER = new ActivationRequesterProcessor() ;
    
    /**
     * Get the requester singleton.
     * @return The singleton.
     */
    public static ActivationRequesterProcessor getRequester()
    {
        return REQUESTER ;
    }

    /**
     * Handle a create coordination context response.
     * @param createCoordinationContextResponse The response.
     * @param addressingContext The current addressing context.
     */
    public void handleCreateCoordinationContextResponse(final CreateCoordinationContextResponseType createCoordinationContextResponse,
        final AddressingContext addressingContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((ActivationRequesterCallback)callback).createCoordinationContextResponse(createCoordinationContextResponse, addressingContext) ;
            }
        }, getIDs(addressingContext)) ;
    }

    /**
     * Register a SOAP fault response.
     * @param soapFault The SOAP fault response.
     * @param addressingContext The current addressing context.
     */
    public void handleSoapFault(final SoapFault soapFault, final AddressingContext addressingContext)
    {
        handleCallbacks(new CallbackExecutorAdapter() {
            public void execute(final Callback callback) {
                ((ActivationRequesterCallback)callback).soapFault(soapFault, addressingContext) ;
            }
        }, getIDs(addressingContext)) ;
    }

    /**
     * Register a callback for the specific message id.
     * @param messageID The message ID.
     * @param callback The callback for the response.
     */
    public void registerCallback(final String messageID, final ActivationRequesterCallback callback)
    {
        register(messageID, callback) ;
    }
}
