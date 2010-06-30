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
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.util.StreamHelper;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;

/*
 * <xsd:complexType name="CoordinationContextType">
 *   <xsd:sequence>
 *     <xsd:element name="Identifier">
 *       <xsd:complexType>
 *         <xsd:simpleContent>
 *           <xsd:extension base="xsd:anyURI">
 *             <xsd:anyAttribute namespace="##other"/>
 *           </xsd:extension>
 *         </xsd:simpleContent>
 *       </xsd:complexType>
 *     </xsd:element>
 *     <xsd:element ref="wscoor:Expires" minOccurs="0"/>
 *     <xsd:element name="CoordinationType" type="xsd:anyURI"/>
 *     <xsd:element name="RegistrationService" type="wsa:EndpointReferenceType"/>
 *   </xsd:sequence>
 *   <xsd:anyAttribute namespace="##other" processContents="lax"/>
 * </xsd:complexType>
 * 
 * Also added at the end of the sequence 
 *   <xsd:any namespace="##other" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 */
/**
 * Representation of the Coordination Context type.
 * @author kevin
 */
public class CoordinationContextType extends AnyContentAnyAttributeSupport
{
    /**
     * The Identifier element.
     */
    private AttributedURIType identifier ;
    /**
     * The Expires element.
     */
    private AttributedUnsignedIntType expires ;
    /**
     * The Coordination Type uri.
     */
    private URI coordinationType ;
    /**
     * The registration service.
     */
    private EndpointReferenceType registrationService ;
    
    /**
     * Default constructor.
     */
    public CoordinationContextType()
    {
    }
    
    /**
     * Construct a coordination context from the input stream.
     * 
     * @param in The input stream.
     * @throws XMLStreamException For errors during parsing.
     */
    public CoordinationContextType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Get the identifier URI.
     * @return The identifier URI.
     */
    public AttributedURIType getIdentifier()
    {
        return identifier ;
    }
    
    /**
     * Set the identifier URI.
     * @param identifier The to URI.
     */
    public void setIdentifier(final AttributedURIType identifier)
    {
        this.identifier = identifier ;
    }
    
    /**
     * Get the expires data.
     * @return The expires information or null if not present.
     */
    public AttributedUnsignedIntType getExpires()
    {
        return expires ;
    }
    
    /**
     * Set the expires data.
     * @param expires The expires data.
     */
    public void setExpires(final AttributedUnsignedIntType expires)
    {
        this.expires = expires ;
    }
    
    /**
     * Get the coordination type.
     * @return The coordination type.
     */
    public URI getCoordinationType()
    {
        return coordinationType ;
    }
    
    /**
     * Set the coordination type.
     * @param coordinationType The coordination type.
     */
    public void setCoordinationType(final URI coordinationType)
    {
        this.coordinationType = coordinationType ;
    }
    
    /**
     * Get the registration service endpoint reference.
     * @return The registration service endpoint reference.
     */
    public EndpointReferenceType getRegistrationService()
    {
        return registrationService ;
    }
    
    /**
     * Set the registration service endpoint reference.
     * @param registrationService The registration service endpoint reference.
     */
    public void setRegistrationService(final EndpointReferenceType registrationService)
    {
        this.registrationService = registrationService ;
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
           throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wscoor_CoordinationContextType_1()) ;
       }
       
       StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_IDENTIFIER_QNAME, identifier) ;
       if (expires != null)
       {
           StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_EXPIRES_QNAME, expires) ;
       }
       StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_TYPE_QNAME, coordinationType) ;
       StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_REGISTRATION_SERVICE_QNAME, registrationService) ;
       
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
        final String namespace = elementName.getNamespaceURI() ;
        if (CoordinationConstants.WSCOOR_NAMESPACE.equals(namespace))
        {
            final String localPart = elementName.getLocalPart() ;
            if (CoordinationConstants.WSCOOR_ELEMENT_IDENTIFIER.equals(localPart))
            {
                setIdentifier(new AttributedURIType(in)) ;
            }
            else if (CoordinationConstants.WSCOOR_ELEMENT_EXPIRES.equals(localPart))
            {
                setExpires(new AttributedUnsignedIntType(in)) ;
            }
            else if (CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_TYPE.equals(localPart))
            {
                setCoordinationType(new URI(in)) ;
            }
            else if (CoordinationConstants.WSCOOR_ELEMENT_REGISTRATION_SERVICE.equals(localPart))
            {
                setRegistrationService(new EndpointReferenceType(in)) ;
            }
            else
            {
                throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wscoor_CoordinationContextType_2(elementName)) ;
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
        return ((identifier != null) && identifier.isValid()) &&
            ((expires == null) || expires.isValid()) &&
            ((coordinationType != null) && coordinationType.isValid()) &&
            ((registrationService != null) && registrationService.isValid()) ;
    }
}
