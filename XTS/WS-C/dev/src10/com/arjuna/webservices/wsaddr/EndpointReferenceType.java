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
 * <xs:complexType name="EndpointReferenceType">
 *   <xs:sequence>
 *     <xs:element name="Address" type="wsa:AttributedURI"/>
 *     <xs:element name="ReferenceProperties" type="wsa:ReferencePropertiesType" minOccurs="0"/>
 *     <xs:element name="ReferenceParameters" type="wsa:ReferenceParametersType" minOccurs="0"/>
 *     <xs:element name="PortType" type="wsa:AttributedQName" minOccurs="0"/>
 *     <xs:element name="ServiceName" type="wsa:ServiceNameType" minOccurs="0"/>
 *     <xs:any namespace="##other" processContents="lax" minOccurs="0" maxOccurs="unbounded">
 *       <xs:annotation>
 *         <xs:documentation>
 *           If "Policy" elements from namespace "http://schemas.xmlsoap.org/ws/2002/12/policy#policy" are used, they must appear first (before any extensibility elements).
 *         </xs:documentation>
 *       </xs:annotation>
 *     </xs:any>
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
     * The reference properties.
     */
    private ReferencePropertiesType referenceProperties ;
    /**
     * The reference parameters.
     */
    private ReferenceParametersType referenceParameters ;
    /**
     * The port type.
     */
    private AttributedQNameType portType ;
    /**
     * The service name.
     */
    private ServiceNameType serviceName ;

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
     * Get the reference properties.
     * @return The reference properties.
     */
    public ReferencePropertiesType getReferenceProperties()
    {
        return referenceProperties ;
    }
    
    /**
     * Set the reference properties.
     * @param referenceProperties The reference properties.
     */
    public void setReferenceProperties(final ReferencePropertiesType referenceProperties)
    {
        this.referenceProperties = referenceProperties ;
    }
    
    /**
     * Add a reference property.
     * @param property The reference property.
     */
    public void addReferenceProperty(final NamedElement property)
    {
        if (referenceProperties == null)
        {
            referenceProperties = new ReferencePropertiesType() ;
        }
        referenceProperties.putAnyContent(property) ;
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
     * get the port type.
     * @return The port type.
     */
    public AttributedQNameType getPortType()
    {
        return portType ;
    }
    
    /**
     * Set the port type.
     * @param portType The port type.
     */
    public void setPortType(final AttributedQNameType portType)
    {
        this.portType = portType ;
    }
    
    /**
     * Get the service name.
     * @return The service name.
     */
    public ServiceNameType getServiceName()
    {
        return serviceName ;
    }
    
    /**
     * Set the service name.
     * @param serviceName The service name.
     */
    public void setServiceName(final ServiceNameType serviceName)
    {
        this.serviceName = serviceName ;
    }
    
    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wsaddr.EndpointReferenceType_1 [com.arjuna.webservices.wsaddr.EndpointReferenceType_1] - Unexpected element name: {0}
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
            else if (AddressingConstants.WSA_ELEMENT_REFERENCE_PROPERTIES.equals(localPart))
            {
                setReferenceProperties(new ReferencePropertiesType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_REFERENCE_PARAMETERS.equals(localPart))
            {
                setReferenceParameters(new ReferenceParametersType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_PORT_TYPE.equals(localPart))
            {
                setPortType(new AttributedQNameType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_SERVICE_NAME.equals(localPart))
            {
                setServiceName(new ServiceNameType(in)) ;
            }
            else
            {
                throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wsaddr_EndpointReferenceType_1(elementName)) ;
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
        StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_ADDRESS_QNAME, address) ;
        if (referenceProperties != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_REFERENCE_PROPERTIES_QNAME, referenceProperties) ;
        }
        if (referenceParameters != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_REFERENCE_PARAMETERS_QNAME, referenceParameters) ;
        }
        if (portType != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_PORT_TYPE_QNAME, portType) ;
        }
        if (serviceName != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_SERVICE_NAME_QNAME, serviceName) ;
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
            ((referenceProperties == null) || referenceProperties.isValid()) &&
            ((referenceParameters == null) || referenceParameters.isValid()) &&
            ((portType == null) || portType.isValid()) &&
            ((serviceName == null) || serviceName.isValid()) ;
    }
}
