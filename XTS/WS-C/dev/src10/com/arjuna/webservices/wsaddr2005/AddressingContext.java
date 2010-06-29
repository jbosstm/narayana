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
package com.arjuna.webservices.wsaddr2005;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyContentSupport;
import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.util.StreamHelper;

/**
 * The complete addressing context.
 * @author kevin
 */
public class AddressingContext extends AnyContentSupport
{
    /**
     * The key used for the addressing context within a message exchange.
     */
    protected static final byte[] ADDRESSING_CONTEXT_PROPERTY = new byte[0] ;
    
    /**
     * The To header.
     */
    private AttributedURIType to ;
    /**
     * The Action header.
     */
    private AttributedURIType action ;
    /**
     * The MessageID header.
     */
    private AttributedURIType messageID ;
    /**
     * The From header.
     */
    private EndpointReferenceType from ;
    /**
     * The ReplyTo header.
     */
    private EndpointReferenceType replyTo ;
    /**
     * The FaultTo header.
     */
    private EndpointReferenceType faultTo ;
    /**
     * The RelatesTo headers.
     */
    private List relatesTo ;
    
    /**
     * The associated request addressing context.
     */
    private AddressingContext requestContext ;
    
    /**
     * Default constructor.
     */
    protected AddressingContext()
    {
    }
    
    /**
     * Construct an addressing context from the input stream.
     * N.B. This method is for testing purposes only.
     * 
     * @param in The input stream.
     * @throws XMLStreamException For errors during parsing.
     */
    public AddressingContext(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Get the to URI.
     * @return The to URI.
     */
    public AttributedURIType getTo()
    {
        return to ;
    }
    
    /**
     * Set the to URI.
     * @param to The to URI.
     */
    public void setTo(final AttributedURIType to)
    {
        this.to = to ;
    }
    
    /**
     * Get the action.
     * @return The action.
     */
    public AttributedURIType getAction()
    {
        return action ;
    }
    
    /**
     * Set the action.
     * @param action The action.
     */
    public void setAction(final AttributedURIType action)
    {
        this.action = action ;
    }
    
    /**
     * Get the messageID.
     * @return The messageID.
     */
    public AttributedURIType getMessageID()
    {
        return messageID ;
    }
    
    /**
     * Set the messageID.
     * @param messageID The messageID.
     */
    public void setMessageID(final AttributedURIType messageID)
    {
        this.messageID = messageID ;
    }
    
    /**
     * Get the from endpoint reference.
     * @return The from endpoint reference.
     */
    public EndpointReferenceType getFrom()
    {
        return from ;
    }
    
    /**
     * Set the from endpoint reference.
     * @param from The from endpoint reference.
     */
    public void setFrom(final EndpointReferenceType from)
    {
        this.from = from ;
    }
    
    /**
     * Get the reply endpoint reference.
     * @return The reply endpoint reference.
     */
    public EndpointReferenceType getReplyTo()
    {
        return replyTo ;
    }
    
    /**
     * Set the reply endpoint reference.
     * @param replyTo The reply endpoint reference.
     */
    public void setReplyTo(final EndpointReferenceType replyTo)
    {
        this.replyTo = replyTo ;
    }
    
    /**
     * Get the fault endpoint reference.
     * @return The fault endpoint reference.
     */
    public EndpointReferenceType getFaultTo()
    {
        return faultTo ;
    }
    
    /**
     * Set the fault endpoint reference.
     * @param faultTo The fault endpoint reference.
     */
    public void setFaultTo(final EndpointReferenceType faultTo)
    {
        this.faultTo = faultTo ;
    }
    
    /**
     * Get the list of relationships.
     * @return The list of relationships or null.
     */
    public RelatesToType[] getRelatesTo()
    {
        return (relatesTo == null ? null : (RelatesToType[]) relatesTo.toArray(new RelatesToType[relatesTo.size()])) ;
    }
    
    /**
     * Add a relationship to this addressing context.
     * @param relationship The relationship to add.
     */
    public void addRelatesTo(final RelatesToType relationship)
    {
        if (relatesTo == null)
        {
            relatesTo = new ArrayList() ;
        }
        relatesTo.add(relationship) ;
    }
    
    /**
     * Get the associated request context.
     * @return The associated request context or null if no association exists.
     */
    public AddressingContext getRequestContext()
    {
        return requestContext ;
    }
    
    /**
     * Set the associated request context.
     * @param requestContext The associated request context.
     */
    private void setRequestContext(final AddressingContext requestContext)
    {
        this.requestContext = requestContext ;
    }
    
    /**
     * Write the content of the context to the stream.
     * @param out The current output stream.
     * @message com.arjuna.webservices.wsaddr2005.AddressingContext_1 [com.arjuna.webservices.wsaddr2005.AddressingContext_1] - Addressing context is not valid
     */
    public void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        if ((to != null) && !AddressingConstants.WSA_ADDRESS_ANONYMOUS.equals(to.getValue()))
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_TO, to) ;
        }
        if (action != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_ACTION, action) ;
        }
        if (messageID != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_MESSAGE_ID, messageID) ;
        }
        if (from != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_FROM, from) ;
        }
        if (replyTo != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_REPLY_TO, replyTo) ;
        }
        if (faultTo != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_FAULT_TO, faultTo) ;
        }
        if (relatesTo != null)
        {
            final Iterator relatesToIter = relatesTo.iterator() ;
            while(relatesToIter.hasNext())
            {
                final RelatesToType relationship = (RelatesToType)relatesToIter.next() ;
                StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_RELATES_TO, relationship) ;
            }
        }
       
        super.writeChildContent(out) ;
    }

    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wsaddr2005.AddressingContext_2 [com.arjuna.webservices.wsaddr2005.AddressingContext_2] - Unexpected element name: {0}
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        if (AddressingConstants.WSA_NAMESPACE.equals(elementName.getNamespaceURI()))
        {
            final String localPart = elementName.getLocalPart() ;
            if (AddressingConstants.WSA_ELEMENT_TO.equals(localPart))
            {
                setTo(new AttributedURIType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_ACTION.equals(localPart))
            {
                setAction(new AttributedURIType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_MESSAGE_ID.equals(localPart))
            {
                setMessageID(new AttributedURIType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_FROM.equals(localPart))
            {
                setFrom(new EndpointReferenceType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_REPLY_TO.equals(localPart))
            {
                setReplyTo(new EndpointReferenceType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_FAULT_TO.equals(localPart))
            {
                setFaultTo(new EndpointReferenceType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_RELATES_TO.equals(localPart))
            {
                addRelatesTo(new RelatesToType(in)) ;
            }
            else
            {
                throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wsaddr2005_AddressingContext_2(elementName)) ;
            }
        }
        else
        {
            throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wsaddr2005_AddressingContext_2(elementName)) ;
        }
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((to == null) || to.isValid()) &&
            ((from == null) || from.isValid()) &&
            ((faultTo == null) || faultTo.isValid()) &&
            ((replyTo == null) || replyTo.isValid()) &&
            ((action != null) && action.isValid()) &&
            (((replyTo == null) && (faultTo == null)) || ((messageID != null) && messageID.isValid())) ;
    }
    
    /**
     * Update a SOAP fault with addressing headers.
     * @param soapFault The SOAP fault to update.
     */
    public void updateSoapFaultHeaders(final SoapFault soapFault)
    {
        final List soapFaultHeaders = new ArrayList() ;
        if (to != null)
        {
            soapFaultHeaders.add(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_TO, to)) ;
        }
        if (action != null)
        {
            soapFaultHeaders.add(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_ACTION, action)) ;
        }
        if (messageID != null)
        {
            soapFaultHeaders.add(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_MESSAGE_ID, messageID)) ;
        }
        if (from != null)
        {
            soapFaultHeaders.add(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_FROM, from)) ;
        }
        if (replyTo != null)
        {
            soapFaultHeaders.add(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_REPLY_TO, replyTo)) ;
        }
        if (faultTo != null)
        {
            soapFaultHeaders.add(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_FAULT_TO, faultTo)) ;
        }
        if (relatesTo != null)
        {
            final Iterator relatesToIter = relatesTo.iterator() ;
            while(relatesToIter.hasNext())
            {
                final RelatesToType relationship = (RelatesToType)relatesToIter.next() ;
                soapFaultHeaders.add(new NamedElement(AddressingConstants.WSA_ELEMENT_QNAME_RELATES_TO, relationship)) ;
            }
        }
        final NamedElement[] currentHeaders = ((SoapFault10)soapFault).getHeaderElements() ;
        final int numHeaderElements = (currentHeaders == null ? 0 : currentHeaders.length) ;
        for(int count = 0 ; count < numHeaderElements ; count++)
        {
            soapFaultHeaders.add(currentHeaders[count]) ;
        }
        ((SoapFault10)soapFault).setHeaderElements((NamedElement[]) soapFaultHeaders.toArray(new NamedElement[soapFaultHeaders.size()])) ;
    }

    /**
     * Get the addressing context from the message context if present.
     * @param messageContext The message context.
     * @return The addressing context or null if not present.
     */
    public static AddressingContext getContext(final MessageContext messageContext)
    {
        final AddressingContext addressingContext = (AddressingContext)messageContext.getProperty(ADDRESSING_CONTEXT_PROPERTY) ;
        if (addressingContext != null)
        {
            return addressingContext ;
        }
        final AddressingContext newAddressingContext = new AddressingContext() ;
        setContext(messageContext, newAddressingContext) ;
        return newAddressingContext ;
    }

    /**
     * Set the addressing context for the message context.
     * @param messageContext The message context.
     * @param addressingContext The addressing context.
     */
    public static void setContext(final MessageContext messageContext, final AddressingContext addressingContext)
    {
        messageContext.setProperty(ADDRESSING_CONTEXT_PROPERTY, addressingContext) ;
    }

    /**
     * Create an addressing context that represents a reply to the specified addressing context.
     * @param addressingContext The addressing context being replied to.
     * @param messageID The message id of the new message.
     * @return The reply addressing context.
     * 
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static AddressingContext createOneWayResponseContext(final AddressingContext addressingContext, final String messageID)
    {
        final AddressingContext responseContext = new AddressingContext() ;
        responseContext.setRequestContext(addressingContext) ;
        initialiseTo(responseContext, addressingContext) ;
        final AttributedURIType contextMessageID = addressingContext.getMessageID() ;
        if (contextMessageID != null)
        {
            responseContext.addRelatesTo(new RelatesToType(addressingContext.getMessageID().getValue())) ;
        }
        responseContext.setMessageID(new AttributedURIType(messageID)) ;
        
        return responseContext ;
    }

    /**
     * Create an addressing context that represents an inline reply to the specified addressing context.
     * @param addressingContext The addressing context being replied to.
     * @param messageID The message id of the new message.
     * @return The reply addressing context.
     */
    public static AddressingContext createResponseContext(final AddressingContext addressingContext, final String messageID)
    {
        final AddressingContext responseContext = new AddressingContext() ;
        responseContext.setRequestContext(addressingContext) ;
        initialiseTo(responseContext, addressingContext) ;
        final AttributedURIType contextMessageID = addressingContext.getMessageID() ;
        if (contextMessageID != null)
        {
            responseContext.addRelatesTo(new RelatesToType(contextMessageID.getValue())) ;
        }
        responseContext.setMessageID(new AttributedURIType(messageID)) ;
        
        return responseContext ;
    }

    /**
     * Create an addressing context that represents a fault to the specified addressing context.
     * @param addressingContext The addressing context being replied to.
     * @param messageID The message id of the new message.
     * @return The fault addressing context.
     * 
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static AddressingContext createFaultContext(final AddressingContext addressingContext, final String messageID)
    {
        final AddressingContext faultContext = new AddressingContext() ;
        faultContext.setRequestContext(addressingContext) ;
        final EndpointReferenceType faultTo = addressingContext.getFaultTo() ;
        if (faultTo != null)
        {
            initialiseTo(faultContext, faultTo) ;
        }
        else
        {
            initialiseTo(faultContext, addressingContext) ;
        }
        final AttributedURIType contextMessageID = addressingContext.getMessageID() ;
        if (contextMessageID != null)
        {
            faultContext.addRelatesTo(new RelatesToType(contextMessageID.getValue())) ;
        }
        faultContext.setMessageID(new AttributedURIType(messageID)) ;
        
        return faultContext ;
    }

    /**
     * Create an addressing context that represents a request to the specified endpoint reference.
     * @param endpointReference The target endpoint reference.
     * @param messageID The message id of the new message.
     * @return The addressing context.
     * 
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static AddressingContext createRequestContext(final EndpointReferenceType endpointReference, final String messageID)
    {
        final AddressingContext requestContext = new AddressingContext() ;
        initialiseTo(requestContext, endpointReference) ;
        if (messageID != null)
        {
            requestContext.setMessageID(new AttributedURIType(messageID)) ;
        }
        return requestContext ;
    }

    /**
     * Create an addressing context that represents a request to the specified address.
     * @param address The target address.
     * @param messageID The message id of the new message.
     * @return The addressing context.
     * 
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static AddressingContext createRequestContext(final String address, final String messageID)
    {
        final AddressingContext requestContext = new AddressingContext() ;
        requestContext.setTo(new AttributedURIType(address)) ;
        if (messageID != null)
        {
            requestContext.setMessageID(new AttributedURIType(messageID)) ;
        }
        return requestContext ;
    }

    /**
     * Create an addressing context that represents a notification to the specified context.
     * @param addressingContext The addressing context used to derive the notification addressing context.
     * @param messageID The message id of the new message.
     * @return The notification addressing context.
     * 
     * N.B. Still need to do From, Action, ReplyTo, FaultTo if needed.
     */
    public static AddressingContext createRequestContext(final AddressingContext addressingContext, final String messageID)
    {
        final AddressingContext requestContext = new AddressingContext() ;
        initialiseTo(requestContext, addressingContext) ;
        if (messageID != null)
        {
            requestContext.setMessageID(new AttributedURIType(messageID)) ;
        }
        
        return requestContext ;
    }
    
    /**
     * Initialise the To values from the specified addressing context.
     * @param newContext The addressing context to initialise.
     * @param origContext The addressing context to check.
     */
    private static void initialiseTo(final AddressingContext newContext, final AddressingContext origContext)
    {
        final EndpointReferenceType replyTo = origContext.getReplyTo() ;
        if (replyTo != null)
        {
            initialiseTo(newContext, replyTo) ;
        }
        else
        {
            newContext.setTo(new AttributedURIType(AddressingConstants.WSA_ADDRESS_ANONYMOUS)) ;
        }
    }
    
    /**
     * Initialise the To values from the specified endpoint reference.
     * @param addressingContext The addressing context to initialise.
     * @param endpointReference The endpoint reference
     */
    private static void initialiseTo(final AddressingContext addressingContext, final EndpointReferenceType endpointReference)
    {
        if (endpointReference != null)
        {
            addressingContext.setTo(endpointReference.getAddress()) ;
            final ReferenceParametersType referenceParametersType = endpointReference.getReferenceParameters() ;
            
            if (referenceParametersType != null)
            {
                final NamedElement[] anyContent = referenceParametersType.getAnyContent() ;
                final int numAnyContent = (anyContent == null ? 0 : anyContent.length) ;
                for(int count = 0 ; count < numAnyContent ; count++)
                {
                    final NamedElement namedElement = anyContent[count] ;
                    addressingContext.putAnyContent(new NamedElement(namedElement.getName(), new AddressingElementContent(namedElement.getElementContent()))) ;
                }
            }
        }
    }
}
