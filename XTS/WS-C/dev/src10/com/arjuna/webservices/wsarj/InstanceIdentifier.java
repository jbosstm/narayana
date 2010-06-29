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
package com.arjuna.webservices.wsarj;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;

/**
 * Representation of an InstanceIdentifier element.
 * @author kevin
 */
public class InstanceIdentifier extends ElementContent
{
    /**
     * The instance identifier.
     */
    private String instanceIdentifier ;
    
    /**
     * Default constructor.
     */
    public InstanceIdentifier()
    {
    }
    
    /**
     * Construct an instance identifier with the specific identifier
     * @param instanceIdentifier The instance identifier.
     */
    public InstanceIdentifier(final String instanceIdentifier)
    {
        this.instanceIdentifier = instanceIdentifier ;
    }
    
    /**
     * Construct the InstanceIdentifier from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public InstanceIdentifier(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wsarj.InstanceIdentifier_1 [com.arjuna.webservices.wsarj.InstanceIdentifier_1] - InstanceIdentifier elements cannot have embedded elements.
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wsarj_InstanceIdentifier_1()) ;
    }
    
    /**
     * Set the text value of this element.
     * @param in The current input stream.
     * @param value The text value of this element.
     */
    protected void putValue(final XMLStreamReader in, final String value)
        throws XMLStreamException
    {
        instanceIdentifier = value ;
    }
    
    /**
     * Set the instance identifier of this element.
     * @param instanceIdentifier The instance identifier of the element.
     */
    public void setInstanceIdentifier(final String instanceIdentifier)
    {
        this.instanceIdentifier = instanceIdentifier ;
    }
    
    /**
     * Get the instance identifier of this element.
     * @return The instance identifier of the element or null if not set.
     */
    public String getInstanceIdentifier()
    {
        return instanceIdentifier ;
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        out.writeCharacters(instanceIdentifier) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return (instanceIdentifier != null) && (instanceIdentifier.trim().length() > 0)
            && super.isValid() ;
    }
    
    /**
     * Get a string representation of this instance identifier.
     * @return the string representation.
     */
    public String toString()
    {
        return (instanceIdentifier != null ? instanceIdentifier : "") ;
    }
    
    /**
     * Set the identifier on the endpoint reference.
     * @param endpointReference The endpoint reference.
     * @param identifier The identifier.
     */
    public static void setEndpointInstanceIdentifier(final EndpointReferenceType endpointReference, final String identifier)
    {
        setEndpointInstanceIdentifier(endpointReference, new InstanceIdentifier(identifier)) ;
    }
    
    /**
     * Set the identifier on the endpoint reference.
     * @param endpointReference The endpoint reference.
     * @param instanceIdentifier The identifier.
     */
    public static void setEndpointInstanceIdentifier(final EndpointReferenceType endpointReference, final InstanceIdentifier instanceIdentifier)
    {
        endpointReference.addReferenceParameter(new NamedElement(ArjunaConstants.WSARJ_ELEMENT_INSTANCE_IDENTIFIER_QNAME, instanceIdentifier)) ;
    }
}
