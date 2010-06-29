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
package com.arjuna.webservices.wscoor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyContentAnyAttributeSupport;
import com.arjuna.webservices.util.StreamHelper;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;

/*
 * <xsd:complexType name="RegisterResponseType">
 *   <xsd:sequence>
 *     <xsd:element name="CoordinatorProtocolService" type="wsa:EndpointReferenceType"/>
 *     <xsd:any namespace="##any" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *   </xsd:sequence>
 *   <xsd:anyAttribute namespace="##other" processContents="lax"/>
 * </xsd:complexType>
 */
/**
 * Representation of the Register Response type.
 * @author kevin
 */
public class RegisterResponseType extends AnyContentAnyAttributeSupport
{
    /**
     * The coordinator protocol service.
     */
    private EndpointReferenceType coordinatorProtocolService ;
    
    /**
     * Default constructor.
     */
    public RegisterResponseType()
    {
    }
    
    /**
     * Construct a register response from the input stream.
     * 
     * @param in The input stream.
     * @throws XMLStreamException For errors during parsing.
     */
    public RegisterResponseType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Get the coordinator protocol service endpoint reference.
     * @return The coordinator protocol service endpoint reference.
     */
    public EndpointReferenceType getCoordinatorProtocolService()
    {
        return coordinatorProtocolService ;
    }
    
    /**
     * Set the coordinator protocol service endpoint reference.
     * @param coordinatorProtocolService The coordinator protocol service endpoint reference.
     */
    public void setCoordinatorProtocolService(final EndpointReferenceType coordinatorProtocolService)
    {
        this.coordinatorProtocolService = coordinatorProtocolService ;
    }
    
    /**
     * Write the content of the context to the stream.
     * @param out The current output stream.
     * @message com.arjuna.webservices.wscoor.RegisterResponseType_1 [com.arjuna.webservices.wscoor.RegisterResponseType_1] - Register is not valid
     */
    public void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
       if (!isValid())
       {
           throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wscoor_RegisterResponseType_1()) ;
       }
       
       StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_COORDINATOR_PROTOCOL_SERVICE_QNAME, coordinatorProtocolService) ;
       
       super.writeChildContent(out) ;
    }

    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wscoor.RegisterResponseType_2 [com.arjuna.webservices.wscoor.RegisterResponseType_2] - Unexpected element name: {0}
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        if (CoordinationConstants.WSCOOR_NAMESPACE.equals(elementName.getNamespaceURI()))
        {
            final String localPart = elementName.getLocalPart() ;
            if (CoordinationConstants.WSCOOR_ELEMENT_COORDINATOR_PROTOCOL_SERVICE.equals(localPart))
            {
                setCoordinatorProtocolService(new EndpointReferenceType(in)) ;
            }
            else
            {
                throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wscoor_RegisterResponseType_2(elementName)) ;
            }
        }
        else
        {
            super.putElement(in, elementName) ;
        }
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((coordinatorProtocolService != null) && coordinatorProtocolService.isValid()) ;
    }
}
