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
package com.arjuna.webservices.stax;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSCLogger;

/**
 * Utility class representing a text element.
 * @author kevin
 */
public class TextElement extends ElementContent
{
    /**
     * The value of the text element.
     */
    private String text ;
    
    /**
     * Construct the text element.
     * @param text The text element.
     */
    public TextElement(final String text)
    {
        this.text = text ;
    }
    
    /**
     * Construct the text element.
     * @param in The current input stream.
     * @throws XMLStreamException For errors during reading.
     */
    public TextElement(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Get the text value of this element.
     * @return The text value of this element.
     */
    public String getText()
    {
        return text ;
    }
    
    /**
     * Set the text value of this element.
     * @param in The current input stream.
     * @param value The text value of this element.
     */
    protected void putValue(final XMLStreamReader in, final String value)
        throws XMLStreamException
    {
        text = value ;
    }
    
    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * 
     * @message com.arjuna.webservices.stax.TextElement_1 [com.arjuna.webservices.stax.TextElement_1] - Text elements cannot have embedded elements.
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_stax_TextElement_1()) ;
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        out.writeCharacters(text) ;
    }
}
