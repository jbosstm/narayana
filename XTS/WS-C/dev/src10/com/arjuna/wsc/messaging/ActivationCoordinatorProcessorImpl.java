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
package com.arjuna.wsc.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wscoor.AttributedUnsignedIntType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wscoor.CreateCoordinationContextType;
import com.arjuna.webservices.wscoor.client.ActivationRequesterClient;
import com.arjuna.webservices.wscoor.processors.ActivationCoordinatorProcessor;
import com.arjuna.wsc.ContextFactory;
import com.arjuna.wsc.ContextFactoryMapper;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc.messaging.MessageId;

/**
 * The Activation Coordinator processor.
 * @author kevin
 */
public class ActivationCoordinatorProcessorImpl extends ActivationCoordinatorProcessor
{
    /**
     * Create the coordination context.
     * @param createCoordinationContext The create coordination context request.
     * @param addressingContext The addressing context.
     * @message com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl_1 [com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl_1] - Invalid create coordination context parameters
     * @message com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl_2 [com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl_2] - Unexpected exception thrown from create: 
     * @message com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl_3 [com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl_3] - CreateCoordinationContext called for unknown coordination type: {0}
     */
    public void createCoordinationContext(final CreateCoordinationContextType createCoordinationContext,
        final AddressingContext addressingContext)
    {
        final ContextFactoryMapper contextFactoryMapper = ContextFactoryMapper.getFactory() ;
        try
        {
            final String coordinationType = createCoordinationContext.getCoordinationType().getValue() ;
            final ContextFactory contextFactory = contextFactoryMapper.getContextFactory(coordinationType) ;
            
            if (contextFactory != null)
            {
                final CoordinationContextType coordinationContext ;
                final AddressingContext responseAddressingContext ;
                try
                {
                    final AttributedUnsignedIntType expiresElement = createCoordinationContext.getExpires() ;
                    final Long expires = (expiresElement == null ? null : new Long(expiresElement.getValue())) ;
                    
                    coordinationContext = contextFactory.create(coordinationType, expires, createCoordinationContext.getCurrentContext()) ;
                    responseAddressingContext = AddressingContext.createResponseContext(addressingContext, MessageId.getMessageId()) ;
                }
                catch (final InvalidCreateParametersException invalidCreateParametersException)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME,
                        WSCLogger.arjLoggerI18N.getString("com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl_1")) ;
                    ActivationRequesterClient.getClient().sendSoapFault(faultAddressingContext, soapFault) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSCLogger.arjLoggerI18N.isDebugEnabled())
                    {
                        WSCLogger.arjLoggerI18N.debug("com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl_2", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault10(th) ;
                    ActivationRequesterClient.getClient().sendSoapFault(faultAddressingContext, soapFault) ;
                    return ;
                }
                ActivationRequesterClient.getClient().sendCreateCoordinationResponse(responseAddressingContext, coordinationContext) ;
            }
            else
            {
                if (WSCLogger.arjLoggerI18N.isDebugEnabled())
                {
                    WSCLogger.arjLoggerI18N.debug("com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl_3", new Object[] {coordinationType}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME,
                    WSCLogger.arjLoggerI18N.getString("com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl_1")) ;
                ActivationRequesterClient.getClient().sendSoapFault(faultAddressingContext, soapFault) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
}
