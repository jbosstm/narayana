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
import org.xml.sax.SAXException;

import com.jboss.transaction.txinterop.proxy.AddressingConstants;
import com.arjuna.webservices.wsarj.ArjunaConstants;

/**
 * Sax parser for rewriting the XML via the proxy.
 * @author kevin
 */
public class AddressingProxySAXHandler extends BaseHandler
{
    /**
     * The WS-Addressing namespace URI.
     */
    private static final String WSA_NAMESPACE_URI = AddressingConstants.WSA_NAMESPACE ;
    /**
     * The WS-Addressing To element.
     */
    private static final String WSA_ELEMENT_TO = AddressingConstants.WSA_ELEMENT_TO ;
    /**
     * The WS-Addressing Address element.
     */
    private static final String WSA_ELEMENT_ADDRESS = AddressingConstants.WSA_ELEMENT_ADDRESS ;
    
    /**
     * Are we in a rewrite element?
     */
    private boolean inRewriteElement ;
    /**
     * Are we in a to element?
     */
    private boolean inToElement ;
    /**
     * Are we in an identifier element?
     */
    private boolean inIdentifierElement ;
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
     * The current conversation identifier.
     */
    private final String conversationIdentifier ;
    
    /**
     * Construct the SAX handler with the output writer.
     * @param nextHandler The next content handler.
     * @param conversationIdentifier The conversation identifier.
     */
    public AddressingProxySAXHandler(final ContentHandler nextHandler, final String conversationIdentifier)
    {
	super(nextHandler) ;
        this.conversationIdentifier = conversationIdentifier ;
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
        if (WSA_NAMESPACE_URI.equals(uri))
        {
            if (WSA_ELEMENT_ADDRESS.equals(localName))
            {
                inRewriteElement = true ;
            }
            else if (WSA_ELEMENT_TO.equals(localName))
            {
                inRewriteElement = true ;
                inToElement = true ;
            }
        }
        else if (ArjunaConstants.WSARJ_NAMESPACE.equals(uri) && ArjunaConstants.WSARJ_ELEMENT_INSTANCE_IDENTIFIER.equals(localName)
            && (attributes.getValue(WSA_NAMESPACE_URI, AddressingConstants.WSA_ATTRIBUTE_IS_REFERENCE_PARAMETER) != null))
        {
            inIdentifierElement = true ;
        }
        
        getNextHandler().startElement(uri, localName, qName, attributes) ;
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
        if (characterContent.length() > 0)
        {
            if (inRewriteElement)
            {
                final String newValue = ProxyURIRewriting.rewriteURI(conversationIdentifier, characterContent.toString().trim()) ;
                getNextHandler().characters(newValue.toCharArray(), 0, newValue.length()) ;
                if (inToElement)
                {
                    toAddress = newValue ;
                    inToElement = false;
                }
                inRewriteElement = false;
            }
            else if (inIdentifierElement)
            {
                identifier = characterContent.toString() ;
                getNextHandler().characters(identifier.toCharArray(), 0, identifier.length()) ;
                inIdentifierElement = false;
            }
            characterContent.setLength(0) ;
        }
        getNextHandler().endElement(uri, localName, qName) ;
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
        if (inRewriteElement || inIdentifierElement)
        {
            characterContent.append(chars, start, length) ;
        }
        else
        {
            getNextHandler().characters(chars, start, length) ;
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
}
