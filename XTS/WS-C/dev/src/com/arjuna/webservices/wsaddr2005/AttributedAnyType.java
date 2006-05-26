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
import com.arjuna.webservices.stax.AnyAttributeSupport;
import com.arjuna.webservices.stax.AnyElement;
import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.util.StreamHelper;

/*
 * <xs:complexType name="AttributedAnyType" mixed="false">
 *   <xs:sequence>
 *     <xs:any namespace="##any" processContents="lax" minOccurs="1" maxOccurs="1"/>
 *   </xs:sequence>
 *   <xs:anyAttribute namespace="##other" processContents="lax"/>
 * </xs:complexType>
 */
/**
 * Representation of an AttributedAnyType
 * @author kevin
 */
public class AttributedAnyType extends AnyAttributeSupport
{
    /**
     * The Any value of this element.
     */
    private NamedElement any ;

    /**
     * Default constructor.
     */
    public AttributedAnyType()
    {
    }
    
    /**
     * Construct the AttributedAnyType with a specific value.
     * @param any The Any.
     */
    public AttributedAnyType(final NamedElement any)
    {
        this.any = any ;
    }
    
    /**
     * Construct the AttributedAnyType from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for errors during reading.
     */
    public AttributedAnyType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Set the any element.
     * @param any The any element.
     */
    public void setAny(final NamedElement any)
    {
        this.any = any ;
    }
    
    /**
     * Get the any element.
     * @return The any element.
     */
    public NamedElement getAny()
    {
        return any ;
    }
    
    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wsaddr2005.AttributedAnyType_1 [com.arjuna.webservices.wsaddr2005.AttributedAnyType_1] - Unexpected second element name: {0}
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        if (any != null)
        {
            final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.wsaddr2005.AttributedAnyType_1") ;
            final String message = MessageFormat.format(pattern, new Object[] {elementName}) ;
            throw new XMLStreamException(message) ;
        }
        final AnyElement anyElement = new AnyElement(in) ;
        setAny(new NamedElement(elementName, anyElement)) ;
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        if (any != null)
        {
            final QName name = any.getName() ;
            
            if (name != null)
            {
                final String origNamespace = StreamHelper.writeStartElement(out, name) ;
                any.getElementContent().writeContent(out) ;
                StreamHelper.writeEndElement(out, name.getPrefix(), origNamespace) ;
            }
            else
            {
                any.getElementContent().writeContent(out) ;
            }
        }
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        if (any != null)
        {
            final QName name = any.getName() ;
            if (name != null)
            {
                final ElementContent elementContent = any.getElementContent() ;
                return ((elementContent != null) && elementContent.isValid()) ;
            }
        }
        return false ;
    }
}
