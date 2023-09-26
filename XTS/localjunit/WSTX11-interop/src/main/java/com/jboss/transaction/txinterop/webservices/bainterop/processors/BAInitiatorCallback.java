/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.txinterop.webservices.bainterop.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.Callback;
import org.jboss.ws.api.addressing.MAP;

/**
 * The callback for the initiator client.
 * @author kevin
 */
public abstract class BAInitiatorCallback extends Callback
{
    /**
     * A response.
     * @param map The current addressing context.
     */
    public abstract void response(final MAP map) ;

    /**
     * A SOAP fault response.
     * @param soapFault The SOAP fault response.
     * @param map The current addressing context.
     */
    public abstract void soapFault(final SoapFault soapFault, final MAP map) ;
}