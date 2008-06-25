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
package com.arjuna.webservices.wsba.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.arjuna.services.framework.task.Task;
import com.arjuna.services.framework.task.TaskManager;
import com.arjuna.webservices.BodyHandler;
import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapBody;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.soap.SoapDetails;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorProcessor;

/**
 * The coordinator completion coordinator cancelled handler.
 * @author kevin
 */
public class CoordinatorCompletionCoordinatorCancelledHandler implements BodyHandler
{
    /**
     * Handle the body element.
     * @param soapDetails The SOAP details.
     * @param context The current message context.
     * @param responseContext The response message context.
     * @param action The transport SOAP action. 
     * @param in The current stream reader.
     * @throws XMLStreamException for parsing errors.
     * @throws SoapFault for processing errors.
     * @return The response elements or null if one way.
     */
    public SoapBody invoke(final SoapDetails soapDetails, final MessageContext context,
        final MessageContext responseContext, final String action, final XMLStreamReader in)
        throws XMLStreamException, SoapFault
    {
        final NotificationType cancelled = new NotificationType(in) ;
        final AddressingContext addressingContext = AddressingContext.getContext(context) ;
        final ArjunaContext arjunaContext = ArjunaContext.getContext(context) ;
        TaskManager.getManager().queueTask(new Task() {
            public void executeTask() {
                CoordinatorCompletionCoordinatorProcessor.getProcessor().cancelled(cancelled, addressingContext, arjunaContext) ;
            }
        }) ;
        return null ;
    }
}
