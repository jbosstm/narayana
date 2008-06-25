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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Utility class implementing any AnyElement.
 * @author kevin
 */
public class AnyElement extends AnyContentAnyAttributeSupport
{
    /**
     * The namespaces defined on this element.
     */
    private Map namespaces ;
    
    /**
     * Default constructor.
     */
    public AnyElement()
    {
    }
    
    /**
     * Construct the any element from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public AnyElement(final XMLStreamReader in)
        throws XMLStreamException
    {
        parseNamespaces(in) ;
        parse(in) ;
    }
    
    /**
     * Set the text value of this element.
     * @param in The current input stream.
     * @param value The text value of this element.
     * @throws XMLStreamException for parsing errors.
     */
    protected void putValue(final XMLStreamReader in, final String value)
        throws XMLStreamException
    {
        putAnyContent(new NamedElement(null, new TextElement(value))) ;
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        if (namespaces != null)
        {
            final NamespaceContext namespaceContext = out.getNamespaceContext() ;
            final Set entries = namespaces.entrySet() ;
            final Iterator entryIter = entries.iterator() ;
            do
            {
                final Map.Entry entry = (Map.Entry)entryIter.next() ;
                final String namespaceURI = (String)entry.getValue() ;
                if (namespaceContext.getPrefix(namespaceURI) == null)
                {
                    final String prefix = (String)entry.getKey() ;
                    if (prefix.length() == 0)
                    {
                        out.setDefaultNamespace(namespaceURI) ;
                        out.writeDefaultNamespace(namespaceURI) ;
                    }
                    else
                    {
                        out.setPrefix(prefix, namespaceURI) ;
                        out.writeNamespace(prefix, namespaceURI) ;
                    }
                }
            }
            while(entryIter.hasNext()) ;
        }
        super.writeChildContent(out) ;
    }
    
    /**
     * Parse the namespaces associated with this element.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    private void parseNamespaces(final XMLStreamReader in)
        throws XMLStreamException
    {
        final int numNamespaces = in.getNamespaceCount() ;
        final String elementNamespaceURI = in.getNamespaceURI() ;
        
        if ((numNamespaces > 1) || ((numNamespaces == 1) &&
            !in.getNamespaceURI(0).equals(elementNamespaceURI)))
        {
            namespaces = new HashMap() ;
            for(int count = 0 ; count < numNamespaces ; count++)
            {
                final String namespaceURI = in.getNamespaceURI(count) ;
                if (!namespaceURI.equals(elementNamespaceURI))
                {
                    namespaces.put(in.getNamespacePrefix(count), in.getNamespaceURI(count)) ;
                }
            }
        }
    }
}
