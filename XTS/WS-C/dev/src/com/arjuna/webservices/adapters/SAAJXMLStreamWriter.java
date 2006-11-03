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

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.stream.XMLStreamException;

import com.arjuna.webservices.logging.WSCLogger;


/**
 * XMLStreamWriter for writing to a SAAJ tree.
 * @author kevin
 * @message com.arjuna.webservices.adapters.SAAJXMLStreamWriter_1 [com.arjuna.webservices.adapters.SAAJXMLStreamWriter_1] - CData sections not currently supported.
 */
public class SAAJXMLStreamWriter extends BaseXMLStreamWriter
{
    /**
     * The SOAP envelope.
     */
    private final SOAPEnvelope env ;
    /**
     * The current element to populate.
     */
    private SOAPElement currentElement ;
    
    /**
     * Construct the SAAJ XMLStreamWriter.
     * @param env The SOAP envelope.
     * @param headerElement The header element to populate.
     */
    public SAAJXMLStreamWriter(final SOAPEnvelope env, final SOAPHeaderElement headerElement)
    {
        this.env = env ;
        this.currentElement = headerElement ;
    }

    /**
     * Write a start element.
     * @param localName The local name of the tag.
     */
    public void writeStartElement(final String localName)
        throws XMLStreamException
    {
        try
        {
            currentElement = currentElement.addChildElement(localName) ;
        }
        catch (final SOAPException soapException)
        {
            throw new XMLStreamException(soapException) ;
        }
        pushNamespaceContext() ;
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
        try
        {
            currentElement = currentElement.addChildElement(localName, prefix, namespaceURI) ;
        }
        catch (final SOAPException soapException)
        {
            throw new XMLStreamException(soapException) ;
        }
        pushNamespaceContext() ;
    }

    /**
     * Write an empty element.
     * @param localName The local name of the tag.
     */
    public void writeEmptyElement(final String localName)
        throws XMLStreamException
    {
        try
        {
            currentElement.addChildElement(localName) ;
        }
        catch (final SOAPException soapException)
        {
            throw new XMLStreamException(soapException) ;
        }
    }

    /**
     * Write an empty element with the specified namespace URI.
     * @param namespaceURI The namespace URI of the tag.
     * @param localName The local name of the tag.
     */
    public void writeEmptyElement(final String namespaceURI, final String localName)
        throws XMLStreamException
    {
        final String prefix = getNamespaceContext().getPrefix(normaliseNamespace(namespaceURI)) ;
        writeEmptyElement(prefix, localName, namespaceURI) ;
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
        try
        {
            currentElement.addChildElement(localName, prefix, namespaceURI) ;
        }
        catch (final SOAPException soapException)
        {
            throw new XMLStreamException(soapException) ;
        }
    }

    /**
     * Write an end element.
     */
    public void writeEndElement()
        throws XMLStreamException
    {
        currentElement = currentElement.getParentElement() ;
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
        try
        {
            final Name name = env.createName(localName) ;
            currentElement.addAttribute(name, value) ;
        }
        catch (final SOAPException soapException)
        {
            throw new XMLStreamException(soapException) ;
        }
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
        final String prefix = getNamespaceContext().getPrefix(normaliseNamespace(namespaceURI)) ;
        writeAttribute(prefix, namespaceURI, localName, value) ;
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
        try
        {
            final Name name = env.createName(localName, prefix, namespaceURI) ;
            currentElement.addAttribute(name, value) ;
        }
        catch (final SOAPException soapException)
        {
            throw new XMLStreamException(soapException) ;
        }
    }

    /**
     * Write a text section.
     * @param text The text to write.
     */
    public void writeCharacters(final String text)
        throws XMLStreamException
    {
        try
        {
            currentElement.addTextNode(text) ;
        }
        catch (final SOAPException soapException)
        {
            throw new XMLStreamException(soapException) ;
        }
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
        throw new XMLStreamException(WSCLogger.log_mesg.getString("com.arjuna.webservices.adapters.SAAJXMLStreamWriter_1")) ;
    }
    
    /**
     * Write the namespace to the stream.
     * @param prefix The namespace prefix.
     * @param uri The namespace URI.
     */
    public void writeNamespace(final String prefix, final String uri)
            throws XMLStreamException
    {
        try
        {
            currentElement.addNamespaceDeclaration(prefix, uri) ;
        }
        catch (final SOAPException soapException)
        {
            throw new XMLStreamException(soapException) ;
        }
    }

    /**
     * Write the default namespace to the stream.
     * @param uri The namespace URI.
     */
    public void writeDefaultNamespace(final String uri)
        throws XMLStreamException
    {
        writeNamespace(null, uri) ;
    }
}
