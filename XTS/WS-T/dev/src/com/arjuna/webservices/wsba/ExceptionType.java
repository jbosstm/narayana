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
package com.arjuna.webservices.wsba;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.stax.AnyContentAnyAttributeSupport;
import com.arjuna.webservices.util.StreamHelper;

/*
 * <xsd:complexType name="ExceptionType">
 *   <xsd:sequence>
 *     <xsd:element name="ExceptionIdentifier" type="xsd:string"/>
 *     <xsd:any namespace="##other" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *   </xsd:sequence>
 *   <xsd:anyAttribute namespace="##other" processContents="lax"/>
 * </xsd:complexType>
 */
/**
 * Representation of an ExceptionType
 * @author kevin
 */
public class ExceptionType extends AnyContentAnyAttributeSupport
{
    /**
     * The exception identifier.
     */
    private ExceptionIdentifierType exceptionIdentifier ;

    /**
     * Default constructor.
     */
    public ExceptionType()
    {
    }
    
    /**
     * Construct an exception type with the specified exception identifier.
     * @param exceptionIdentifier The exception identifier.
     */
    public ExceptionType(final String exceptionIdentifier)
    {
        setExceptionIdentifier(exceptionIdentifier) ;
    }
    
    /**
     * Construct the exception from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public ExceptionType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }

    /**
     * Set the exception identifier.
     * @param exceptionIdentifier The exception identifier.
     */
    private void setExceptionIdentifier(final ExceptionIdentifierType exceptionIdentifier)
    {
        this.exceptionIdentifier = exceptionIdentifier ;
    }

    /**
     * Set the exception identifier.
     * @param exceptionIdentifier The exception identifier.
     */
    public void setExceptionIdentifier(final String exceptionIdentifier)
    {
        setExceptionIdentifier(new ExceptionIdentifierType(exceptionIdentifier)) ;
    }
    
    /**
     * Get the exception identifier.
     * @return The exception identifier.
     */
    public String getExceptionIdentifier()
    {
        return (exceptionIdentifier == null ? null : exceptionIdentifier.getExceptionIdentifier()) ;
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
        if (BusinessActivityConstants.WSBA_NAMESPACE.equals(elementName.getNamespaceURI()))
        {
            final String localPart = elementName.getLocalPart() ;
            if (BusinessActivityConstants.WSBA_ELEMENT_EXCEPTION_IDENTIFIER.equals(localPart))
            {
                setExceptionIdentifier(new ExceptionIdentifierType(in)) ;
            }
            else
            {
                throw new XMLStreamException("Unexpected element name: " + elementName) ;
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
        StreamHelper.writeElement(out, BusinessActivityConstants.WSBA_ELEMENT_EXCEPTION_IDENTIFIER_QNAME, exceptionIdentifier) ;
        super.writeChildContent(out) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((exceptionIdentifier != null) && exceptionIdentifier.isValid()) &&
            super.isValid() ;
    }
}
