/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
package com.arjuna.webservices.soap;

import java.text.MessageFormat;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyElement;
import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.stax.TextElement;
import com.arjuna.webservices.util.QNameHelper;
import com.arjuna.webservices.util.StreamHelper;


/**
 * The class implementing SOAP 1.1 specific processing.
 * @author kevin
 */
public class Soap11Details implements SoapDetails
{
    /**
     * The SOAP 1.1 namespace.
     */
    private static final String NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/" ;
    /**
     * The SOAP 1.1 local name of the role attribute.
     */
    private static final String ROLE_LOCAL_NAME = "actor" ;
    /**
     * The qualified SOAP 1.1 role attribute. 
     */
    private final static QName QNAME_ATTRIBUTE_ROLE = getQName(ROLE_LOCAL_NAME) ;
    /**
     * The SOAP 1.1 next role.
     */
    private static final String NEXT_ROLE = NAMESPACE_URI + "http://schemas.xmlsoap.org/soap/actor/next" ;
    /**
     * The SOAP 1.1 last role.
     */
    private static final String LAST_ROLE = null ;
    /**
     * The SOAP 1.1 must understand value.
     */
    private final static String MUST_UNDERSTAND_VALUE = "1" ;
    
    /**
     * The qualified SOAP 1.1 MustUnderstand name.
     */
    private final static QName QNAME_MUST_UNDERSTAND = getQName("mustUnderstand") ;
    /**
     * The qualified SOAP 1.1 Fault name. 
     */
    private final static QName QNAME_FAULT = getQName("Fault") ;
    /**
     * The qualified SOAP 1.1 faultcode name. 
     */
    private final static QName QNAME_FAULTCODE = new QName("faultcode") ;
    /**
     * The qualified SOAP 1.1 faultstring name. 
     */
    private final static QName QNAME_FAULTSTRING = new QName("faultstring") ;
    /**
     * The qualified SOAP 1.1 faultactor name. 
     */
    private final static QName QNAME_FAULTACTOR = new QName("faultactor") ;
    /**
     * The qualified SOAP 1.1 detail name. 
     */
    private final static QName QNAME_DETAIL = new QName("detail") ;
    
    /**
     * The Fault Code for version mismatch.
     */
    private final static QName FAULT_CODE_VERSION_MISMATCH = getQName("VersionMismatch") ;
    /**
     * The Fault Code for must understand.
     */
    private final static QName FAULT_CODE_MUST_UNDERSTAND = getQName("MustUnderstand") ;
    /**
     * The Fault Code for sender.
     */
    private final static QName FAULT_CODE_SENDER = getQName("Client") ;
    /**
     * The Fault Code for receiver.
     */
    private final static QName FAULT_CODE_RECEIVER = getQName("Server") ;
    
    /**
     * Get the SOAP version.
     * @return The SOAP version
     */
    public String getVersion()
    {
        return SOAP_11_VERSION ;
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
     * @return The must understand qualified name.
     */
    public QName getMustUnderstandQName()
    {
        return QNAME_MUST_UNDERSTAND ;
    }
    
    /**
     * Get the value of the must understand attribute.
     * @return The must understand value.
     */
    public String getMustUnderstandValue()
    {
        return MUST_UNDERSTAND_VALUE ;
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
     * @param soapFault The soap fault.
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
     * @param soapFault The soap fault.
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
     * @return The soap fault.
     * @throws XMLStreamException For errors during reading.
     * 
     * @message com.arjuna.webservices.soap.Soap11Details_1 [com.arjuna.webservices.soap.Soap11Details_1] - Unexpected start element: {0}
     */
    public SoapFault parseSoapFault(final XMLStreamReader streamReader)
        throws XMLStreamException
    {
        final SoapFault10 soapFault = new SoapFault10() ;
        
        // faultcode processing
        StreamHelper.checkNextStartTag(streamReader, QNAME_FAULTCODE) ;
        final TextElement codeElement = new TextElement(streamReader) ;
        final QName code = QNameHelper.toQName(streamReader.getNamespaceContext(), codeElement.getText()) ;
        if (!NAMESPACE_URI.equals(code.getNamespaceURI()))
        {
            soapFault.setSoapFaultType(SoapFaultType.FAULT_SENDER) ;
            soapFault.setSubcode(code) ;
        }
        else
        {
            soapFault.setSoapFaultType(getSoapFaultType(code)) ;
        }
        
        // faultstring processing
        StreamHelper.checkNextStartTag(streamReader, QNAME_FAULTSTRING) ;
        final TextElement reasonElement = new TextElement(streamReader) ;
        soapFault.setReason(reasonElement.getText()) ;
        if (StreamHelper.checkParentFinished(streamReader))
        {
            return soapFault ;
        }
        
        // faultactor if present
        if (QNAME_FAULTACTOR.equals(streamReader.getName()))
        {
            final TextElement roleElement = new TextElement(streamReader) ;
            soapFault.setRole(roleElement.getText()) ;
            if (StreamHelper.checkParentFinished(streamReader))
            {
                return soapFault ;
            }
        }
        
        // detail if present
        if (QNAME_DETAIL.equals(streamReader.getName()))
        {
            soapFault.setDetailElement(new NamedElement(null, new AnyElement(streamReader))) ;
            if (StreamHelper.checkParentFinished(streamReader))
            {
                return soapFault ;
            }
        }

        throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_soap_Soap11Details_1(streamReader.getName())) ;
    }

    /**
     * Get the headers passed with a MustUnderstand fault.
     * @param headerName The name of the header that cannot be processed.
     * @return The headers or null if none required.
     */
    public NamedElement[] getMustUnderstandHeaders(final QName headerName)
    {
        return null ;
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
        final NamedElement[] headerElements = ((SoapFault10)soapFault).getHeaderElements() ;
        final int numHeaderElements = (headerElements == null ? 0 : headerElements.length) ;
        for(int count = 0 ; count < numHeaderElements ; count++)
        {
            final NamedElement namedElement = headerElements[count] ;
            final QName headerElementName = namedElement.getName() ;
            final String namespaceURI = StreamHelper.writeStartElement(streamWriter, headerElementName) ;
            namedElement.getElementContent().writeContent(streamWriter) ;
            StreamHelper.writeEndElement(streamWriter, headerElementName.getPrefix(), namespaceURI) ;
        }
        
        // Detail output
        if (isHeaderFault)
        {
            final NamedElement detail = ((SoapFault10)soapFault).getDetailElement() ;
            if (detail != null)
            {
            		final QName detailName = detail.getName() ;
            		if (detailName != null)
            		{
                    final String namespaceURI = StreamHelper.writeStartElement(streamWriter, detailName) ;
                    detail.getElementContent().writeContent(streamWriter) ;
                    StreamHelper.writeEndElement(streamWriter, detailName.getPrefix(), namespaceURI) ;
            		}
            }
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
        
        final QName faultCode ;
        final QName subcode = soapFault.getSubcode() ;
        if (subcode != null)
        {
            faultCode = soapFault.getSubcode() ;
        }
        else
        {
            faultCode = getSoapFaultCodeQName(soapFault) ;
        }
        
        final String faultCodeNamespaceURI = QNameHelper.getNormalisedValue(faultCode.getNamespaceURI()) ;
        
        if (faultCodeNamespaceURI.length() > 0)
        {
            StreamHelper.writeNamespace(streamWriter, faultCode.getPrefix(), faultCodeNamespaceURI) ;
        }
        
        // faultcode output
        StreamHelper.writeStartElement(streamWriter, QNAME_FAULTCODE) ;
        StreamHelper.writeQualifiedName(streamWriter, faultCode) ;
        StreamHelper.writeEndElement(streamWriter, null, null) ;
        
        // faultstring output
        StreamHelper.writeStartElement(streamWriter, QNAME_FAULTSTRING) ;
        streamWriter.writeCharacters(soapFault.getReason()) ;
        StreamHelper.writeEndElement(streamWriter, null, null) ;
        
        // faultactor output
        final String faultactor = soapFault.getRole() ;
        if (faultactor != null)
        {
            StreamHelper.writeStartElement(streamWriter, QNAME_FAULTACTOR) ;
            streamWriter.writeCharacters(faultactor) ;
            StreamHelper.writeEndElement(streamWriter, null, null) ;
        }
        
        // Detail output
        if (!isHeaderFault)
        {
            final NamedElement detail = ((SoapFault10)soapFault).getDetailElement() ;
            if (detail != null)
            {
                StreamHelper.writeStartElement(streamWriter, QNAME_DETAIL) ;
                final ElementContent detailContent = detail.getElementContent() ;
                detailContent.writeContent(streamWriter) ;
                StreamHelper.writeEndElement(streamWriter, null, null) ;
            }
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
        else
        {
            return FAULT_CODE_VERSION_MISMATCH ;
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
        else
        {
            return SoapFaultType.FAULT_VERSION_MISMATCH ;
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
