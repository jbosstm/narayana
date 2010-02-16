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

import java.text.MessageFormat;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyAttributeSupport;

/*
 * <xsd:complexType>
 *   <xsd:simpleContent>
 *     <xsd:extension base="xsd:unsignedInt">
 *       <xsd:anyAttribute namespace="##other"/>
 *     </xsd:extension>
 *   </xsd:simpleContent>
 * </xsd:complexType>
 */
/**
 * Representation of an attributed unsigned int type.
 * @author kevin
 */
public class AttributedUnsignedIntType extends AnyAttributeSupport
{
    /**
     * The maximum value of an unsigned int.
     */
    private static final long MAX_UNSIGNED_INT = (1L<<32)-1 ;
    
    /**
     * Default constructor.
     */
    public AttributedUnsignedIntType()
    {
    }
    
    /**
     * Construct the attributed unsigned int type with the specific value.
     * @param value The unsigned int value.
     */
    public AttributedUnsignedIntType(final long value)
    {
        setValue(value) ;
    }
    
    /**
     * Construct the attributed unsigned int type from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public AttributedUnsignedIntType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * The value of this element.
     */
    private long value ;
    
    /**
     * Set the text value of this element.
     * @param in The current input stream.
     * @param value The text value.
     * @message com.arjuna.webservices.wscoor.AttributedUnsignedIntType_1 [com.arjuna.webservices.wscoor.AttributedUnsignedIntType_1] - non numerical value: {0}
     */
    protected void putValue(final XMLStreamReader in, final String value)
        throws XMLStreamException
    {
        try
        {
            setValue(Long.parseLong(value.trim())) ;
        }
        catch (final NumberFormatException nfe)
        {
            final String pattern = WSCLogger.arjLoggerI18N.getString("com.arjuna.webservices.wscoor.AttributedUnsignedIntType_1") ;
            final String message = MessageFormat.format(pattern, new Object[] {value}) ;
            throw new XMLStreamException(message) ;
        }
    }
    
    /**
     * Set the unsigned int value of this element.
     * @param value The unsigned int value of the element.
     */
    public void setValue(final long value)
    {
        this.value = value ;
    }
    
    /**
     * Get the unsigned int value of this element.
     * @return The unsigned int value of the element.
     */
    public long getValue()
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
        out.writeCharacters(Long.toString(value)) ;
    }
    
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((value >= 0) && (value <= MAX_UNSIGNED_INT)) ;
    }
}
