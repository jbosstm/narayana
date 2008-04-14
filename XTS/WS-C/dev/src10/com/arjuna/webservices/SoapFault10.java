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

package com.arjuna.webservices;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.stax.NamedElement;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

/**
 * IMplementtaion of SoapFault used by WSCOOR1.0 classes
 */
public class SoapFault10 extends SoapFault
{
    /**
     * The detailElement element.
     */
    private NamedElement detailElement;
    /**
     * The header elements.
     */
    private NamedElement[] headerElements ;
    /**
     * Default constructor.
     */
    public SoapFault10()
    {
        super();
    }

    /**
     * Construct a SOAP fault based on a throwable.
     * @param th The throwable.
     */
    public SoapFault10(final Throwable th)
    {
        super(th);
    }

    /**
     * Construct a SOAP header fault based on a throwable.
     * @param th The throwable.
     * @param headerElements The header elements.
     */
    public SoapFault10(final Throwable th, final NamedElement[] headerElements)
    {
        super(th);
        this.headerElements = headerElements;
    }

    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param subcode The fault subcode.
     * @param reason The fault reason.
     */
    public SoapFault10(final SoapFaultType soapFaultType, final QName subcode, final String reason)
    {
        super(soapFaultType, subcode, reason) ;
    }

    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param subcode The fault subcode.
     * @param reason The fault reason.
     * @param headerElements The header elements.
     */
    public SoapFault10(final SoapFaultType soapFaultType, final QName subcode, final String reason, final NamedElement[] headerElements)
    {
        super(soapFaultType, subcode, reason, null);
        this.headerElements = headerElements ;
    }

    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param subcode The fault subcode.
     * @param reason The fault reason.
     * @param detailElement The fault detailElement.
     */
    public SoapFault10(final SoapFaultType soapFaultType, final QName subcode, final String reason, final NamedElement detailElement)
    {
        super(soapFaultType, subcode, reason, getDetailString(detailElement)) ;
        this.detailElement = detailElement;
    }

    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param reason The fault reason.
     */
    public SoapFault10(final SoapFaultType soapFaultType, final String reason)
    {
        this(soapFaultType, null, reason, null, null) ;
    }

    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param reason The fault reason.
     * @param detailElement The fault detailElement.
     */
    public SoapFault10(final SoapFaultType soapFaultType,
        final String reason, final NamedElement detailElement)
    {
        this(soapFaultType, null, reason, detailElement, null) ;
    }

    /**
     * Create a custom SOAP fault.
     * @param soapFaultType the soap fault type.
     * @param subcode The fault subcode.
     * @param reason The fault reason.
     * @param detailElement The fault detailElement.
     * @param headerElements The fault header elements.
     */
    public SoapFault10(final SoapFaultType soapFaultType, final QName subcode,
        final String reason, final NamedElement detailElement, final NamedElement[] headerElements)
    {
        this.soapFaultType = soapFaultType ;
        this.subcode = subcode ;
        this.reason = reason ;
        this.detailElement = detailElement;
        this.headerElements = headerElements ;
    }

    /**
     * Get the SOAP fault detailElement.
     * @return The SOAP fault detailElement.
     */
    public NamedElement getDetailElement()
    {
        return detailElement;
    }

    /**
     * Set the SOAP fault detailElement.
     * @param detailElement The SOAP fault detailElement.
     */
    public void setDetailElement(final NamedElement detailElement)
    {
        this.detailElement = detailElement;
        setDetail(getDetailString(detailElement));
    }

    /**
     * Get the SOAP fault header elements.
     * @return The SOAP fault header elements.
     */
    public NamedElement[] getHeaderElements()
    {
        return headerElements ;
    }

    /**
     * Set the SOAP fault header elements.
     * @param headerElements The SOAP fault header elements.
     */
    public void setHeaderElements(final NamedElement[] headerElements)
    {
        this.headerElements = headerElements ;
    }

    /**
     * return a String representing the supplied detail element
     * @param detailElement
     * @return
     */

    private static String getDetailString(NamedElement detailElement)
    {
        XMLOutputFactory factory = getFactory();
        if (factory != null) {
            try {
                StringWriter stringWriter = new StringWriter();
                XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(stringWriter);
                detailElement.getElementContent().writeContent(xmlWriter);
                xmlWriter.close();
                return stringWriter.toString();
            } catch (XMLStreamException e) {
            }
        }

        return "";
    }

    /**
     * helper to safely initialise and return a static xml output factory variable
     * @return the factory
     */
    private static synchronized XMLOutputFactory getFactory()
    {
        if (factory == null) {
            try {
                factory = XMLOutputFactory.newInstance();
            } catch (FactoryConfigurationError e) {
                // TODO log error here
            }
        }
        return null;
    }

    /**
     * xml output factory used to create XMLStreamWriters when converting detail elements to strings
     */
    private static XMLOutputFactory factory;
}
