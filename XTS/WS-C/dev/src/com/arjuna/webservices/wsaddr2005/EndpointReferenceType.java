/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
package com.arjuna.webservices.wsaddr2005;

import java.text.MessageFormat;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyContentAnyAttributeSupport;
import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.util.StreamHelper;

/*
 * <xs:complexType name="EndpointReferenceType" mixed="false">
 *   <xs:sequence>
 *     <xs:element name="Address" type="tns:AttributedURIType"/>
 *     <xs:element name="ReferenceParameters" type="tns:ReferenceParametersType" minOccurs="0"/>
 *     <xs:element ref="tns:Metadata" minOccurs="0"/>
 *     <xs:any namespace="##other" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *   </xs:sequence>
 *   <xs:anyAttribute namespace="##other" processContents="lax"/>
 * </xs:complexType>
 */
/**
 * Representation of an EndpointReferenceType
 * @author kevin
 */
public class EndpointReferenceType extends AnyContentAnyAttributeSupport
{
    /**
     * The address.
     */
    private AttributedURIType address ;
    /**
     * The reference parameters.
     */
    private ReferenceParametersType referenceParameters ;
    /**
     * The metadata.
     */
    private MetadataType metadata ;

    /**
     * Default constructor.
     */
    public EndpointReferenceType()
    {
    }
    
    /**
     * Construct an endpoint reference to a specified address.
     * @param address The endpoint reference address.
     */
    public EndpointReferenceType(final AttributedURIType address)
    {
        setAddress(address) ;
    }
    
    /**
     * Construct the endpoint reference from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public EndpointReferenceType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }

    /**
     * Set the address.
     * @param address The address.
     */
    public void setAddress(final AttributedURIType address)
    {
        this.address = address ;
    }
    
    /**
     * Get the address.
     * @return The address.
     */
    public AttributedURIType getAddress()
    {
        return address ;
    }
    
    /**
     * Get the reference parameters.
     * @return The reference parameters.
     */
    public ReferenceParametersType getReferenceParameters()
    {
        return referenceParameters ;
    }
    
    /**
     * Set the reference parameters.
     * @param referenceParameters The reference parameters.
     */
    public void setReferenceParameters(final ReferenceParametersType referenceParameters)
    {
        this.referenceParameters = referenceParameters ;
    }
    
    /**
     * Add a reference parameter.
     * @param parameter The reference parameter.
     */
    public void addReferenceParameter(final NamedElement parameter)
    {
        if (referenceParameters == null)
        {
            referenceParameters = new ReferenceParametersType() ;
        }
        referenceParameters.putAnyContent(parameter) ;
    }
    
    /**
     * Get the metadata.
     * @return The metadata.
     */
    public MetadataType getMetadata()
    {
        return metadata ;
    }
    
    /**
     * Set the metadata.
     * @param metadata The metadata.
     */
    public void setMetadata(final MetadataType metadata)
    {
        this.metadata = metadata ;
    }
    
    /**
     * Add a metadata.
     * @param parameter The metadata.
     */
    public void addMetadata(final NamedElement parameter)
    {
        if (metadata == null)
        {
        		metadata = new MetadataType() ;
        }
        metadata.putAnyContent(parameter) ;
    }
    
    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wsaddr2005.EndpointReferenceType_1 [com.arjuna.webservices.wsaddr2005.EndpointReferenceType_1] - Unexpected element name: {0}
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        if (AddressingConstants.WSA_NAMESPACE.equals(elementName.getNamespaceURI()))
        {
            final String localPart = elementName.getLocalPart() ;
            if (AddressingConstants.WSA_ELEMENT_ADDRESS.equals(localPart))
            {
                setAddress(new AttributedURIType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_REFERENCE_PARAMETERS.equals(localPart))
            {
                setReferenceParameters(new ReferenceParametersType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_METADATA.equals(localPart))
            {
                setMetadata(new MetadataType(in)) ;
            }
            else
            {
                final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.wsaddr2005.EndpointReferenceType_1") ;
                final String message = MessageFormat.format(pattern, new Object[] {elementName}) ;
                throw new XMLStreamException(message) ;
            }
        }
        else
        {
            super.putElement(in, elementName) ;
        }
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_ADDRESS, address) ;
        if (referenceParameters != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_REFERENCE_PARAMETERS, referenceParameters) ;
        }
        if (metadata != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_METADATA, metadata) ;
        }
        super.writeChildContent(out) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((address != null) && address.isValid()) &&
            ((referenceParameters == null) || referenceParameters.isValid()) &&
            ((metadata == null) || metadata.isValid()) ;
    }
}
