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
package com.arjuna.mw.wst.common;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;

import com.arjuna.webservices.adapters.DOMXMLStreamReader;
import com.arjuna.webservices.adapters.DOMXMLStreamWriter;
import com.arjuna.webservices.adapters.SAAJXMLStreamReader;
import com.arjuna.webservices.adapters.SAAJXMLStreamWriter;
import com.arjuna.webservices.wscoor.CoordinationContextType;

/**
 * Helper class for serialising Coordination Contexts into SOAP headers.
 * @author kevin
 */
public class CoordinationContextHelper
{
    /**
     * Serialise a coordination context to a SAAJ SOAP Header Element.
     * @param env The SOAP envelope.
     * @param headerElement The SOAP header element to populate.
     * @param coordinationContext The coordination context.
     * @throws XMLStreamException for errors during parsing.
     */
    public static void serialise(final SOAPEnvelope env, final SOAPHeaderElement headerElement, final CoordinationContextType coordinationContext)
        throws XMLStreamException
    {
        final XMLStreamWriter out = new SAAJXMLStreamWriter(env, headerElement) ;
        coordinationContext.writeContent(out) ;
    }

    /**
     * Deserialise a coordination context from a SAAJ SOAP Header Element.
     * @param env The SOAP envelope.
     * @param headerElement The SOAP header element to deserialise.
     * @return The coordination context.
     * @throws XMLStreamException for errors during parsing.
     */
    public static CoordinationContextType deserialise(final SOAPEnvelope env, final SOAPHeaderElement headerElement)
        throws XMLStreamException
    {
        final XMLStreamReader in = new SAAJXMLStreamReader(headerElement) ;
        return new CoordinationContextType(in) ;
    }

    /**
     * Serialise a coordination context to a DOM SOAP Header Element.
     * @param headerElement The SOAP header element to populate.
     * @param coordinationContext The coordination context.
     * @throws XMLStreamException for errors during parsing.
     */
    public static void serialise(final Element headerElement, final CoordinationContextType coordinationContext)
        throws XMLStreamException
    {
        final XMLStreamWriter out = new DOMXMLStreamWriter(headerElement) ;
        coordinationContext.writeContent(out) ;
    }

    /**
     * Deserialise a coordination context from a DOM SOAP Header Element.
     * @param headerElement The SOAP header element to deserialise.
     * @return The coordination context.
     * @throws XMLStreamException for errors during parsing.
     */
    public static CoordinationContextType deserialise(final Element headerElement)
        throws XMLStreamException
    {
        final XMLStreamReader in = new DOMXMLStreamReader(headerElement) ;
        return new CoordinationContextType(in) ;
    }
}
