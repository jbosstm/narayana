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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.util.QNameHelper;
import com.arjuna.webservices.util.StreamHelper;

/**
 * Representation of the NotUnderstood header element.
 * @author kevin
 */
public class SoapNotUnderstoodType extends ElementContent
{
    /**
     * The local name of the qname.
     */
    private static final String QNAME_LOCAL_NAME = "qname" ;
    /**
     * The qualified name of the qname.
     */
    private static final QName QNAME_NAME = new QName(QNAME_LOCAL_NAME) ;
        
    /**
     * The value of the qName.
     */
    private QName qName ;
    
    /**
     * Construct the NotUnderstood type.
     * @param qName The header qualified name.
     */
    public SoapNotUnderstoodType(final QName qName)
    {
        this.qName = qName ;
    }
    
    /**
     * Construct the NotUnderstood from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public SoapNotUnderstoodType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.soap.SoapNotUnderstoodType_1 [com.arjuna.webservices.soap.SoapNotUnderstoodType_1] - NotUnderstood elements cannot have embedded elements.
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        throw new XMLStreamException(WSCLogger.arjLoggerI18N.getString("com.arjuna.webservices.soap.SoapNotUnderstoodType_1")) ;
    }
    
    /**
     * Add the attribute value.
     * @param in The current input stream.
     * @param attributeName The qualified attribute name.
     * @param attributeValue The qualified attibute value.
     */
    protected void putAttribute(final XMLStreamReader in,
        final QName attributeName, final String attributeValue)
        throws XMLStreamException
    {
        final String namespaceURI = attributeName.getNamespaceURI() ;
        if (((namespaceURI == null) || (namespaceURI.length() == 0)) &&
            QNAME_LOCAL_NAME.equals(attributeName.getLocalPart()))
        {
            setQName(QNameHelper.toQName(in.getNamespaceContext(), attributeValue)) ;
        }
        else
        {
            super.putAttribute(in, attributeName, attributeValue) ;
        }
    }
    
    /**
     * Set the qName of this element.
     * @param qName The qName of the element.
     */
    public void setQName(final QName qName)
    {
        this.qName = qName ;
    }
    
    /**
     * Get the qName of this element.
     * @return The qName of the element or null if not set.
     */
    public QName getQName()
    {
        return qName ;
    }
    
    /**
     * Write the attributes of the element.
     * @param out The output stream.
     */
    protected void writeAttributes(final XMLStreamWriter out)
        throws XMLStreamException
    {
        StreamHelper.writeAttribute(out, QNAME_NAME, qName) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return (qName != null) && super.isValid() ;
    }
}
