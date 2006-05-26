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

import java.math.BigInteger;
import java.text.MessageFormat;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.stax.AnyAttributeSupport;

/*
 * <xs:complexType>
 *   <xs:simpleContent>
 *     <xs:extension base="xs:unsignedLong">
 *       <xs:anyAttribute namespace="##other" processContents="lax"/>
 *     </xs:extension>
 *   </xs:simpleContent>
 * </xs:complexType>
 */
/**
 * Representation of an attributed unsigned long type.
 * @author kevin
 */
public class AttributedUnsignedLongType extends AnyAttributeSupport
{
    /**
     * The maximum value of an unsigned long.
     */
    private static final BigInteger MAX_UNSIGNED_LONG = BigInteger.ONE.shiftLeft(64) ;
    
    /**
     * Default constructor.
     */
    public AttributedUnsignedLongType()
    {
    }
    
    /**
     * Construct the attributed unsigned int type with the specific value.
     * @param value The unsigned int value.
     */
    public AttributedUnsignedLongType(final BigInteger value)
    {
        setValue(value) ;
    }
    
    /**
     * Construct the attributed unsigned int type from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public AttributedUnsignedLongType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * The value of this element.
     */
    private BigInteger value ;
    
    /**
     * Set the text value of this element.
     * @param in The current input stream.
     * @param value The text value.
     */
    protected void putValue(final XMLStreamReader in, final String value)
        throws XMLStreamException
    {
        try
        {
            setValue(new BigInteger(value.trim())) ;
        }
        catch (final NumberFormatException nfe)
        {
            final String pattern = "non numerical value: {0}" ;
            final String message = MessageFormat.format(pattern, new Object[] {value}) ;
            throw new XMLStreamException(message) ;
        }
    }
    
    /**
     * Set the unsigned int value of this element.
     * @param value The unsigned int value of the element.
     */
    public void setValue(final BigInteger value)
    {
        this.value = value ;
    }
    
    /**
     * Get the unsigned int value of this element.
     * @return The unsigned int value of the element.
     */
    public BigInteger getValue()
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
        out.writeCharacters(value.toString()) ;
    }
    
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((value != null) && (value.compareTo(MAX_UNSIGNED_LONG) < 0)) ;
    }
}
