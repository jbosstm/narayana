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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Sax parser for rewriting the XML via the proxy.
 * @author kevin
 */
public class BaseHandler implements ContentHandler
{
    /**
     * The next handler in the sequence.
     */
    private final ContentHandler nextHandler ;
    
    /**
     * Construct the base handler.
     * @param nextHandler The next content handler.
     */
    protected BaseHandler(final ContentHandler nextHandler)
    {
	this.nextHandler = nextHandler ;
    }
    
    /**
     * Set the document locator.
     * @param locator The document locator.
     */
    public void setDocumentLocator(final Locator locator)
    {
	nextHandler.setDocumentLocator(locator) ;
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
	nextHandler.processingInstruction(target, data) ;
    }
    
    /**
     * Start the document.
     * @throws SAXException for any errors.
     */
    public void startDocument()
    	throws SAXException
    {
	nextHandler.startDocument() ;
    }
    
    /**
     * End the document.
     * @throws SAXException for any errors.
     */
    public void endDocument()
    	throws SAXException
    {
	nextHandler.endDocument() ;
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
	nextHandler.startPrefixMapping(prefix, uri) ;
    }
    
    /**
     * End the prefix mapping.
     * @param prefix The namespace prefix.
     * @throws SAXException for any errors.
     */
    public void endPrefixMapping(final String prefix)
    	throws SAXException
    {
	nextHandler.endPrefixMapping(prefix) ;
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
	nextHandler.startElement(uri, localName, qName, attributes) ;
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
	nextHandler.endElement(uri, localName, qName) ;
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
	nextHandler.characters(chars, start, length) ;
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
	nextHandler.ignorableWhitespace(chars, start, length) ;
    }
    
    /**
     * Skip an entity.
     * @throws SAXException for any errors.
     */
    public void skippedEntity(final String name)
    	throws SAXException
    {
	nextHandler.skippedEntity(name) ;
    }
    
    /**
     * Get the next handler.
     * @return The next handler.
     */
    protected final ContentHandler getNextHandler()
    {
	return nextHandler ;
    }
}
