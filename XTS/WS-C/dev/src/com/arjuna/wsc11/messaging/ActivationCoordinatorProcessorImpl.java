/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wsc11.messaging;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.processors.ActivationCoordinatorProcessor;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.wsc11.ContextFactory;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc11.ContextFactoryMapper;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.ws.soap.SOAPFaultException;
import jakarta.xml.ws.ProtocolException;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPFactory;

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
                    final Long expires = (expiresElement == null ? null : expiresElement.getValue()) ;
                    
                    coordinationContext = contextFactory.create(coordinationType, expires, createCoordinationContext.getCurrentContext(), isSecure) ;
                    final CreateCoordinationContextResponseType response = new CreateCoordinationContextResponseType() ;
                    response.setCoordinationContext(coordinationContext) ;
                    return response;
                }
                catch (final InvalidCreateParametersException invalidCreateParametersException)
                {
                    if (WSCLogger.logger.isTraceEnabled())
                        WSCLogger.logger.tracev(invalidCreateParametersException, "{0}, type {1} and context {2}",
                                WSCLogger.i18NLogger.get_wsc11_messaging_ActivationCoordinatorProcessorImpl_1(), coordinationType, contextFactory);

	                SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME);
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME).addTextNode(WSCLogger.i18NLogger.get_wsc11_messaging_ActivationCoordinatorProcessorImpl_1());
	                throw new SOAPFaultException(soapFault);
                }
                catch (final Throwable th)
                {
                    if (WSCLogger.logger.isTraceEnabled())
                        WSCLogger.logger.tracev(th, "Unexpected exception thrown from create coordination context: type {1} and context {2}",
                            coordinationType, contextFactory);

                    SOAPFactory factory = SOAPFactory.newInstance();
                    SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_CREATE_CONTEXT_QNAME) ;
                    soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_CANNOT_CREATE_CONTEXT_QNAME).addTextNode(th.getMessage());
                    throw new SOAPFaultException(soapFault);
                }
            }
            else
            {
                if (WSCLogger.logger.isTraceEnabled())
                    WSCLogger.logger.tracev("CreateCoordinationContext called for unknown coordination type: {0} [{1}]",
                        coordinationType, WSCLogger.i18NLogger.get_wsc11_messaging_ActivationCoordinatorProcessorImpl_1());

                SOAPFactory factory = SOAPFactory.newInstance();
                SOAPFault soapFault = factory.createFault(SoapFaultType.FAULT_SENDER.getValue(), CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME) ;
                soapFault.addDetail().addDetailEntry(CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME).addTextNode(WSCLogger.i18NLogger.get_wsc11_messaging_ActivationCoordinatorProcessorImpl_1());
                throw new SOAPFaultException(soapFault);
            }
        }
        catch (SOAPException se)
        {
            WSCLogger.i18NLogger.error_failure_to_create_coordination_context(
                createCoordinationContext == null ? null : createCoordinationContext.getCoordinationType(),se);
            throw new ProtocolException(se);
        }
    }
}