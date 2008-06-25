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
import com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType;

/**
 * The callback for the Activation Requester client.
 * @author kevin
 */
public abstract class ActivationRequesterCallback extends Callback
{
    /**
     * A create coordination context response.
     * @param createCoordinationContextResponse The response.
     * @param addressingContext The current addressing context.
     */
    public abstract void createCoordinationContextResponse(final CreateCoordinationContextResponseType createCoordinationContextResponse,
        final AddressingContext addressingContext) ;

    /**
     * A SOAP fault response.
     * @param soapFault The SOAP fault response.
     * @param addressingContext The current addressing context.
     */
    public abstract void soapFault(final SoapFault soapFault, final AddressingContext addressingContext) ;
}
