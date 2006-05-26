/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
package com.arjuna.webservices.soap;

import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Utility methods for SOAP processing.
 * @author kevin
 *
 */
public class SoapUtils
{
    /**
     * The XML input factory.
     */
    private static final XMLInputFactory XML_INPUT_FACTORY = getXMLInputFactory() ;
    /**
     * The XML output factory.
     */
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance() ;
    
    /**
     * Get the XML stream reader.
     * @param reader The input reader.
     * @return The XML stream reader.
     * @throws XMLStreamException For errors obtaining an XML stream reader.
     */
    public static XMLStreamReader getXMLStreamReader(final Reader reader)
        throws XMLStreamException
    {
        return XML_INPUT_FACTORY.createXMLStreamReader(reader) ;
    }
    
    /**
     * Get the XML stream writer.
     * @param writer The output writer.
     * @return The XML stream writer.
     * @throws XMLStreamException For errors obtaining an XML stream writer.
     */
    public static XMLStreamWriter getXMLStreamWriter(final Writer writer)
        throws XMLStreamException
    {
        return XML_OUTPUT_FACTORY.createXMLStreamWriter(writer) ;
    }
    
    /**
     * Create the XML input factory.
     * @return The XML input factory.
     */
    private static XMLInputFactory getXMLInputFactory()
    {
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance() ;
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE) ;
        return xmlInputFactory ;
    }
}
