/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices.wsarj;

import java.text.MessageFormat;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.MessageContext;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyContentSupport;
import com.arjuna.webservices.util.StreamHelper;

/**
 * The arjuna context.
 * @author kevin
 */
public class ArjunaContext extends AnyContentSupport
{
    /**
     * The key used for the arjuna context within a message exchange.
     */
    private static final byte[] ARJUNA_CONTEXT_PROPERTY = new byte[0] ;
    
    /**
     * The InstanceIdentifier header.
     */
    private InstanceIdentifier instanceIdentifier ;
    
    /**
     * Default constructor.
     */
    private ArjunaContext()
    {
    }
    
    /**
     * Construct an arjuna context from the input stream.
     * N.B. This method is for testing purposes only.
     * 
     * @param in The input stream.
     * @throws XMLStreamException For errors during parsing.
     */
    public ArjunaContext(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Get the instance identifier.
     * @return The instance identifier.
     */
    public InstanceIdentifier getInstanceIdentifier()
    {
        return instanceIdentifier ;
    }
    
    /**
     * Set the instance identifier.
     * @param instanceIdentifier The instance identifier.
     */
    public void setInstanceIdentifier(final InstanceIdentifier instanceIdentifier)
    {
        this.instanceIdentifier = instanceIdentifier ;
    }
    
    /**
     * Write the content of the context to the stream.
     * @param out The current output stream.
     * @message com.arjuna.webservices.wsarj.ArjunaContext_1 [com.arjuna.webservices.wsarj.ArjunaContext_1] - Arjuna context is not valid
     */
    public void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
       if (!isValid())
       {
           throw new XMLStreamException(WSCLogger.log_mesg.getString("com.arjuna.webservices.wsarj.ArjunaContext_1")) ;
       }
       
       StreamHelper.writeElement(out, ArjunaConstants.WSARJ_ELEMENT_INSTANCE_IDENTIFIER_QNAME, instanceIdentifier) ;
       
       super.writeChildContent(out) ;
    }

    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wsarj.ArjunaContext_2 [com.arjuna.webservices.wsarj.ArjunaContext_2] - Unexpected element name: {0}
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        if (ArjunaConstants.WSARJ_ATTRIBUTE_NAMESPACE.equals(elementName.getNamespaceURI()))
        {
            final String localPart = elementName.getLocalPart() ;
            if (ArjunaConstants.WSARJ_ELEMENT_INSTANCE_IDENTIFIER.equals(localPart))
            {
                setInstanceIdentifier(new InstanceIdentifier(in)) ;
            }
            else
            {
                final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.wsarj.ArjunaContext_2") ;
                final String message = MessageFormat.format(pattern, new Object[] {elementName}) ;
                throw new XMLStreamException(message) ;
            }
        }
        else
        {
            final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.wsarj.ArjunaContext_2") ;
            final String message = MessageFormat.format(pattern, new Object[] {elementName}) ;
            throw new XMLStreamException(message) ;
        }
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((instanceIdentifier != null) && instanceIdentifier.isValid()) ;
    }

    /**
     * Get the arjuna context from the message context if present.
     * @param messageContext The message context.
     * @return The arjuna context or null if not present.
     */
    public static ArjunaContext getCurrentContext(final MessageContext messageContext)
    {
        return (ArjunaContext)messageContext.getProperty(ARJUNA_CONTEXT_PROPERTY) ;
    }

    /**
     * Get the arjuna context from the message context.
     * @param messageContext The message context.
     * @return The arjuna context.
     */
    public static ArjunaContext getContext(final MessageContext messageContext)
    {
        final ArjunaContext current = (ArjunaContext)messageContext.getProperty(ARJUNA_CONTEXT_PROPERTY) ;
        if (current != null)
        {
            return current ;
        }
        final ArjunaContext newContext = new ArjunaContext() ;
        messageContext.setProperty(ARJUNA_CONTEXT_PROPERTY, newContext) ;
        return newContext ;
    }
}
