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
package com.arjuna.webservices.wsaddr;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyAttributeSupport;
import com.arjuna.webservices.util.QNameHelper;
import com.arjuna.webservices.util.StreamHelper;

/*
 * <xs:complexType name="AttributedQName">
 *   <xs:simpleContent>
 *     <xs:extension base="xs:QName">
 *       <xs:anyAttribute namespace="##other" processContents="lax"/>
 *     </xs:extension>
 *   </xs:simpleContent>
 * </xs:complexType>
 */
/**
 * Representation of an AttributedQName
 * @author kevin
 */
public class AttributedQNameType extends AnyAttributeSupport
{
    /**
     * The QName value of this element.
     */
    private QName value ;
    
    /**
     * Default constructor.
     */
    public AttributedQNameType()
    {
    }
    
    /**
     * Construct the attributed QName from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public AttributedQNameType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Set the text value of this element.
     * @param in The current input stream.
     * @param value The text value.
     */
    protected void putValue(final XMLStreamReader in, final String value)
        throws XMLStreamException
    {
        final NamespaceContext namespaceContext = in.getNamespaceContext() ;
        setValue(QNameHelper.toQName(namespaceContext, value)) ;
    }
    
    /**
     * Set the QName value of this element.
     * @param value The QName value of the element.
     */
    public void setValue(final QName value)
    {
        this.value = value ;
    }
    
    /**
     * Get the QName value of this element.
     * @return The QName value of the element or null if not set.
     */
    public QName getValue()
    {
        return value ;
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     * @message com.arjuna.webservices.wsaddr.AttributedQNameType_1 [com.arjuna.webservices.wsaddr.AttributedQNameType_1] - Invalid QName value for attributed QName
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        if (value == null)
        {
            throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wsaddr_AttributedQNameType_1()) ;
        }
        
        StreamHelper.writeQualifiedName(out, value) ;
    }
    
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return (value != null) && super.isValid() ;
    }
}
