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

import com.arjuna.webservices.stax.ElementContent;

/**
 * Representation of an ExceptionIdentifier element.
 * @author kevin
 */
class ExceptionIdentifierType extends ElementContent
{
    /**
     * The exception identifier.
     */
    private String exceptionIdentifier ;
    
    /**
     * Construct the exception identifier with the specified identifier.
     * @param exceptionIdentifier The exception identifier.
     */
    ExceptionIdentifierType(final String exceptionIdentifier)
    {
        this.exceptionIdentifier = exceptionIdentifier ;
    }
    
    /**
     * Construct the ExceptionIdentifier from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    ExceptionIdentifierType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
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
        throw new XMLStreamException("ExceptionIdentifier elements cannot have embedded elements.") ;
    }
    
    /**
     * Set the exception identifier of this element.
     * @param exceptionIdentifier The exception identifier of the element.
     */
    void setExceptionIdentifier(final String exceptionIdentifier)
    {
        this.exceptionIdentifier = exceptionIdentifier ;
    }
    
    /**
     * Get the exception identifier of this element.
     * @return The exception identifier of the element or null if not set.
     */
    String getExceptionIdentifier()
    {
        return exceptionIdentifier ;
    }
    
    /**
     * Set the text value of this element.
     * @param in The current input stream.
     * @param value The text value of this element.
     */
    protected void putValue(final XMLStreamReader in, final String value)
        throws XMLStreamException
    {
        exceptionIdentifier = value ;
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        out.writeCharacters(exceptionIdentifier) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return (exceptionIdentifier != null) && (exceptionIdentifier.trim().length() > 0)
            && super.isValid() ;
    }
}
