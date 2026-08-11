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
package com.jboss.transaction.txinterop.interop.states;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.jboss.transaction.txinterop.proxy.BaseHandler;


/**
 * A conversation state for unsolicited complete.
 */
public class BAInteropUnsolicitedCompleteState extends InteropWaitState
{
    /**
     * The replaced flag.
     */
    private boolean replaced ;
    
    /**
     * Construct the unsolicited complete state.
     */
    public BAInteropUnsolicitedCompleteState()
    {
	super(CoordinationConstants.WSCOOR_ACTION_FAULT) ;
    }
    
    /**
     * Get the Handler for rewriting the XML.
     * @param nextHandler The next handler in the sequence.
     * @return The handler or null if no rewriting required.
     */
    public ContentHandler getHandler(final ContentHandler nextHandler)
    {
	return (replaced ? null : new RegistrationHandler(nextHandler)) ;
    }
    
    /**
     * The registration handler.
     */
    private final class RegistrationHandler extends BaseHandler
    {
        /**
         * The 'in register' flag.
         */
        private boolean inRegister ;
        /**
         * The 'in protocol identifier' flag.
         */
        private boolean inProtocolIdentifier ;
        
        /**
         * Construct the registration handler.
         * @param nextHandler The next handler.
         */
	RegistrationHandler(final ContentHandler nextHandler)
	{
	    super(nextHandler) ;
	}
	
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
	    throws SAXException
	{
	    if (CoordinationConstants.WSCOOR_NAMESPACE.equals(uri) && CoordinationConstants.WSCOOR_ELEMENT_REGISTER.equals(localName))
	    {
		inRegister = true ;
	    }
	    else if (inRegister && CoordinationConstants.WSCOOR_NAMESPACE.equals(uri) && CoordinationConstants.WSCOOR_ELEMENT_PROTOCOL_IDENTIFIER.equals(localName))
	    {
		inProtocolIdentifier = true ;
	    }
	    super.startElement(uri, localName, qName, attributes);
	}
	
	public void endElement(final String uri, final String localName, final String qName)
	    throws SAXException
	{
	    if (inProtocolIdentifier && CoordinationConstants.WSCOOR_NAMESPACE.equals(uri) && CoordinationConstants.WSCOOR_ELEMENT_PROTOCOL_IDENTIFIER.equals(localName))
	    {
		inProtocolIdentifier = false ;
		final char[] chars = BusinessActivityConstants.WSBA_SUB_PROTOCOL_COORDINATOR_COMPLETION.toCharArray() ;
		getNextHandler().characters(chars, 0, chars.length) ;
	    }
	    else if (inRegister && CoordinationConstants.WSCOOR_NAMESPACE.equals(uri) && CoordinationConstants.WSCOOR_ELEMENT_REGISTER.equals(localName))
	    {
		inRegister = false ;
	    }
	    super.endElement(uri, localName, qName);
	}
	
	public void characters(final char[] chars, final int start, final int length)
	    throws SAXException
	{
	    if (!inProtocolIdentifier)
	    {
		super.characters(chars, start, length);
	    }
	}
	
    }
}
