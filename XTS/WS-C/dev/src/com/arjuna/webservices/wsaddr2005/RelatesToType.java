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
package com.arjuna.webservices.wsaddr2005;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.util.StreamHelper;

/*
 * <xs:complexType name="RelatesToType" mixed="false">
 *   <xs:simpleContent>
 *     <xs:extension base="xs:anyURI">
 *       <xs:attribute name="RelationshipType" type="tns:RelationshipTypeOpenEnum" use="optional" default="http://www.w3.org/2005/08/addressing/reply"/>
 *       <xs:anyAttribute namespace="##other" processContents="lax"/>
 *     </xs:extension>
 *   </xs:simpleContent>
 * </xs:complexType>
 */
/**
 * Representation of a RelatesTo
 * @author kevin
 */
public class RelatesToType extends AttributedURIType
{
    /**
     * The relationship type attribute.
     */
    private String relationshipType ;
    
    /**
     * Default constructor.
     */
    public RelatesToType()
    {
    }
    
    /**
     * Construct the relationship type.
     * @param relatesTo The relates to id.
     */
    public RelatesToType(final String relatesTo)
    {
        setValue(relatesTo) ;
    }
    
    /**
     * Construct the relationship from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public RelatesToType(final XMLStreamReader in)
        throws XMLStreamException
    {
        super(in) ;
    }
    /**
     * Get the relationship type.
     * @return The relationship type or null if not set.
     */
    public String getRelationshipType()
    {
        return relationshipType ;
    }
    
    /**
     * Set the relationship type.
     * @param relationshipType The relationship type.
     */
    public void setRelationshipType(final String relationshipType)
    {
        this.relationshipType = relationshipType ;
    }
    
    /**
     * Add the attribute value to the list of known attributes.
     * @param in The current input stream.
     * @param attributeName The qualified attribute name.
     * @param attributeValue The qualified attibute value.
     */
    protected void putAttribute(final XMLStreamReader in, final QName attributeName,
        final String attributeValue)
        throws XMLStreamException
    {
        if (AddressingConstants.WSA_ATTRIBUTE_NAMESPACE.equals(attributeName.getNamespaceURI()) &&
                AddressingConstants.WSA_ATTRIBUTE_RELATIONSHIP_TYPE.equals(attributeName.getLocalPart()))
        {
        		setRelationshipType(attributeValue) ;
        }
        else
        {
            super.putAttribute(in, attributeName, attributeValue) ;
        }
    }
    
    /**
     * Write the attributes of the element.
     * @param out The output stream.
     */
    protected void writeAttributes(final XMLStreamWriter out)
        throws XMLStreamException
    {
        if (relationshipType != null)
        {
            StreamHelper.writeAttribute(out, AddressingConstants.WSA_ATTRIBUTE_QNAME_RELATIONSHIP_TYPE, relationshipType) ;
        }
        super.writeAttributes(out) ;
    }
}
