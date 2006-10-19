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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.stax.NamedElement;

/**
 * The interface representing SOAP specific processing.
 * @author kevin
 */
public interface SoapDetails
{
    /**
     * The version string for SOAP 1.1.
     */
    public static String SOAP_11_VERSION = "1.1" ;
    
    /**
     * The version string for SOAP 1.2.
     */
    public static String SOAP_12_VERSION = "1.2" ;
    
    /**
     * Get the SOAP version.
     * @return The SOAP version
     */
    public String getVersion() ;
    
    /**
     * Get the SOAP namespace URI.
     * @return The SOAP namespace URI.
     */
    public String getNamespaceURI() ;
    
    /**
     * Get the local name of the role attribute.
     * @return The role local name,
     */
    public String getRoleLocalName() ;
    
    /**
     * Get the qualified name of the role attribute.
     * @return The role qualified name,
     */
    public QName getRoleQName() ;
    
    /**
     * Get the SOAP name for the next role.
     * @return The name of the next role.
     */
    public String getNextRole() ;
    
    /**
     * Get the SOAP name for the last role.
     * @return The name of the last role.
     */
    public String getLastRole() ;
    
    /**
     * Get the qualified SOAP Fault element name.
     * @return The qualified SOAP Fault name.
     */
    public QName getFaultName() ;
    
    /**
     * Get the qualified name of the must understand attribute.
     * @return The must understand qualified name.
     */
    public QName getMustUnderstandQName() ;
    
    /**
     * Get the value of the must understand attribute.
     * @return The must understand value.
     */
    public String getMustUnderstandValue() ;
    
    /**
     * Write SOAP fault headers to the stream.
     * @param streamWriter The output stream.
     * @param soapFault The soap fault.
     * @throws XMLStreamException For errors during writing.
     */
    public void writeSoapFaultHeaders(final XMLStreamWriter streamWriter, final SoapFault soapFault)
        throws XMLStreamException ;
    
    /**
     * Write a SOAP fault to the stream.
     * @param streamWriter The output stream.
     * @param soapFault The soap fault.
     * @throws XMLStreamException For errors during writing.
     */
    public void writeSoapFault(final XMLStreamWriter streamWriter, final SoapFault soapFault)
        throws XMLStreamException ;
    
    /**
     * Write a header SOAP fault headers to the stream.
     * @param streamWriter The output stream.
     * @param soapFault The soap fault.
     * @throws XMLStreamException For errors during writing.
     */
    public void writeHeaderSoapFaultHeaders(final XMLStreamWriter streamWriter, final SoapFault soapFault)
        throws XMLStreamException ;
    
    /**
     * Write a header SOAP fault to the stream.
     * @param streamWriter The output stream.
     * @param soapFault The soap fault.
     * @throws XMLStreamException For errors during writing.
     */
    public void writeHeaderSoapFault(final XMLStreamWriter streamWriter, final SoapFault soapFault)
        throws XMLStreamException ;
    
    /**
     * Parse a SOAP fault from the stream.
     * @param streamReader The input stream.
     * @return The soap fault.
     * @throws XMLStreamException For errors during reading.
     */
    public SoapFault parseSoapFault(final XMLStreamReader streamReader)
        throws XMLStreamException ;

    /**
     * Get the headers passed with a MustUnderstand fault.
     * @param headerName The name of the header that cannot be processed.
     * @return The headers or null if none required.
     */
    public NamedElement[] getMustUnderstandHeaders(final QName headerName) ;
}
