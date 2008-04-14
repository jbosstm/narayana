/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
package com.arjuna.webservices.adapters;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;



/**
 * XMLStreamWriter for writing to a DOM tree.
 * @author kevin
 */
public class DOMXMLStreamWriter extends BaseXMLStreamWriter
{
    /**
     * The XMLNS namespace.
     */
    private static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/" ;
    
    /**
     * The owner document.
     */
    private final Document document ;
    /**
     * The current Element ;
     */
    private Element currentElement ;
    
    /**
     * Construct the DOM XMLStreamWriter.
     * @param headerElement The header element to populate.
     * @throws XMLStreamException for errors during construction.
     */
    public DOMXMLStreamWriter(final Element headerElement)
        throws XMLStreamException
    {
        this.currentElement = headerElement ;
        this.document = headerElement.getOwnerDocument() ;
        final String namespaceURI = headerElement.getNamespaceURI() ;
        if (namespaceURI != null)
        {
            final String prefix = headerElement.getPrefix() ;
            setPrefix(prefix,namespaceURI) ;
        }
    }

    /**
     * Write a start element.
     * @param localName The local name of the tag.
     */
    public void writeStartElement(final String localName)
        throws XMLStreamException
    {
        final Element element = document.createElement(localName) ;
        setCurrentElement(element) ;
    }

    /**
     * Write a start element with the specified namespace URI.
     * @param namespaceURI The namespace URI of the tag.
     * @param localName The local name of the tag.
     */
    public void writeStartElement(final String namespaceURI, final String localName)
        throws XMLStreamException
    {
        final String prefix = getNamespaceContext().getPrefix(normaliseNamespace(namespaceURI)) ;
        writeStartElement(prefix, localName, namespaceURI) ;
    }

    /**
     * Write a start element with the specified namespace URI and prefix.
     * @param prefix The prefix of the tag.
     * @param localName The local name of the tag.
     * @param namespaceURI The namespace URI of the tag.
     */
    public void writeStartElement(final String prefix, final String localName, final String namespaceURI)
        throws XMLStreamException
    {
            final Element element = document.createElementNS(normaliseNamespace(namespaceURI), localName) ;
            element.setPrefix(prefix) ;
            setCurrentElement(element) ;
    }

    /**
     * Write an empty element.
     * @param localName The local name of the tag.
     */
    public void writeEmptyElement(final String localName)
        throws XMLStreamException
    {
        final Element element = document.createElement(localName) ;
        currentElement.appendChild(element) ;
    }

    /**
     * Write an empty element with the specified namespace URI.
     * @param namespaceURI The namespace URI of the tag.
     * @param localName The local name of the tag.
     */
    public void writeEmptyElement(final String namespaceURI, final String localName)
        throws XMLStreamException
    {
        final Element element = document.createElementNS(normaliseNamespace(namespaceURI), localName) ;
        currentElement.appendChild(element) ;
    }

    /**
     * Write an empty element with the specified namespace URI and prefix.
     * @param prefix The prefix of the tag.
     * @param localName The local name of the tag.
     * @param namespaceURI The namespace URI of the tag.
     */
    public void writeEmptyElement(final String prefix, final String localName, final String namespaceURI)
        throws XMLStreamException
    {
        final Element element = document.createElementNS(normaliseNamespace(namespaceURI), localName) ;
        element.setPrefix(prefix) ;
        currentElement.appendChild(element) ;
    }

    /**
     * Write an end element.
     */
    public void writeEndElement()
        throws XMLStreamException
    {
        currentElement = (Element)currentElement.getParentNode() ;
        popNamespaceContext() ;
    }

    /**
     * Write an attribute to the stream without a prefix.
     * @param localName the local name of the attribute.
     * @param value The attribute value.
     */
    public void writeAttribute(final String localName, final String value)
            throws XMLStreamException
    {
        currentElement.setAttribute(localName, value) ;
    }

    /**
     * Write an attribute to the stream.
     * @param namespaceURI The namespace URI of the attribute.
     * @param localName the local name of the attribute.
     * @param value The attribute value.
     */
    public void writeAttribute(final String namespaceURI, final String localName, final String value)
        throws XMLStreamException
    {
        currentElement.setAttributeNS(normaliseNamespace(namespaceURI), localName, value) ;
    }

    /**
     * Write an attribute to the stream.
     * @param prefix The prefix of the attribute.
     * @param namespaceURI The namespace URI of the attribute.
     * @param localName the local name of the attribute.
     * @param value The attribute value.
     */
    public void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value)
            throws XMLStreamException
    {
        currentElement.setAttributeNS(normaliseNamespace(namespaceURI), prefix+":"+localName, value) ;
    }

    /**
     * Write a text section.
     * @param text The text to write.
     */
    public void writeCharacters(final String text)
        throws XMLStreamException
    {
        final Text textNode = document.createTextNode(text) ;
        currentElement.appendChild(textNode) ;
    }
    
    /**
     * Write a text section.
     * @param text The text to write.
     * @param start The index of the first character.
     * @param len The length of the text.
     */
    public void writeCharacters(char[] text, final int start, final int len)
            throws XMLStreamException
    {
        writeCharacters(new String(text, start, len)) ;
    }

    /**
     * Write a CData section.
     * @param cdata The CData section.
     */
    public void writeCData(final String cdata)
        throws XMLStreamException
    {
        final CDATASection cDataSection = document.createCDATASection(cdata) ;
        currentElement.appendChild(cDataSection) ;
    }
    
    /**
     * Write the namespace to the stream.
     * @param prefix The namespace prefix.
     * @param uri The namespace URI.
     */
    public void writeNamespace(final String prefix, final String uri)
            throws XMLStreamException
    {
        currentElement.setAttributeNS(XMLNS_NAMESPACE, "xmlns:"+prefix, uri) ;
    }

    /**
     * Write the default namespace to the stream.
     * @param uri The namespace URI.
     */
    public void writeDefaultNamespace(final String uri)
        throws XMLStreamException
    {
        currentElement.setAttributeNS(XMLNS_NAMESPACE, "xmlns", uri) ;
    }
    
    /**
     * Set the current element.
     * @param element The next element.
     */
    private void setCurrentElement(final Element element)
    {
        currentElement.appendChild(element) ;
        currentElement = element ;
        pushNamespaceContext() ;
    }
}
