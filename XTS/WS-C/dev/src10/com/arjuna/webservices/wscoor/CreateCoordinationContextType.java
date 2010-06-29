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
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.util.StreamHelper;

/*
 * <xsd:complexType name="CreateCoordinationContextType">
 *   <xsd:sequence>
 *     <xsd:element ref="wscoor:Expires" minOccurs="0"/>
 *     <xsd:element name="CurrentContext" minOccurs="0">
 *       <xsd:complexType>
 *         <xsd:complexContent>
 *           <xsd:extension base="wscoor:CoordinationContextType">
 *             <xsd:sequence>
 *               <xsd:any namespace="##other" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *             </xsd:sequence>
 *           </xsd:extension>
 *         </xsd:complexContent>
 *       </xsd:complexType>
 *     </xsd:element>
 *     <xsd:element name="CoordinationType" type="xsd:anyURI"/>
 *     <xsd:any namespace="##any" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *   </xsd:sequence>
 *   <xsd:anyAttribute namespace="##other" processContents="lax"/>
 * </xsd:complexType>
 */
/**
 * Representation of the Create Coordination Context type.
 * @author kevin
 */
public class CreateCoordinationContextType extends AnyContentAnyAttributeSupport
{
    /**
     * The Expires element.
     */
    private AttributedUnsignedIntType expires ;
    /**
     * The CurrentContext element.
     */
    private CoordinationContextType currentContext ;
    /**
     * The Coordination Type uri.
     */
    private URI coordinationType ;
    
    /**
     * Default constructor.
     */
    public CreateCoordinationContextType()
    {
    }
    
    /**
     * Construct a create coordination context from the input stream.
     * 
     * @param in The input stream.
     * @throws XMLStreamException For errors during parsing.
     */
    public CreateCoordinationContextType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Get the expires data.
     * @return The expires information or null if not present.
     */
    public AttributedUnsignedIntType getExpires()
    {
        return expires ;
    }
    
    /**
     * Set the expires data.
     * @param expires The expires data.
     */
    public void setExpires(final AttributedUnsignedIntType expires)
    {
        this.expires = expires ;
    }
    
    /**
     * Get the current context.
     * @return The current context or null if not present.
     */
    public CoordinationContextType getCurrentContext()
    {
        return currentContext ;
    }
    
    /**
     * Set the current context.
     * @param currentContext The current context.
     */
    public void setCurrentContext(final CoordinationContextType currentContext)
    {
        this.currentContext = currentContext ;
    }
    
    /**
     * Get the coordination type.
     * @return The coordination type.
     */
    public URI getCoordinationType()
    {
        return coordinationType ;
    }
    
    /**
     * Set the coordination type.
     * @param coordinationType The coordination type.
     */
    public void setCoordinationType(final URI coordinationType)
    {
        this.coordinationType = coordinationType ;
    }
    
    /**
     * Write the content of the context to the stream.
     * @param out The current output stream.
     * @message com.arjuna.webservices.wscoor.CreateCoordinationContextType_1 [com.arjuna.webservices.wscoor.CreateCoordinationContextType_1] - Create Coordination Context is not valid
     */
    public void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
       if (!isValid())
       {
           throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wscoor_CreateCoordinationContextType_1()) ;
       }
       
       if (expires != null)
       {
           StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_EXPIRES_QNAME, expires) ;
       }
       if (currentContext != null)
       {
           StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_CURRENT_CONTEXT_QNAME, currentContext) ;
       }
       StreamHelper.writeElement(out, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_TYPE_QNAME, coordinationType) ;
       
       super.writeChildContent(out) ;
    }

    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wscoor.CreateCoordinationContextType_2 [com.arjuna.webservices.wscoor.CreateCoordinationContextType_2] - Unexpected element name: {0}
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        if (CoordinationConstants.WSCOOR_NAMESPACE.equals(elementName.getNamespaceURI()))
        {
            final String localPart = elementName.getLocalPart() ;
            if (CoordinationConstants.WSCOOR_ELEMENT_EXPIRES.equals(localPart))
            {
                setExpires(new AttributedUnsignedIntType(in)) ;
            }
            else if (CoordinationConstants.WSCOOR_ELEMENT_CURRENT_CONTEXT.equals(localPart))
            {
                setCurrentContext(new CoordinationContextType(in)) ;
            }
            else if (CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_TYPE.equals(localPart))
            {
                setCoordinationType(new URI(in)) ;
            }
            else
            {
                throw new XMLStreamException(WSCLogger.i18NLogger.get_webservices_wscoor_CreateCoordinationContextType_2(elementName)) ;
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
        return ((expires == null) || expires.isValid()) &&
            ((currentContext == null) || currentContext.isValid()) &&
            ((coordinationType != null) && coordinationType.isValid()) ;
    }
}
