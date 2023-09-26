/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.wstf.webservices.sc007.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices11.wsaddr.processor.BaseWSAddrResponseProcessor;
import org.jboss.ws.api.addressing.MAP;

/**
 * The Initiator processor.
 * @author kevin
 */
public class InitiatorProcessor extends BaseWSAddrResponseProcessor
{
    /**
     * The initiator singleton.
     */
    private static final InitiatorProcessor INITIATOR = new InitiatorProcessor() ;
    
    /**
     * Get the initiator singleton.
     * @return The singleton.
     */
    public static InitiatorProcessor getInitiator()
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
                ((InitiatorCallback)callback).response(map) ;
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
                ((InitiatorCallback)callback).soapFault(soapFault, map) ;
            }
        }, getIDs(map)) ;
    }

    /**
     * Register a callback for the specific message id.
     * @param messageID The message ID.
     * @param callback The callback for the response.
     */
    public void registerCallback(final String messageID, final InitiatorCallback callback)
    {
        register(messageID, callback) ;
    }
}