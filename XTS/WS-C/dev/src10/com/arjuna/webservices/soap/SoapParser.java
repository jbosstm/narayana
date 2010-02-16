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
package com.arjuna.webservices.soap;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.arjuna.webservices.BodyHandler;
import com.arjuna.webservices.HeaderHandler;
import com.arjuna.webservices.InterceptorChain;
import com.arjuna.webservices.InterceptorHandler;
import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapBody;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.ElementConsumer;
import com.arjuna.webservices.util.StreamHelper;

/**
 * Utility class for SOAP parsing.
 * @author kevin
 */
public class SoapParser
{
    /**
     * Handle the processing of the SOAP request.
     * @param messageContext The current message context.
     * @param messageResponseContext The current message response context.
     * @param action The transport SOAP action.
     * @param streamReader The XML stream reader.
     * @param soapService The SOAP service.
     * @param soapDetails The SOAP details.
     * @return The SOAP response.
     * @throws XMLStreamException For XML parsing errors.
     * @throws SoapFault For SOAP processing errors.
     * 
     * @message com.arjuna.webservices.soap.SoapParser_1 [com.arjuna.webservices.soap.SoapParser_1] - Unexpected element: {0}
     * @message com.arjuna.webservices.soap.SoapParser_2 [com.arjuna.webservices.soap.SoapParser_2] - Unexpected body element: {0}
     */
    public static SoapBody parse(final MessageContext messageContext, final MessageContext messageResponseContext,
        final String action, final XMLStreamReader streamReader, final SoapService soapService,
        final SoapDetails soapDetails)
        throws XMLStreamException, SoapFault
    {
        StreamHelper.skipToStartElement(streamReader) ;
        if (!(SoapConstants.SOAP_ENVELOPE_NAME.equals(streamReader.getLocalName()) &&
            soapDetails.getNamespaceURI().equals(streamReader.getNamespaceURI())))
        {
            throw new SoapFault10(SoapFaultType.FAULT_VERSION_MISMATCH,
                streamReader.getName().toString()) ;
        }
        
        StreamHelper.skipToNextStartElement(streamReader) ;
        
        if (SoapConstants.SOAP_HEADER_NAME.equals(streamReader.getLocalName()) &&
            soapDetails.getNamespaceURI().equals(streamReader.getNamespaceURI()))
        {
            handleHeaders(messageContext, streamReader, soapService, soapDetails) ;
            streamReader.require(XMLStreamConstants.END_ELEMENT,
                soapDetails.getNamespaceURI(), SoapConstants.SOAP_HEADER_NAME) ;
            StreamHelper.skipToNextStartElement(streamReader) ;
        }
        
        if (!(SoapConstants.SOAP_BODY_NAME.equals(streamReader.getLocalName()) &&
            soapDetails.getNamespaceURI().equals(streamReader.getNamespaceURI())))
        {
            final String pattern = WSCLogger.arjLoggerI18N.getString("com.arjuna.webservices.soap.SoapParser_1") ;
            final String message = MessageFormat.format(pattern, new Object[] {streamReader.getName()}) ;
            throw new SoapFault10(SoapFaultType.FAULT_SENDER, message) ;
        }
        
        StreamHelper.skipToNextStartElement(streamReader) ;
        final QName bodyName = streamReader.getName() ;
        final BodyHandler bodyHandler ;
        final BodyHandler namedBodyHandler = soapService.getBodyHandler(bodyName) ;
        if (namedBodyHandler == null)
        {
            if (!bodyName.equals(soapDetails.getFaultName()))
            {
                final String pattern = WSCLogger.arjLoggerI18N.getString("com.arjuna.webservices.soap.SoapParser_2") ;
                final String message = MessageFormat.format(pattern, new Object[] {streamReader.getName()}) ;
                throw new SoapFault10(SoapFaultType.FAULT_SENDER, message) ;
            }
            bodyHandler = soapService.getFaultHandler() ;
        }
        else
        {
            bodyHandler = namedBodyHandler ;
        }
        
        final Set interceptorHandlers = soapService.getInterceptorHandlers() ;
        try
        {
            if (interceptorHandlers.size() > 0)
            {
                final Iterator interceptorHandlerIter = interceptorHandlers.iterator() ;
                InterceptorChain interceptorChain = new InterceptorBodyHandler(bodyHandler) ;
                do
                {
                    final InterceptorHandler handler = (InterceptorHandler)interceptorHandlerIter.next();
                    interceptorChain = new InterceptorChainHandler(interceptorChain, handler) ;
                }
                while (interceptorHandlerIter.hasNext()) ;
                
                return interceptorChain.invokeNext(soapService, soapDetails, messageContext, messageResponseContext, action, streamReader) ;
            }
            else
            {
                return bodyHandler.invoke(soapDetails, messageContext, messageResponseContext, action, streamReader) ;
            }
        }
        catch (final SoapFault sf)
        {
            throw sf ;
        }
        catch (final Throwable th)
        {
            throw new SoapFault10(th) ;
        }
    }
    
    /**
     * Handle the processing of the headers.
     * @param messageContext The current message context.
     * @param streamReader The XML stream reader.
     * @param soapService The SOAP service.
     * @param soapDetails The SOAP details.
     * @throws XMLStreamException For XML parsing errors.
     * @throws SoapFault For SOAP processing errors. 
     */
    private static void handleHeaders(final MessageContext messageContext, final XMLStreamReader streamReader,
            final SoapService soapService, final SoapDetails soapDetails)
        throws XMLStreamException, SoapFault
    {
        boolean finished = false ;
        final LinkedList handlers = new LinkedList() ;
        try
        {
            do
            {
                streamReader.nextTag() ;
                if (streamReader.isEndElement())
                {
                    finished = true ;
                }
                else
                {
                    final String role = streamReader.getAttributeValue(soapDetails.getNamespaceURI(), soapDetails.getRoleLocalName()) ;
                    if ((role == null) || role.equals(soapDetails.getLastRole()) ||
                        role.equals(soapDetails.getNextRole()))
                    {
                        final QName headerName = streamReader.getName() ;
                        final HeaderHandler headerHandler = soapService.getHeaderHandler(headerName) ;
                        if (headerHandler == null)
                        {
                            checkMustUnderstand(streamReader, soapDetails) ;
                            ElementConsumer.consume(streamReader) ;
                        }
                        else
                        {
                            headerHandler.invoke(streamReader, messageContext) ;
                            handlers.add(headerHandler) ;
                        }
                        streamReader.require(XMLStreamConstants.END_ELEMENT,
                                headerName.getNamespaceURI(), headerName.getLocalPart()) ;
                    }
                    else
                    {
                        ElementConsumer.consume(streamReader) ;
                    }
                }
            }
            while(!finished) ;
            
            final Map headerHandlers = soapService.getHeaderHandlers() ;
            final Iterator headerHandlerIter = headerHandlers.values().iterator() ;
            while(headerHandlerIter.hasNext())
            {
                final HeaderHandler handler = (HeaderHandler)headerHandlerIter.next() ;
                handler.headerValidate(messageContext) ;
            }
        }
        catch (final Throwable th)
        {
            final int numHandlers = handlers.size() ;
            if (numHandlers > 0)
            {
                final ListIterator listIterator = handlers.listIterator(numHandlers) ;
                do
                {
                    final HeaderHandler headerHandler = (HeaderHandler)listIterator.previous() ;
                    headerHandler.headerFaultNotification(messageContext) ;
                }
                while(listIterator.hasPrevious()) ;
            }
            
            if (th instanceof XMLStreamException)
            {
                throw (XMLStreamException)th ;
            }
            else if (th instanceof SoapFault)
            {
                final SoapFault soapFault = (SoapFault)th ;
                soapFault.setHeaderFault(true) ;
                throw soapFault ;
            }
            else if (th instanceof RuntimeException)
            {
                throw (RuntimeException)th ;
            }
            else
            {
                throw (Error)th ;
            }
        }
    }
    
    /**
     * Check the existence of the must understand parameter.
     * @param streamReader
     * @param soapDetails
     * @throws XMLStreamException
     * @throws SoapFault
     * 
     * @message com.arjuna.webservices.soap.SoapParser_3 [com.arjuna.webservices.soap.SoapParser_3] - Did not understand header: {0}
     */
    private static void checkMustUnderstand(final XMLStreamReader streamReader, final SoapDetails soapDetails)
        throws XMLStreamException, SoapFault
    {
        final String mustUnderstand = streamReader.getAttributeValue(soapDetails.getNamespaceURI(),
            SoapConstants.SOAP_MUST_UNDERSTAND_NAME) ;
        if ((mustUnderstand != null) && ("1".equals(mustUnderstand) || "true".equalsIgnoreCase(mustUnderstand)))
        {
            final QName headerName = streamReader.getName() ;
            final String pattern = WSCLogger.arjLoggerI18N.getString("com.arjuna.webservices.soap.SoapParser_3") ;
            final String message = MessageFormat.format(pattern, new Object[] {streamReader.getName()}) ;
            final SoapFault10 soapFault = new SoapFault10(SoapFaultType.FAULT_MUST_UNDERSTAND, message) ;
            soapFault.setHeaderElements(soapDetails.getMustUnderstandHeaders(headerName)) ;
            throw soapFault ;
        }
    }
    
    /**
     * The interceptor chain for processing the terminal body handler.
     * @author kevin
     */
    private static class InterceptorBodyHandler implements InterceptorChain
    {
        /**
         * The body handler.
         */
        private final BodyHandler bodyHandler ;
        
        /**
         * Construct the interceptor body handler.
         * @param bodyHandler The body handler.
         */
        InterceptorBodyHandler(final BodyHandler bodyHandler)
        {
            this.bodyHandler = bodyHandler ;
        }
        
        /**
         * Invoke the next interceptor in the chain.
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
        public SoapBody invokeNext(final SoapService soapService,
            final SoapDetails soapDetails, final MessageContext context,
            final MessageContext responseContext, final String action,
            final XMLStreamReader in)
            throws XMLStreamException, SoapFault
        {
            return bodyHandler.invoke(soapDetails, context, responseContext, action, in) ;
        }
    }
    
    /**
     * The interceptor chain for processing interceptor handlers.
     * @author kevin
     */
    private static class InterceptorChainHandler implements InterceptorChain
    {
        /**
         * The interceptor chain handler.
         */
        private final InterceptorChain interceptorChain ;
        /**
         * The interceptor handler.
         */
        private final InterceptorHandler interceptorHandler ;
        
        /**
         * Construct the interceptor chain handler.
         * @param interceptorChain The interceptor chain handler.
         * @param interceptorHandler The interceptor handler.
         */
        InterceptorChainHandler(final InterceptorChain interceptorChain, final InterceptorHandler interceptorHandler)
        {
            this.interceptorChain = interceptorChain ;
            this.interceptorHandler = interceptorHandler ;
        }
        
        /**
         * Invoke the next interceptor in the chain.
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
        public SoapBody invokeNext(final SoapService soapService,
            final SoapDetails soapDetails, final MessageContext context,
            final MessageContext responseContext, final String action,
            final XMLStreamReader in)
            throws XMLStreamException, SoapFault
        {
            return interceptorHandler.invoke(interceptorChain, soapService, soapDetails, context, responseContext, action, in) ;
        }
    }
}
