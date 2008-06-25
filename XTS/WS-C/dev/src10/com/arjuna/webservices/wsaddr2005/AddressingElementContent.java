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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.util.StreamHelper;

/**
 * A class representing an addressing header element.
 * @author kevin
 *
 */
public class AddressingElementContent extends ElementContent
{
    /**
     * The wrapped element content.
     */
    private final ElementContent elementContent ;
    
    /**
     * Construct the addressing header element.
     * @param elementContent The wrapped element content.
     */
    AddressingElementContent(final ElementContent elementContent)
    {
        this.elementContent = elementContent ;
    }
    
    /**
     * Write the contents of the element.
     * @param out The output stream.
     */
    public final void writeContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        StreamHelper.writeAttribute(out, AddressingConstants.WSA_ATTRIBUTE_QNAME_IS_REFERENCE_PARAMETER, "true") ;
        elementContent.writeContent(out) ;
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
    }

}
