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
package com.arjuna.webservices.stax;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;


/*
 * xs:anyURI
 */
/**
 * Representation of a URI
 * @author kevin
 */
public class URI extends ElementContent
{
    /**
     * The URI value of this element.
     */
    private String value ;

    /**
     * Default constructor.
     */
    public URI()
    {
    }
    
    /**
     * Create the URI with a specific value.
     * @param value The URI value.
     */
    public URI(final String value)
    {
        setValue(value) ;
    }
    
    /**
     * Construct the attributed uri from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for errors during reading.
     */
    public URI(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }

    /**
     * Set the text value of this element.
     * @param in The current input stream.
     * @param value The text value.
     */
    protected void putValue(final XMLStreamReader in, final String value)
        throws XMLStreamException
    {
        setValue(value) ;
    }
    
    /**
     * Set the URI value of this element.
     * @param value The URI value of the element.
     */
    public void setValue(final String value)
    {
        this.value = value ;
    }
    
    /**
     * Get the URI value of this element.
     * @return The URI value of the element or null if not set.
     */
    public String getValue()
    {
        return value ;
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        out.writeCharacters(value) ;
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
        // Ignore elements.
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return (value != null) && (value.trim().length() > 0) && super.isValid() ;
    }
}
