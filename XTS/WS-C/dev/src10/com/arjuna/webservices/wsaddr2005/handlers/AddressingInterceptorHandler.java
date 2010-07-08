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
package com.arjuna.webservices.wsaddr2005.handlers;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.arjuna.services.framework.task.Task;
import com.arjuna.services.framework.task.TaskManager;
import com.arjuna.webservices.InterceptorChain;
import com.arjuna.webservices.InterceptorHandler;
import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapBody;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.soap.SoapDetails;
import com.arjuna.webservices.stax.AnyElement;
import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.wsaddr2005.AddressingConstants;
import com.arjuna.webservices.wsaddr2005.AddressingContext;
import com.arjuna.webservices.wsaddr2005.AttributedQNameType;
import com.arjuna.webservices.wsaddr2005.AttributedURIType;
import com.arjuna.webservices.wsaddr2005.EndpointReferenceType;
import com.arjuna.webservices.wsaddr2005.client.WSAddr2005Client;
import com.arjuna.wsc.messaging.MessageId;

/**
 * The interceptor handler responsible for handling faults.
 * @author kevin
 * 
 */
public class AddressingInterceptorHandler implements InterceptorHandler
{
    /**
     * Invoke the interceptor.
     * @param chain The interceptor chain.
     * @param soapService The SOAP service being called.
     * @param soapDetails The SOAP details.
     * @param context The current message context.
     * @param responseContext The response message context.
     * @param action The transport SOAP action.
     * @param in The current stream reader.
     * @throws XMLStreamException for parsing errors.
     * @throws SoapFault for processing errors.
     * @return The response elements or null if one way.
     */
    public SoapBody invoke(final InterceptorChain chain, final SoapService soapService,
        final SoapDetails soapDetails, final MessageContext context, final MessageContext responseContext,
        final String action, final XMLStreamReader in)
        throws XMLStreamException, SoapFault
    {
        final AddressingContext addressingContext = AddressingContext.getContext(context) ;
        final EndpointReferenceType replyTo = addressingContext.getReplyTo() ;
        if (replyTo != null)
        {
            if (!replyTo.isValid())
            {
                if (WSCLogger.logger.isTraceEnabled())
                {
                    WSCLogger.logger.tracev("Ignoring invalid WS-Addressing replyTo endpoint reference.") ;
                }
            }
            else if (!AddressingConstants.WSA_ADDRESS_ANONYMOUS.equals(replyTo.getAddress().getValue()))
            {
                final EndpointReferenceType faultTo = addressingContext.getFaultTo() ;
                if (faultTo != null)
                {
                    if (!faultTo.isValid())
                    {
                        if (WSCLogger.logger.isTraceEnabled())
                        {
                            WSCLogger.logger.tracev("Ignoring invalid WS-Addressing faultTo endpoint reference.") ;
                        }
                    }
                    else if (AddressingConstants.WSA_ADDRESS_ANONYMOUS.equals(faultTo.getAddress().getValue()))
                    {
                        return processInvocation(chain, soapService, soapDetails, context, responseContext, action, in) ;
                    }
                }
                TaskManager.getManager().queueTask(new Task() {
                    public void executeTask()
                    {
                        try
                        {
                            processInvocation(chain, soapService, soapDetails, context, responseContext, action, in) ;
                        }
                        catch (final SoapFault soapFault)
                        {
                            WSCLogger.i18NLogger.error_webservices_wsaddr2005_handlers_AddressingInterceptorHandler_3(soapFault.toString());
                        }
                    }
                }) ;
                return null ;
            }
        }
        return processInvocation(chain, soapService, soapDetails, context, responseContext, action, in) ;
    }
    
    /**
     * Process the invocation.
     * @param chain The interceptor chain.
     * @param soapService The SOAP service being called.
     * @param soapDetails The SOAP details.
     * @param context The current message context.
     * @param responseContext The response message context.
     * @param action The transport SOAP action.
     * @param in The current stream reader.
     * @throws XMLStreamException for parsing errors.
     * @throws SoapFault for processing errors.
     * @return The response elements or null if one way.
     */
    private SoapBody processInvocation(final InterceptorChain chain, final SoapService soapService,
            final SoapDetails soapDetails, final MessageContext context, final MessageContext responseContext,
            final String action, final XMLStreamReader in)
            throws SoapFault
    {
        final AddressingContext addressingContext = AddressingContext.getContext(context) ;
        try
        {
            final SoapBody response = invokeNext(chain, soapService, soapDetails, context, responseContext, action, in) ;
            if (response != null)
            {
                final AddressingContext responseAddressingContext = AddressingContext.createResponseContext(addressingContext, MessageId.getMessageId()) ;
                final String soapBodyAction = response.getAction() ;
                if (soapBodyAction != null)
                {
                    responseAddressingContext.setAction(new AttributedURIType(soapBodyAction)) ;
                }
                AddressingContext.setContext(responseContext, responseAddressingContext) ;
                
                final AttributedURIType to = responseAddressingContext.getTo() ;
                if (to != null)
                {
                    if (!to.isValid())
                    {
                        if (WSCLogger.logger.isTraceEnabled())
                        {
                            WSCLogger.logger.tracev("Ignoring invalid WS-Addressing replyTo endpoint reference.") ;
                        }
                    }
                    else
                    {
                        final String toURI = to.getValue() ;
                        if (!AddressingConstants.WSA_ADDRESS_ANONYMOUS.equals(toURI))
                        {
                            if (!AddressingConstants.WSA_ADDRESS_NONE.equals(toURI))
                            {
                                try
                                {
                                    WSAddr2005Client.sendOneWay(response, responseAddressingContext, soapDetails, soapService) ;
                                }
                                catch (final IOException ioe)
                                {
                                    // Something happened while sending an async response so null out the replyTo and throw a fault.
                                    addressingContext.setReplyTo(null) ;
                                    
                                    final AnyElement detail = new AnyElement() ;
                                    detail.putAnyContent(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_PROBLEM_IRI, responseAddressingContext.getTo())) ;
                                    final NamedElement detailElement = new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_FAULT_DETAIL, detail) ;
                                    final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, AddressingConstants.WSA_FAULT_CODE_QNAME_DESTINATION_UNREACHABLE,
                                        "Destination unreachable", detailElement) ;
                                    soapFault.setAction(AddressingConstants.WSA_ACTION_FAULT) ;
                                    throw soapFault ;
                                }
                            }
                            return null ;
                        }
                    }
                }
            }
            
            return response ;
        }
        catch (final SoapFault soapFault)
        {
            final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
            
            final String soapFaultAction = soapFault.getAction() ;
            final String faultAction = (soapFaultAction != null ? soapFaultAction : AddressingConstants.WSA_ACTION_SOAP_FAULT) ;
            
            faultAddressingContext.setAction(new AttributedURIType(faultAction)) ;
            
            final AttributedURIType to = faultAddressingContext.getTo() ;
            if (to != null)
            {
                if (!to.isValid())
                {
                    if (WSCLogger.logger.isTraceEnabled())
                    {
                        WSCLogger.logger.tracev("Ignoring invalid WS-Addressing faultTo endpoint reference.") ;
                    }
                }
                else
                {
                    final String toURI = to.getValue() ;
                    if (!AddressingConstants.WSA_ADDRESS_ANONYMOUS.equals(toURI))
                    {
                        if (!AddressingConstants.WSA_ADDRESS_NONE.equals(toURI))
                        {
                            try
                            {
                                WSAddr2005Client.sendSoapFault(soapFault, faultAddressingContext,
                                        soapDetails, soapService) ;
                            }
                            catch (final IOException ioe)
                            {
                                throw new SoapFault10(ioe) ;
                            }
                        }
                        return null ;
                    }
                }
            }
            
            if (soapFault.isHeaderFault())
            {
                faultAddressingContext.updateSoapFaultHeaders(soapFault) ;
            }
            else
            {
                AddressingContext.setContext(responseContext, faultAddressingContext) ;
            }
            
            throw soapFault ;
        }
    }
    
    /**
     * Handle the processing of the next interceptor.
     * @param chain The interceptor chain.
     * @param soapService The SOAP service.
     * @param soapDetails The SOAP details.
     * @param context The current context.
     * @param responseContext The response context.
     * @param action The current action.
     * @param in The XML stream reader.
     * @return The response.
     * @throws SoapFault For errors during processing.
     */
    private SoapBody invokeNext(final InterceptorChain chain, final SoapService soapService,
        final SoapDetails soapDetails, final MessageContext context, final MessageContext responseContext,
        final String action, final XMLStreamReader in)
        throws SoapFault
    {
        final HandlerAddressingContext addressingContext = HandlerAddressingContext.getHandlerContext(context) ;
        final QName faultHeaderName = addressingContext.getFaultHeaderName() ;
        if (faultHeaderName != null)
        {
            final ElementContent faultHeader = addressingContext.getFaultHeader() ;
            throw initialiseDuplicateHeaderSoapFault(context, faultHeaderName, faultHeader) ;
        }
        
        if (addressingContext.getAction() == null)
        {
            throw initialiseMissingActionSoapFault(context) ;
        }
        
        try
        {
            return chain.invokeNext(soapService, soapDetails, context, responseContext, action, in) ;
        }
        catch (final SoapFault soapFault)
        {
            throw soapFault ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault10(th) ;
        }
    }
    
    /**
     * Generate a SOAP fault for a duplicate addressing header.
     * @param context The message context.
     * @return The SOAP fault.
     */
    protected SoapFault initialiseMissingActionSoapFault(final MessageContext context)
    {
        final AnyElement detail = new AnyElement() ;
        detail.putAnyContent(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_PROBLEM_HEADER_QNAME, new AttributedQNameType(AddressingConstants.WSA_ELEMENT_QNAME_ACTION))) ;
        
        final NamedElement detailElement = new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_FAULT_DETAIL, detail) ;
        final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, AddressingConstants.WSA_FAULT_CODE_QNAME_MESSAGING_ADDRESSING_HEADER_REQUIRED,
            "Missing Addressing Header", detailElement) ;
        soapFault.setAction(AddressingConstants.WSA_ACTION_FAULT) ;
        soapFault.setHeaderFault(true) ;
        return soapFault ;
    }
    
    /**
     * Generate a SOAP fault for a duplicate addressing header.
     * @param context The message context.
     * @param headerName The qualified name of the header.
     * @param header The contents of the header.
     * @return The SOAP fault.
     */
    protected SoapFault initialiseDuplicateHeaderSoapFault(final MessageContext context, final QName headerName, final ElementContent header)
    {
        final AnyElement problemHeader = new AnyElement() ;
        problemHeader.putAnyContent(new NamedElement(headerName, header)) ;

        final AnyElement detail = new AnyElement() ;
        detail.putAnyContent(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_PROBLEM_HEADER_QNAME, new AttributedQNameType(headerName))) ;
        detail.putAnyContent(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_PROBLEM_HEADER, problemHeader)) ;
        final NamedElement detailElement = new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_FAULT_DETAIL, detail) ;
        final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, AddressingConstants.WSA_FAULT_CODE_QNAME_INVALID_ADDRESSING_HEADER,
            "Duplicate Addressing Header", detailElement) ;
        soapFault.setSubSubcode(AddressingConstants.WSA_FAULT_CODE_QNAME_INVALID_CARDINALITY) ;
        soapFault.setAction(AddressingConstants.WSA_ACTION_FAULT) ;
        soapFault.setHeaderFault(true) ;
        return soapFault ;
    }
}
