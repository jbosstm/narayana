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
package com.arjuna.webservices.wscoor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.stax.AnyContentAnyAttributeSupport;
import com.arjuna.webservices.util.StreamHelper;

/*
 * <xsd:complexType name="CreateCoordinationContextResponseType">
 *   <xsd:sequence>
 *     <xsd:element ref="wscoor:CoordinationContext"/>
 *     <xsd:any namespace="##other" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *   </xsd:sequence>
 *   <xsd:anyAttribute namespace="##other" processContents="lax"/>
 * </xsd:complexType>
 */
/**
 * Representation of the Create Coordination Context Reponse type.
 * @author kevin
 */
public class CreateCoordinationContextResponseType extends AnyContentAnyAttributeSupport
{
    /**
     * The CoordinationContext element.
     */
    private CoordinationContextType coordinationContext ;
    
    /**
     * Default constructor.
     */
    public CreateCoordinationContextResponseType()
    {
    }
    
    /**
     * Construct a create coordination context response from the input stream.
     * 
     * @param in The input stream.
     * @throws XMLStreamException For errors during parsing.
     */
    public CreateCoordinationContextResponseType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Get the coordination context.
     * @return The coordination context.
     */
    public CoordinationContextType getCoordinationContext()
    {
        return coordinationContext ;
    }
    
    /**
     * Set the coordination context.
     * @param coordinationContext The coordination context.
     */
    public void setCoordinationContext(final CoordinationContextType coordinationContext)
    {
        this.coordinationContext = coordinationContext ;
    }
    
    /**
     * Write the content of the context to the stream.
     * @param out The current output stream.
     * @message com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType_1 [com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType_1] - Create Coordination Context Response is not valid
     */
    public void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
       if (!isValid())
       {
           throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wscoor_CreateCoordinationContextResponseType_1()) ;
       }
       
       StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT_QNAME, coordinationContext) ;
       
       super.writeChildContent(out) ;
    }

    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType_2 [com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType_2] - Unexpected element name: {0}
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        if (CoordinationConstants.WSCOOR_NAMESPACE.equals(elementName.getNamespaceURI()))
        {
            final String localPart = elementName.getLocalPart() ;
            if (CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT.equals(localPart))
            {
                setCoordinationContext(new CoordinationContextType(in)) ;
            }
            else
            {
                throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wscoor_CreateCoordinationContextResponseType_2(elementName)) ;
            }
        }
        else
        {
            super.putElement(in, elementName) ;
        }
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((coordinationContext != null) && coordinationContext.isValid()) ;
    }
}
