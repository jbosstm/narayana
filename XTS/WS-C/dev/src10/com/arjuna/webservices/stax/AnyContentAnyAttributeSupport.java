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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.util.QNameHelper;
import com.arjuna.webservices.util.StreamHelper;

/**
 * Utility class providing support for AnyAttributes.
 * @author kevin
 */
public abstract class AnyContentAnyAttributeSupport extends ElementContent
{
    /**
     * The map of attributes.
     */
    private Map anyAttributes ;
    
    /**
     * The list of any content.
     */
    private List anyContent ;
    
    /**
     * Get the any content.
     * @return the live any content.
     */
    public NamedElement[] getAnyContent()
    {
        return (anyContent == null ? null : (NamedElement[]) anyContent.toArray(new NamedElement[anyContent.size()])) ;
    }
    
    /**
     * Add a named element to the list of any elements.
     * @param namedElement The named element.
     */
    public void putAnyContent(final NamedElement namedElement)
    {
        getAnyContentList().add(namedElement) ;
    }
    
    /**
     * Get the any additional attributes if set.
     * @return The attibutes if set, null otherwise.
     */
    public Map getAnyAttributes()
    {
        return anyAttributes ;
    }
    
    /**
     * Put an attribute with a string value.
     * @param attributeName The attribute name.
     * @param attributeValue The attribute value.
     */
    public void putAttribute(final QName attributeName, final String attributeValue)
    {
        putAnyAttribute(attributeName, attributeValue) ;
    }
    
    /**
     * Put an attribute with a QName value.
     * @param attributeName The attribute name.
     * @param attributeValue The attribute value.
     */
    public void putAttribute(final QName attributeName, final QName attributeValue)
    {
        putAnyAttribute(attributeName, attributeValue) ;
    }
    
    /**
     * Add the attribute value to the list of known attributes.
     * @param in The current input stream.
     * @param attributeName The qualified attribute name.
     * @param attributeValue The qualified attibute value.
     */
    protected void putAttribute(final XMLStreamReader in,
        final QName attributeName, final String attributeValue)
        throws XMLStreamException
    {
        if (anyAttributes == null)
        {
            anyAttributes = new HashMap() ;
        }
        
        if (attributeValue.indexOf(':') > 0)
        {
            final QName qname = QNameHelper.toQName(in.getNamespaceContext(), attributeValue) ;
            if (qname.getNamespaceURI().length() > 0)
            {
                putAttribute(attributeName, qname) ;
            }
            else
            {
                putAttribute(attributeName, attributeValue) ;
            }
        }
        else
        {
            putAttribute(attributeName, attributeValue) ;
        }
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
        final AnyElement anyElement = new AnyElement(in) ;
        putAnyContent(new NamedElement(elementName, anyElement)) ;
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        if (anyContent != null)
        {
            final Iterator anyContentIter = anyContent.iterator() ;
            while(anyContentIter.hasNext())
            {
                final NamedElement element = (NamedElement)anyContentIter.next() ;
                final QName name = element.getName() ;
                
                if (name != null)
                {
                    final String origNamespace = StreamHelper.writeStartElement(out, name) ;
                    element.getElementContent().writeContent(out) ;
                    StreamHelper.writeEndElement(out, name.getPrefix(), origNamespace) ;
                }
                else
                {
                    element.getElementContent().writeContent(out) ;
                }
            }
        }
    }
    
    /**
     * Write the attributes of the element.
     * @param out The output stream.
     */
    protected void writeAttributes(final XMLStreamWriter out)
        throws XMLStreamException
    {
        if (anyAttributes != null)
        {
            StreamHelper.writeAttributes(out, anyAttributes) ;
        }
    }
    
    /**
     * Copy the any elements from another source.
     * @param source The source of the any data.
     */
    public void copyAnyContents(final AnyContentAnyAttributeSupport source)
    {
        if ((source != null) && (source.anyContent != null))
        {
            getAnyContentList().addAll(source.anyContent) ;
        }
    }
    
    /**
     * Get the initialised any content list.
     * @return The initialised any content list.
     */
    private List getAnyContentList()
    {
        if (anyContent == null)
        {
            anyContent = new LinkedList() ;
        }
        return anyContent ;
    }
    
    /**
     * Put an attribute.
     * @param attributeName The attribute name.
     * @param attributeValue The attribute value.
     */
    private void putAnyAttribute(final QName attributeName, final Object attributeValue)
    {
        if (anyAttributes == null)
        {
            anyAttributes = new HashMap() ;
        }
        
        anyAttributes.put(attributeName, attributeValue) ;
    }
}
