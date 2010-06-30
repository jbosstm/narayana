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
package com.arjuna.webservices.wsaddr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyContentSupport;
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
    private static final byte[] ADDRESSING_CONTEXT_PROPERTY = new byte[0] ;
    
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
     * Default constructor.
     */
    private AddressingContext()
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
    public RelationshipType[] getRelatesTo()
    {
        return (relatesTo == null ? null : (RelationshipType[]) relatesTo.toArray(new RelationshipType[relatesTo.size()])) ;
    }
    
    /**
     * Add a relationship to this addressing context.
     * @param relationship The relationship to add.
     */
    public void addRelatesTo(final RelationshipType relationship)
    {
        if (relatesTo == null)
        {
            relatesTo = new ArrayList() ;
        }
        relatesTo.add(relationship) ;
    }
    
    /**
     * Write the content of the context to the stream.
     * @param out The current output stream.
     */
    public void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
       if (!isValid())
       {
           throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wsaddr_AddressingContext_1()) ;
       }
       
       StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_TO_QNAME, to) ;
       StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_ACTION_QNAME, action) ;
       if (messageID != null)
       {
           StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_MESSAGE_ID_QNAME, messageID) ;
       }
       if (from != null)
       {
           StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_FROM_QNAME, from) ;
       }
       if (replyTo != null)
       {
           StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_REPLY_TO_QNAME, replyTo) ;
       }
       if (faultTo != null)
       {
           StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_FAULT_TO_QNAME, faultTo) ;
       }
       if (relatesTo != null)
       {
           final Iterator relatesToIter = relatesTo.iterator() ;
           while(relatesToIter.hasNext())
           {
               final RelationshipType relationship = (RelationshipType)relatesToIter.next() ;
               StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_RELATES_TO_QNAME, relationship) ;
           }
       }
       
       super.writeChildContent(out) ;
    }

    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
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
                addRelatesTo(new RelationshipType(in)) ;
            }
            else
            {
                throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wsaddr_AddressingContext_2(elementName)) ;
            }
        }
        else
        {
            throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wsaddr_AddressingContext_2(elementName)) ;
        }
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((to != null) && to.isValid()) &&
            ((action != null) && action.isValid()) &&
            ((from == null) || from.isValid()) &&
            ((faultTo == null) || faultTo.isValid()) &&
            ((replyTo == null) || replyTo.isValid()) &&
            (((replyTo == null) && (faultTo == null)) || ((messageID != null) && messageID.isValid())) ;
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
    public static AddressingContext createResponseContext(final AddressingContext addressingContext, final String messageID)
    {
        final AddressingContext responseContext = new AddressingContext() ;
        initialiseTo(responseContext, addressingContext) ;
        responseContext.addRelatesTo(new RelationshipType(addressingContext.getMessageID().getValue())) ;
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
        final EndpointReferenceType faultTo = addressingContext.getFaultTo() ;
        if (faultTo != null)
        {
            initialiseTo(faultContext, faultTo) ;
        }
        else
        {
            initialiseTo(faultContext, addressingContext) ;
        }
        faultContext.addRelatesTo(new RelationshipType(addressingContext.getMessageID().getValue())) ;
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
        requestContext.setMessageID(new AttributedURIType(messageID)) ;
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
        requestContext.setMessageID(new AttributedURIType(messageID)) ;
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
    public static AddressingContext createNotificationContext(final AddressingContext addressingContext, final String messageID)
    {
        final AddressingContext responseContext = new AddressingContext() ;
        initialiseTo(responseContext, addressingContext) ;
        responseContext.setMessageID(new AttributedURIType(messageID)) ;
        
        return responseContext ;
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
            initialiseTo(newContext, origContext.getFrom()) ;
        }
    }
    
    /**
     * Initialise the To values from the specified endpoint reference.
     * @param addressingContext The addressing context to initialise.
     * @param endpointReference The endpoint reference
     */
    private static void initialiseTo(final AddressingContext addressingContext, final EndpointReferenceType endpointReference)
    {
        addressingContext.setTo(endpointReference.getAddress()) ;
        addressingContext.copyAnyContents(endpointReference.getReferenceParameters()) ;
        addressingContext.copyAnyContents(endpointReference.getReferenceProperties()) ;
    }
}
