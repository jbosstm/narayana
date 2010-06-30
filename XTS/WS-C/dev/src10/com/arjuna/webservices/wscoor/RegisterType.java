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
import com.arjuna.webservices.wsaddr.EndpointReferenceType;

/*
 * <xsd:complexType name="RegisterType">
 *   <xsd:sequence>
 *     <xsd:element name="ProtocolIdentifier" type="xsd:anyURI"/>
 *     <xsd:element name="ParticipantProtocolService" type="wsa:EndpointReferenceType"/>
 *     <xsd:any namespace="##any" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *   </xsd:sequence>
 *   <xsd:anyAttribute namespace="##other" processContents="lax"/>
 * </xsd:complexType>
 */
/**
 * Representation of the Register type.
 * @author kevin
 */
public class RegisterType extends AnyContentAnyAttributeSupport
{
    /**
     * The ProtocolIdentifier element.
     */
    private URI protocolIdentifier ;
    /**
     * The participant protocol service.
     */
    private EndpointReferenceType participantProtocolService ;
    
    /**
     * Default constructor.
     */
    public RegisterType()
    {
    }
    
    /**
     * Construct a register from the input stream.
     * 
     * @param in The input stream.
     * @throws XMLStreamException For errors during parsing.
     */
    public RegisterType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Get the procotol identifier URI.
     * @return The proocol identifier URI.
     */
    public URI getProtocolIdentifier()
    {
        return protocolIdentifier ;
    }
    
    /**
     * Set the protocol identifier URI.
     * @param protocolIdentifier The to URI.
     */
    public void setProtocolIdentifier(final URI protocolIdentifier)
    {
        this.protocolIdentifier = protocolIdentifier ;
    }
    
    /**
     * Get the participant protocol service endpoint reference.
     * @return The participant protocol service endpoint reference.
     */
    public EndpointReferenceType getParticipantProtocolService()
    {
        return participantProtocolService ;
    }
    
    /**
     * Set the participant protocol service endpoint reference.
     * @param participantProtocolService The participant protocol service endpoint reference.
     */
    public void setParticipantProtocolService(final EndpointReferenceType participantProtocolService)
    {
        this.participantProtocolService = participantProtocolService ;
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
           throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wscoor_RegisterType_1()) ;
       }
       
       StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_PROTOCOL_IDENTIFIER_QNAME, protocolIdentifier) ;
       StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_PARTICIPANT_PROTOCOL_SERVICE_QNAME, participantProtocolService) ;
       
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
        if (CoordinationConstants.WSCOOR_NAMESPACE.equals(elementName.getNamespaceURI()))
        {
            final String localPart = elementName.getLocalPart() ;
            if (CoordinationConstants.WSCOOR_ELEMENT_PROTOCOL_IDENTIFIER.equals(localPart))
            {
                setProtocolIdentifier(new URI(in)) ;
            }
            else if (CoordinationConstants.WSCOOR_ELEMENT_PARTICIPANT_PROTOCOL_SERVICE.equals(localPart))
            {
                setParticipantProtocolService(new EndpointReferenceType(in)) ;
            }
            else
            {
                throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wscoor_RegisterType_2(elementName)) ;
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
        return ((protocolIdentifier != null) && protocolIdentifier.isValid()) &&
            ((participantProtocolService != null) && participantProtocolService.isValid()) ;
    }
}
