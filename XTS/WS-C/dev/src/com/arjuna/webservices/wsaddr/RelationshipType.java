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
package com.arjuna.webservices.wsaddr;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.util.QNameHelper;
import com.arjuna.webservices.util.StreamHelper;

/*
 * <xs:complexType name="Relationship">
 *   <xs:simpleContent>
 *     <xs:extension base="xs:anyURI">
 *       <xs:attribute name="RelationshipType" type="xs:QName" use="optional"/>
 *       <xs:anyAttribute namespace="##other" processContents="lax"/>
 *     </xs:extension>
 *   </xs:simpleContent>
 * </xs:complexType>
 */
/**
 * Representation of a Relationship
 * @author kevin
 */
public class RelationshipType extends AttributedURIType
{
    /**
     * The relationship type attribute.
     */
    private QName relationshipType ;
    
    /**
     * Default constructor.
     */
    public RelationshipType()
    {
    }
    
    /**
     * Construct the relationship type.
     * @param relatesTo The relates to id.
     */
    public RelationshipType(final String relatesTo)
    {
        setValue(relatesTo) ;
    }
    
    /**
     * Construct the relationship from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public RelationshipType(final XMLStreamReader in)
        throws XMLStreamException
    {
        super(in) ;
    }
    /**
     * Get the relationship type.
     * @return The relationship type or null if not set.
     */
    public QName getRelationshipType()
    {
        return relationshipType ;
    }
    
    /**
     * Set the relationship type.
     * @param relationshipType The relationship type.
     */
    public void setRelationshipType(final QName relationshipType)
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
            final NamespaceContext namespaceContext = in.getNamespaceContext() ;
            setRelationshipType(QNameHelper.toQName(namespaceContext, attributeValue)) ;
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
            StreamHelper.writeAttribute(out, AddressingConstants.WSA_ATTRIBUTE_RELATIONSHIP_TYPE_QNAME, relationshipType) ;
        }
        super.writeAttributes(out) ;
    }
}
