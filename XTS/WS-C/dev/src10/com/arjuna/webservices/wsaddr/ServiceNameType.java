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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.util.StreamHelper;

/*
 * <xs:complexType name="ServiceNameType">
 *   <xs:simpleContent>
 *     <xs:extension base="xs:QName">
 *       <xs:attribute name="PortName" type="xs:NCName"/>
 *       <xs:anyAttribute namespace="##other" processContents="lax"/>
 *     </xs:extension>
 *   </xs:simpleContent>
 * </xs:complexType>
 */
/**
 * Representation of a ServiceName
 * @author kevin
 */
public class ServiceNameType extends AttributedQNameType
{
    /**
     * Default constructor.
     */
    public ServiceNameType()
    {
    }
    
    /**
     * Construct the service name from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public ServiceNameType(final XMLStreamReader in)
        throws XMLStreamException
    {
        super(in) ;
    }
    
    /**
     * The port name attribute.
     */
    private String portName ;
    
    /**
     * Get the port name.
     * @return The port name.
     */
    public String getPortName()
    {
        return portName ;
    }
    
    /**
     * Set the port name.
     * @param portName The port name.
     */
    public void setPortName(final String portName)
    {
        this.portName = portName ;
    }
    
    /**
     * Add the attribute value to the list of known attributes.
     * @param in The current input stream.
     * @param attributeName The qualified attribute name.
     * @param attributeValue The qualified attibute value.
     */
    protected void putAttribute(final XMLStreamReader in,
        final QName attributeName, final String attributeValue)
        throws XMLStreamException
    {
        if (AddressingConstants.WSA_ATTRIBUTE_NAMESPACE.equals(attributeName.getNamespaceURI()) &&
                AddressingConstants.WSA_ATTRIBUTE_PORT_NAME.equals(attributeName.getLocalPart()))
        {
            setPortName(attributeValue) ;
        }
        else
        {
            super.putAttribute(in, attributeName, attributeValue) ;
        }
    }
    
    /**
     * Write the attributes of the element.
     * @param out The output stream.
     */
    protected void writeAttributes(final XMLStreamWriter out)
        throws XMLStreamException
    {
        StreamHelper.writeAttribute(out, AddressingConstants.WSA_ATTRIBUTE_PORT_NAME_QNAME, portName) ;
        super.writeAttributes(out) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return (portName != null) && super.isValid() ;
    }
}
