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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyAttributeSupport;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.util.StreamHelper;

/*
 * <xs:complexType name="ProblemActionType" mixed="false">
 *   <xs:sequence>
 *     <xs:element ref="tns:Action" minOccurs="0"/>
 *     <xs:element name="SoapAction" minOccurs="0" type="xs:anyURI"/>
 *   </xs:sequence>
 *   <xs:anyAttribute namespace="##other" processContents="lax"/>
 * </xs:complexType>
 */
/**
 * Representation of a ProblemActionType
 * @author kevin
 */
public class ProblemActionType extends AnyAttributeSupport
{
    /**
     * The WS-Addressing action value.
     */
    private AttributedURIType action ;
    /**
     * The SOAP Action value.
     */
    private URI soapAction ;

    /**
     * Default constructor.
     */
    public ProblemActionType()
    {
    }
    
    /**
     * Construct the ProblemActionType with a specific action.
     * @param action The action.
     */
    public ProblemActionType(final AttributedURIType action)
    {
        this(action, null) ;
    }
    
    /**
     * Construct the ProblemActionType with a specific SoapAction.
     * @param soapAction The SoapAction.
     */
    public ProblemActionType(final URI soapAction)
    {
        this(null, soapAction) ;
    }
    
    /**
     * Construct the ProblemActionType with a specific action.
     * @param action The WS-Addressing action.
     * @param soapAction The SOAP action value.
     */
    public ProblemActionType(final AttributedURIType action, final URI soapAction)
    {
        this.action = action ;
        this.soapAction = soapAction ;
    }
    
    /**
     * Construct the ProblemActionType from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for errors during reading.
     */
    public ProblemActionType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Put the action element.
     * @param action The action element.
     */
    public void setAction(final AttributedURIType action)
    {
        this.action = action ;
    }
    
    /**
     * Get the action element.
     * @return The action element.
     */
    public AttributedURIType getAction()
    {
        return action ;
    }
    
    /**
     * Put the soap action element.
     * @param soapAction The soap action element.
     */
    public void setSoapAction(final URI soapAction)
    {
        this.soapAction = soapAction ;
    }
    
    /**
     * Get the soap action element.
     * @return The soap action element.
     */
    public URI getSoapAction()
    {
        return soapAction ;
    }
    
    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wsaddr2005.ProblemActionType_1 [com.arjuna.webservices.wsaddr2005.ProblemActionType_1] - Unexpected element name: {0}
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        if (AddressingConstants.WSA_NAMESPACE.equals(elementName.getNamespaceURI()))
        {
            final String localPart = elementName.getLocalPart() ;
            if (AddressingConstants.WSA_ELEMENT_ACTION.equals(localPart))
            {
                setAction(new AttributedURIType(in)) ;
            }
            else if (AddressingConstants.WSA_ELEMENT_SOAP_ACTION.equals(localPart))
            {
                setSoapAction(new URI(in)) ;
            }
            else
            {
                final String pattern = WSCLogger.arjLoggerI18N.getString("com.arjuna.webservices.wsaddr2005.ProblemActionType_1") ;
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
        if (action != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_ACTION, action) ;
        }
        if (soapAction != null)
        {
            StreamHelper.writeElement(out, AddressingConstants.WSA_ELEMENT_QNAME_SOAP_ACTION, soapAction) ;
        }
        super.writeChildContent(out) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return (((action == null) || action.isValid()) &&
                ((soapAction == null) || soapAction.isValid())) ;
    }
}
