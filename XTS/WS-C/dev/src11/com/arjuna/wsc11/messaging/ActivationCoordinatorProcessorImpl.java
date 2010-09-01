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
package com.arjuna.wsc11.messaging;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.processors.ActivationCoordinatorProcessor;
import org.jboss.wsf.common.addressing.MAP;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.wsc11.ContextFactory;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc11.ContextFactoryMapper;

import javax.xml.soap.SOAPException;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.ProtocolException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPFactory;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.*;

/**
 * The Activation Coordinator processor.
 * @author kevin
 */
public class ActivationCoordinatorProcessorImpl extends ActivationCoordinatorProcessor
{
    /**
     * Create the coordination context.
     * @param createCoordinationContext The create coordination context request.
     * @param map The addressing context.
     */
    public CreateCoordinationContextResponseType
        createCoordinationContext(final CreateCoordinationContextType createCoordinationContext,
                                  final MAP map,
                                  final boolean isSecure)
    {
        final ContextFactoryMapper contextFactoryMapper = ContextFactoryMapper.getMapper() ;
        try
        {
            final String coordinationType = createCoordinationContext.getCoordinationType() ;
            final ContextFactory contextFactory = contextFactoryMapper.getContextFactory(coordinationType) ;
            
            if (contextFactory != null)
            {
                final CoordinationContext coordinationContext ;
                try
                {
                    final Expires expiresElement = createCoordinationContext.getExpires() ;
                    final Long expires = (expiresElement == null ? null : new Long(expiresElement.getValue())) ;
                    
                    coordinationContext = contextFactory.create(coordinationType, expires, createCoordinationContext.getCurrentContext(), isSecure) ;
                    final CreateCoordinationContextResponseType response = new CreateCoordinationContextResponseType() ;
                    response.setCoordinationContext(coordinationContext) ;
                    return response;
                }
                catch (final InvalidCreateParametersException invalidCreateParametersException)
                {
	                SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME).addTextNode(WSCLogger.i18NLogger.get_wsc11_messaging_ActivationCoordinatorProcessorImpl_1());
	                throw new SOAPFaultException(soapFault);
                }
                catch (final Throwable th)
                {
                    if (WSCLogger.logger.isTraceEnabled())
                    {
                        WSCLogger.logger.tracev("Unexpected exception thrown from create:", th) ;
                    }
                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_CREATE_CONTEXT_QNAME) ;
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_CREATE_CONTEXT_QNAME).addTextNode(th.getMessage());
                    throw new SOAPFaultException(soapFault);
                }
            }
            else
            {
                if (WSCLogger.logger.isTraceEnabled())
                {
                    WSCLogger.logger.tracev("CreateCoordinationContext called for unknown coordination type: {0}", new Object[] {coordinationType}) ;
                }

                SOAPFactory factory = SOAPFactory.newInstance();
                SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME) ;
                soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME).addTextNode(WSCLogger.i18NLogger.get_wsc11_messaging_ActivationCoordinatorProcessorImpl_1());
                throw new SOAPFaultException(soapFault);
            }
        }
        catch (SOAPException se)
        {
            se.printStackTrace(System.err);
            throw new ProtocolException(se);
        }
    }
}
