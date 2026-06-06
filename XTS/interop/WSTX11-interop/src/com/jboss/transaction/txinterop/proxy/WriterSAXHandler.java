/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
/*
 * Created on 20-Jan-2005
 */
package com.jboss.transaction.txinterop.proxy;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Sax parser for rewriting the XML via the proxy.
 * @author kevin
 */
public class WriterSAXHandler implements ContentHandler
{
    /**
     * Do we need to end the start tag?
     */
    private boolean endStartTag ;
    /**
     * The to address.
     */
    private String toAddress ;
    /**
     * The identifier.
     */
    private String identifier ;
    /**
     * The string buffer for text element content.
     */
    private StringBuffer characterContent = new StringBuffer() ;
    /**
     * The new namespaces.
     */
    private List newNamespaces = new ArrayList() ;
    /**
     * The stack of active namespaces.
     */
    private Stack namespaceStack = new Stack() ;
    /**
     * The active namespaces.
     */
    private Map activeNamespaces = new HashMap() ;
    
    /**
     * The writer for output.
     */
    private final PrintWriter printWriter ;
    
    /**
     * Construct the SAX handler with the output writer.
     * @param writer The writer for output.
     */
    public WriterSAXHandler(final Writer writer)
    {
        this.printWriter = new PrintWriter(writer) ;
    }
    
    /**
     * Set the document locator.
     * @param locator The document locator.
     */
    public void setDocumentLocator(final Locator locator)
    {
    }
    
    /**
     * Handle the procesing instruction.
     * @param target The pi target.
     * @param data The pi data.
     * @throws SAXException for any errors.
     */
    public void processingInstruction(final String target, final String data)
        throws SAXException
    {
    }
    
    /**
     * Start the document.
     * @throws SAXException for any errors.
     */
    public void startDocument()
    	throws SAXException
    {
    }
    
    /**
     * End the document.
     * @throws SAXException for any errors.
     */
    public void endDocument()
    	throws SAXException
    {
        printWriter.flush() ;
    }
    
    /**
     * Start a prefix mapping.
     * @param prefix The namespace prefix.
     * @param uri The namespace uri.
     * @throws SAXException for any errors.
     */
    public void startPrefixMapping(final String prefix, final String uri)
        throws SAXException
    {
        newNamespaces.add(new NamespaceInfo(prefix, uri)) ;
    }
    
    /**
     * End the prefix mapping.
     * @param prefix The namespace prefix.
     * @throws SAXException for any errors.
     */
    public void endPrefixMapping(final String prefix)
    	throws SAXException
    {
    }
    
    /**
     * Start an element.
     * @param uri The uri.
     * @param localName The local name.
     * @param qName The qualified name.
     * @param attributes The element attributes.
     * @throws SAXException for any errors.
     */
    public void startElement(final String uri, final String localName, final String qName,
        final Attributes attributes)
    	throws SAXException
    {
        checkEndStartTag() ;
        
        namespaceStack.push(activeNamespaces) ;
        final Iterator newNamespacesIter = newNamespaces.iterator() ;
        final String namespaces ;
        if (newNamespacesIter.hasNext())
        {
            final HashMap newActiveNamespaces = new HashMap(activeNamespaces) ;
            final StringBuffer namespacesValue = new StringBuffer() ;
            do
            {
                final NamespaceInfo namespaceInfo = (NamespaceInfo)newNamespacesIter.next() ;
                final String namespacePrefix = namespaceInfo.getPrefix() ;
                final String namespaceURI = namespaceInfo.getURI() ;
                newActiveNamespaces.put(namespaceURI, namespacePrefix) ;
               
                namespacesValue.append(" xmlns") ;
                if ((namespacePrefix != null) && (namespacePrefix.length() > 0))
                {
                    namespacesValue.append(':') ;
                    namespacesValue.append(namespacePrefix) ;
                }
                namespacesValue.append("=\"") ;
                namespacesValue.append(namespaceURI) ;
                namespacesValue.append('"') ;
            }
            while(newNamespacesIter.hasNext()) ;
            newNamespaces.clear() ;
            activeNamespaces = newActiveNamespaces ;
            namespaces = namespacesValue.toString() ;
        }
        else
        {
            namespaces = null ;
        }
        
        printWriter.write('<') ;
        printWriter.write(getQName(uri, localName, qName)) ;
        if (namespaces != null)
        {
            printWriter.write(namespaces) ;
        }
        
        final int numAttributes = attributes.getLength() ;
        if (numAttributes > 0)
        {
            for(int count = 0 ; count < numAttributes ; count++)
            {
                printWriter.write(' ') ;
                final String attributeQName = getQName(attributes.getURI(count), attributes.getLocalName(count), attributes.getQName(count)) ;
                printWriter.write(attributeQName) ;
                printWriter.write("=\"") ;
                final String value = attributes.getValue(count) ;
                escapeTextContent(value.toCharArray(), 0, value.length()) ;
                printWriter.write('"') ;
            }
        }
        
        endStartTag = true ;
    }
    
    /**
     * End an element.
     * @param uri The uri.
     * @param localName The local name.
     * @param qName The qualified name.
     * @throws SAXException for any errors.
     */
    public void endElement(final String uri, final String localName, final String qName)
        throws SAXException
    {
        characterContent.setLength(0) ;
        if (endStartTag)
        {
            printWriter.write("/>") ;
        }
        else
        {
            printWriter.write("</") ;
            printWriter.write(qName) ;
            printWriter.write('>') ;
        }
        activeNamespaces = (Map)namespaceStack.pop() ;
        endStartTag = false ;
    }
    
    /**
     * Process character text.
     * @param chars The character array.
     * @param start The start index.
     * @param length The length of this section.
     * @throws SAXException for any errors.
     */
    public void characters(char[] chars, int start, int length)
        throws SAXException
    {
        checkEndStartTag() ;
        escapeTextContent(chars, start, length) ;
    }
    
    /**
     * Process ignorable white space.
     * @param chars The character array.
     * @param start The start index.
     * @param length The length of this section.
     * @throws SAXException for any errors.
     */
    public void ignorableWhitespace(char[] chars, int start, int length)
        throws SAXException
    {
        checkEndStartTag() ;
        printWriter.write(chars, start, length) ;
    }
    
    /**
     * Skip an entity.
     * @throws SAXException for any errors.
     */
    public void skippedEntity(final String name)
    	throws SAXException
    {
    }
    
    /**
     * Get the qualified name of the element/attribute.
     * @param uri The qualifed namespace uri or empty string.
     * @param localName The qualified local name.
     * @param qName The qualified name.
     * @return The qualified name of the element/attribute.
     */
    private String getQName(final String uri, final String localName, final String qName)
    {
        if (uri.length() > 0)
        {
            final String prefix = (String)activeNamespaces.get(uri) ;
            if (prefix.length() > 0)
            {
                return prefix + ":" + localName ;
            }
            else
            {
                return localName ;
            }
        }
        else
        {
            return qName ;
        }
    }
    
    /**
     * Check to see if the end of the start tag has been processed.
     */
    private void checkEndStartTag()
    {
        if (endStartTag)
        {
            printWriter.write('>') ;
            endStartTag = false ;
        }
    }
    
    /**
     * Escape the contents of a text element.
     * @param chars The character array.
     * @param start The start index.
     * @param length The length of this section.
     */
    private void escapeTextContent(final char[] chars, final int start, final int length)
    {
        final int end = start + length ;
        for(int count = start ; count < end ; count++)
        {
            final char ch = chars[count] ;
            switch(ch)
            {
                case '<':
                    printWriter.print("&lt;") ;
                    break ;
                case '>':
                    printWriter.print("&gt;") ;
                    break ;
                case '&':
                    printWriter.print("&amp;") ;
                    break ;
                case '"':
                    printWriter.print("&quot;") ;
                    break ;
                default:
                    printWriter.print(ch) ;
                    break ;
            }
        }
    }
    
    /**
     * Return the to address from the processing.
     * @return The to address.
     */
    public String getToAddress()
    {
        return toAddress ;
    }
    
    /**
     * Return the identifier from the processing.
     * @return The identifier.
     */
    public String getIdentifier()
    {
        return identifier ;
    }
    
    private static final class NamespaceInfo
    {
        /**
         * The namespace prefix.
         */
        private final String prefix ;
        /**
         * The namespace uri.
         */
        private final String uri ;
        
        /**
         * Construct the namespace information.
         * @param prefix The namespace prefix.
         * @param uri The namespace uri.
         */
        NamespaceInfo(final String prefix, final String uri)
        {
            this.prefix = prefix ;
            this.uri = uri ;
        }
        
        /**
         * Get the namespace prefix.
         * @return The namespace prefix.
         */
        String getPrefix()
        {
            return prefix ;
        }
        
        /**
         * Get the namespace URI.
         * @return The namespace URI.
         */
        String getURI()
        {
            return uri ;
        }
    }
}
