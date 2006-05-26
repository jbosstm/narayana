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

import java.text.MessageFormat;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyElement;
import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.stax.TextElement;
import com.arjuna.webservices.util.QNameHelper;
import com.arjuna.webservices.util.StreamHelper;


/**
 * The class implementing SOAP 1.2 specific processing.
 * @author kevin
 * 
 * @message com.arjuna.webservices.soap.Soap12Details_1 [com.arjuna.webservices.soap.Soap12Details_1] - Unexpected start element: {0}
 */
public class Soap12Details implements SoapDetails
{
    /**
     * The SOAP 1.2 namespace.
     */
    private final static String NAMESPACE_URI = "http://www.w3.org/2003/05/soap-envelope" ;
    /**
     * The SOAP 1.2 local name of the role attribute.
     */
    private static final String ROLE_LOCAL_NAME = "role" ;
    /**
     * The qualified SOAP 1.2 role attribute. 
     */
    private final static QName QNAME_ATTRIBUTE_ROLE = getQName(ROLE_LOCAL_NAME) ;
    /**
     * The SOAP 1.2 next role.
     */
    private final static String NEXT_ROLE = NAMESPACE_URI + "/role/next" ;
    /**
     * The SOAP 1.2 none role.
     */
    private final static String NONE_ROLE = NAMESPACE_URI + "/role/none" ;
    /**
     * The SOAP 1.2 last role.
     */
    private final static String LAST_ROLE = NAMESPACE_URI + "/role/ultimateReceiver" ;
    
    /**
     * The qualified SOAP 1.2 MustUnderstand name.
     */
    private final static QName QNAME_MUST_UNDERSTAND = getQName("mustUnderstand") ;
    /**
     * The qualified SOAP 1.2 Fault name. 
     */
    private final static QName QNAME_FAULT = getQName("Fault") ;
    /**
     * The qualified SOAP 1.2 Code name. 
     */
    private final static QName QNAME_CODE = getQName("Code") ;
    /**
     * The qualified SOAP 1.2 Reason name. 
     */
    private final static QName QNAME_REASON = getQName("Reason") ;
    /**
     * The qualified SOAP 1.2 Node name. 
     */
    private final static QName QNAME_NODE = getQName("Node") ;
    /**
     * The qualified SOAP 1.2 Role name. 
     */
    private final static QName QNAME_ROLE = getQName("Role") ;
    /**
     * The qualified SOAP 1.2 Detail name. 
     */
    private final static QName QNAME_DETAIL = getQName("Detail") ;
    /**
     * The qualified SOAP 1.2 Value name. 
     */
    private final static QName QNAME_VALUE = getQName("Value") ;
    /**
     * The qualified SOAP 1.2 Subcode name. 
     */
    private final static QName QNAME_SUBCODE = getQName("Subcode") ;
    /**
     * The qualified SOAP 1.2 Text name. 
     */
    private final static QName QNAME_TEXT = getQName("Text") ;
    /**
     * The qualified SOAP 1.2 NotUnderstood name.
     */
    private final static QName QNAME_NOT_UNDERSTOOD = getQName("NotUnderstood") ;
    
    /**
     * The XML Lang attribute.
     */
    private final static QName QNAME_XML_LANG = new QName("http://www.w3.org/XML/1998/namespace", "lang", "xml") ;
    
    /**
     * The Fault Code for version mismatch.
     */
    private final static QName FAULT_CODE_VERSION_MISMATCH = getQName("VersionMismatch") ;
    /**
     * The Fault Code for must understand.
     */
    private final static QName FAULT_CODE_MUST_UNDERSTAND = getQName("MustUnderstand") ;
    /**
     * The Fault Code for data encoding unknown.
     */
    private final static QName FAULT_CODE_DATA_ENCODING_UNKNOWN = getQName("DataEncodingUnknown") ;
    /**
     * The Fault Code for sender.
     */
    private final static QName FAULT_CODE_SENDER = getQName("Sender") ;
    /**
     * The Fault Code for receiver.
     */
    private final static QName FAULT_CODE_RECEIVER = getQName("Receiver") ;
    
    /**
     * Get the SOAP version.
     * @return The SOAP version
     */
    public String getVersion()
    {
        return SOAP_12_VERSION ;
    }
    
    /**
     * Get the SOAP namespace URI.
     * @return The SOAP namespace URI.
     */
    public String getNamespaceURI()
    {
        return NAMESPACE_URI ;
    }
    
    /**
     * Get the local name of the role attribute.
     * @return The role local name,
     */
    public String getRoleLocalName()
    {
        return ROLE_LOCAL_NAME ;
    }
    
    /**
     * Get the qualified name of the role attribute.
     * @return The role qualified name,
     */
    public QName getRoleQName()
    {
        return QNAME_ATTRIBUTE_ROLE ;
    }
    
    /**
     * Get the SOAP name for the next role.
     * @return The name of the next role.
     */
    public String getNextRole()
    {
        return NEXT_ROLE ;
    }
    
    /**
     * Get the SOAP name for the none role.
     * @return The name of the none role.
     */
    public String getNoneRole()
    {
        return NONE_ROLE ;
    }
    
    /**
     * Get the SOAP name for the last role.
     * @return The name of the last role.
     */
    public String getLastRole()
    {
        return LAST_ROLE ;
    }
    
    /**
     * Get the qualified SOAP Fault element name.
     * @return The qualified SOAP Fault name.
     */
    public QName getFaultName()
    {
        return QNAME_FAULT ;
    }
    
    /**
     * Get the qualified name of the must understand attribute.
     * @return The must understand qualified name,
     */
    public QName getMustUnderstandQName()
    {
        return QNAME_MUST_UNDERSTAND ;
    }
    
    /**
     * Write SOAP fault headers to the stream.
     * @param streamWriter The output stream.
     * @param soapFault The soap fault.
     * @throws XMLStreamException For errors during writing.
     */
    public void writeSoapFaultHeaders(final XMLStreamWriter streamWriter, final SoapFault soapFault)
        throws XMLStreamException
    {
        writeSoapFaultHeaders(streamWriter, soapFault, false) ;
    }
    
    /**
     * Write a SOAP fault to the stream.
     * @param streamWriter The output stream.
     * @param soapFault The SOAP fault.
     * @throws XMLStreamException For errors during writing.
     */
    public void writeSoapFault(final XMLStreamWriter streamWriter, final SoapFault soapFault)
        throws XMLStreamException
    {
        writeSoapFault(streamWriter, soapFault, false) ;
    }
    
    /**
     * Write a header SOAP fault headers to the stream.
     * @param streamWriter The output stream.
     * @param soapFault The soap fault.
     * @throws XMLStreamException For errors during writing.
     */
    public void writeHeaderSoapFaultHeaders(final XMLStreamWriter streamWriter, final SoapFault soapFault)
        throws XMLStreamException
    {
        writeSoapFaultHeaders(streamWriter, soapFault, true) ;
    }
    
    /**
     * Write a header SOAP fault to the stream.
     * @param streamWriter The output stream.
     * @param soapFault The SOAP fault.
     * @throws XMLStreamException For errors during writing.
     */
    public void writeHeaderSoapFault(final XMLStreamWriter streamWriter, final SoapFault soapFault)
        throws XMLStreamException
    {
        writeSoapFault(streamWriter, soapFault, true) ;
    }
    
    /**
     * Parse a SOAP fault from the stream.
     * @param streamReader The input stream.
     * @return The SOAP fault.
     * @throws XMLStreamException For errors during reading.
     */
    public SoapFault parseSoapFault(final XMLStreamReader streamReader)
        throws XMLStreamException
    {
            final SoapFault soapFault = new SoapFault() ;
            
            // Code processing
            StreamHelper.checkNextStartTag(streamReader, QNAME_CODE) ;
            StreamHelper.checkNextStartTag(streamReader, QNAME_VALUE) ;
            final TextElement codeElement = new TextElement(streamReader) ;
            final QName code = QNameHelper.toQName(streamReader.getNamespaceContext(), codeElement.getText()) ;
            soapFault.setSoapFaultType(getSoapFaultType(code)) ;
            if (!StreamHelper.checkParentFinished(streamReader))
            {
                StreamHelper.checkTag(streamReader, QNAME_SUBCODE) ;
                StreamHelper.checkNextStartTag(streamReader, QNAME_VALUE) ;
                final TextElement subcodeElement = new TextElement(streamReader) ;
                soapFault.setSubcode(QNameHelper.toQName(streamReader.getNamespaceContext(), subcodeElement.getText())) ;
                if (!StreamHelper.checkParentFinished(streamReader))
                {
                    StreamHelper.checkTag(streamReader, QNAME_SUBCODE) ;
                    StreamHelper.checkNextStartTag(streamReader, QNAME_VALUE) ;
                    final TextElement subSubcodeElement = new TextElement(streamReader) ;
                    soapFault.setSubSubcode(QNameHelper.toQName(streamReader.getNamespaceContext(), subSubcodeElement.getText())) ;
                    if (!StreamHelper.checkParentFinished(streamReader))
                    {
                        // Throw away any subcodes as we only support two level.
                        new AnyElement(streamReader) ;
                        if (!StreamHelper.checkParentFinished(streamReader))
                        {
                            final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.soap.Soap12Details_1") ;
                            final String message = MessageFormat.format(pattern, new Object[] {streamReader.getName()}) ;
                            throw new XMLStreamException(message) ;
                        }
                    }
                    if (!StreamHelper.checkParentFinished(streamReader))
                    {
                        final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.soap.Soap12Details_1") ;
                        final String message = MessageFormat.format(pattern, new Object[] {streamReader.getName()}) ;
                        throw new XMLStreamException(message) ;
                    }
                }
                if (!StreamHelper.checkParentFinished(streamReader))
                {
                    final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.soap.Soap12Details_1") ;
                    final String message = MessageFormat.format(pattern, new Object[] {streamReader.getName()}) ;
                    throw new XMLStreamException(message) ;
                }
            }
            
            // Reason processing
            StreamHelper.checkNextStartTag(streamReader, QNAME_REASON) ;
            StreamHelper.checkNextStartTag(streamReader, QNAME_TEXT) ;
            final TextElement reasonElement = new TextElement(streamReader) ;
            soapFault.setReason(reasonElement.getText()) ;
            if (!StreamHelper.checkParentFinished(streamReader))
            {
                final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.soap.Soap12Details_1") ;
                final String message = MessageFormat.format(pattern, new Object[] {streamReader.getName()}) ;
                throw new XMLStreamException(message) ;
            }
            if (StreamHelper.checkParentFinished(streamReader))
            {
                return soapFault ;
            }
            
            // Node if present
            if (QNAME_NODE.equals(streamReader.getName()))
            {
                final TextElement nodeElement = new TextElement(streamReader) ;
                soapFault.setNode(nodeElement.getText()) ;
                if (StreamHelper.checkParentFinished(streamReader))
                {
                    return soapFault ;
                }
            }
            // Role if present
            if (QNAME_ROLE.equals(streamReader.getName()))
            {
                final TextElement roleElement = new TextElement(streamReader) ;
                soapFault.setRole(roleElement.getText()) ;
                if (StreamHelper.checkParentFinished(streamReader))
                {
                    return soapFault ;
                }
            }
            
            // Detail if present
            if (QNAME_DETAIL.equals(streamReader.getName()))
            {
                soapFault.setDetail(new NamedElement(null, new AnyElement(streamReader))) ;
                if (StreamHelper.checkParentFinished(streamReader))
                {
                    return soapFault ;
                }
            }
            final String pattern = WSCLogger.log_mesg.getString("com.arjuna.webservices.soap.Soap12Details_1") ;
            final String message = MessageFormat.format(pattern, new Object[] {streamReader.getName()}) ;
            throw new XMLStreamException(message) ;
    }

    /**
     * Get the headers passed with a MustUnderstand fault.
     * @param headerName The name of the header that cannot be processed.
     * @return The headers or null if none required.
     */
    public NamedElement[] getMustUnderstandHeaders(final QName headerName)
    {
        final ElementContent notUnderstoodElement = new SoapNotUnderstoodType(headerName) ;
        final NamedElement header = new NamedElement(QNAME_NOT_UNDERSTOOD, notUnderstoodElement) ;
        return new NamedElement[] {header} ;
    }
    
    /**
     * Write SOAP fault headers to the stream.
     * @param streamWriter The output stream.
     * @param soapFault The SOAP fault.
     * @param isHeaderFault true if the SOAP fault is generated during header processing.
     * @throws XMLStreamException For errors during writing.
     */
    private void writeSoapFaultHeaders(final XMLStreamWriter streamWriter, final SoapFault soapFault, final boolean isHeaderFault)
        throws XMLStreamException
    {
        final NamedElement[] headerElements = soapFault.getHeaderElements() ;
        final int numHeaderElements = (headerElements == null ? 0 : headerElements.length) ;
        for(int count = 0 ; count < numHeaderElements ; count++)
        {
            final NamedElement namedElement = headerElements[count] ;
            final QName headerElementName = namedElement.getName() ;
            final String namespaceURI = StreamHelper.writeStartElement(streamWriter, headerElementName) ;
            namedElement.getElementContent().writeContent(streamWriter) ;
            StreamHelper.writeEndElement(streamWriter, headerElementName.getPrefix(), namespaceURI) ;
        }
    }
    
    /**
     * Write a SOAP fault to the stream.
     * @param streamWriter The output stream.
     * @param soapFault The SOAP fault.
     * @param isHeaderFault true if the SOAP fault is generated during header processing.
     * @throws XMLStreamException For errors during writing.
     */
    private void writeSoapFault(final XMLStreamWriter streamWriter, final SoapFault soapFault, final boolean isHeaderFault)
        throws XMLStreamException
    {
        final String namespaceURI = StreamHelper.writeStartElement(streamWriter, QNAME_FAULT) ;
        
        // Code output
        StreamHelper.writeStartElement(streamWriter, QNAME_CODE) ;
        
        StreamHelper.writeStartElement(streamWriter, QNAME_VALUE) ;
        StreamHelper.writeQualifiedName(streamWriter, getSoapFaultCodeQName(soapFault)) ;
        StreamHelper.writeEndElement(streamWriter, null, null) ;
        
        final QName subcode = soapFault.getSubcode() ;
        if (subcode != null)
        {
            StreamHelper.writeStartElement(streamWriter, QNAME_SUBCODE) ;
            StreamHelper.writeStartElement(streamWriter, QNAME_VALUE) ;
            StreamHelper.writeQualifiedName(streamWriter, subcode) ;
            StreamHelper.writeEndElement(streamWriter, null, null) ;
            
            final QName subSubcode = soapFault.getSubSubcode() ;
            if (subSubcode != null)
            {
                StreamHelper.writeStartElement(streamWriter, QNAME_SUBCODE) ;
                StreamHelper.writeStartElement(streamWriter, QNAME_VALUE) ;
                StreamHelper.writeQualifiedName(streamWriter, subSubcode) ;
                StreamHelper.writeEndElement(streamWriter, null, null) ;
                StreamHelper.writeEndElement(streamWriter, null, null) ;
            }
            StreamHelper.writeEndElement(streamWriter, null, null) ;
        }
        
        StreamHelper.writeEndElement(streamWriter, null, null) ;
        
        // Reason output
        StreamHelper.writeStartElement(streamWriter, QNAME_REASON) ;
        StreamHelper.writeStartElement(streamWriter, QNAME_TEXT) ;
        StreamHelper.writeAttribute(streamWriter, QNAME_XML_LANG, Locale.getDefault().getLanguage()) ;
        streamWriter.writeCharacters(soapFault.getReason()) ;
        StreamHelper.writeEndElement(streamWriter, null, null) ;
        StreamHelper.writeEndElement(streamWriter, null, null) ;
        
        // Node output
        final String node = soapFault.getNode() ;
        if (node != null)
        {
            StreamHelper.writeStartElement(streamWriter, QNAME_NODE) ;
            streamWriter.writeCharacters(node) ;
            StreamHelper.writeEndElement(streamWriter, null, null) ;
        }
        
        // Role output
        final String role = soapFault.getNode() ;
        if (role != null)
        {
            StreamHelper.writeStartElement(streamWriter, QNAME_ROLE) ;
            streamWriter.writeCharacters(role) ;
            StreamHelper.writeEndElement(streamWriter, null, null) ;
        }
        
        // Detail output
        final NamedElement detail = soapFault.getDetail() ;
        if (detail != null)
        {
            StreamHelper.writeStartElement(streamWriter, QNAME_DETAIL) ;
            final ElementContent detailContent = detail.getElementContent() ;
            detailContent.writeContent(streamWriter) ;
            StreamHelper.writeEndElement(streamWriter, null, null) ;
        }
        
        StreamHelper.writeEndElement(streamWriter, QNAME_FAULT.getPrefix(), namespaceURI) ;
    }
    
    /**
     * Get the code qualified name for the SOAP fault.
     * @param soapFault The SOAP fault.
     * @return The qualified name.
     */
    private QName getSoapFaultCodeQName(final SoapFault soapFault)
    {
        final SoapFaultType soapFaultType = soapFault.getSoapFaultType() ;
        if (soapFaultType == SoapFaultType.FAULT_SENDER)
        {
            return FAULT_CODE_SENDER ;
        }
        else if (soapFaultType == SoapFaultType.FAULT_RECEIVER)
        {
            return FAULT_CODE_RECEIVER ;
        }
        else if (soapFaultType == SoapFaultType.FAULT_MUST_UNDERSTAND)
        {
            return FAULT_CODE_MUST_UNDERSTAND ;
        }
        else if (soapFaultType == SoapFaultType.FAULT_VERSION_MISMATCH)
        {
            return FAULT_CODE_VERSION_MISMATCH ;
        }
        else
        {
            return FAULT_CODE_DATA_ENCODING_UNKNOWN ;
        }
    }

    /**
     * Get the SOAP fault type corresponding to the qualified name.
     * @param code The qualified name.
     * @return The soap fault type.
     */
    private SoapFaultType getSoapFaultType(final QName code)
    {
        if (FAULT_CODE_SENDER.equals(code))
        {
            return SoapFaultType.FAULT_SENDER ;
        }
        else if (FAULT_CODE_RECEIVER.equals(code))
        {
            return SoapFaultType.FAULT_RECEIVER ;
        }
        else if (FAULT_CODE_MUST_UNDERSTAND.equals(code))
        {
            return SoapFaultType.FAULT_MUST_UNDERSTAND ;
        }
        else if (FAULT_CODE_VERSION_MISMATCH.equals(code))
        {
            return SoapFaultType.FAULT_VERSION_MISMATCH ;
        }
        else
        {
            return SoapFaultType.FAULT_DATA_ENCODING_UNKNOWN ;
        }
    }
    
    /**
     * Convenience for generating SOAP qualified names.
     * @param localName The local name.
     * @return The qualified name.
     */
    private static QName getQName(final String localName)
    {
        return new QName(NAMESPACE_URI, localName, SoapConstants.SOAP_PREFIX) ;
    }
}
