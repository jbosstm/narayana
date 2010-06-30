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
package com.arjuna.webservices.wsat;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.util.InvalidEnumerationException;
import com.arjuna.webservices.util.StreamHelper;

/*
 * <xsd:element name="PrepareResponse">
 *   <xsd:complexType>
 *     <xsd:attribute name="vote" type="wsat:Vote"/>
 *   </xsd:complexType>
 * </xsd:element>
 */
/**
 * Representation of the PrepareResponse element.
 * @author kevin
 */
public class PrepareResponseType extends ElementContent
{
    /**
     * The value of the vote.
     */
    private Vote vote ;
    
    /**
     * Default constructor.
     */
    public PrepareResponseType()
    {
    }
    
    /**
     * Construct the PrepareResponse from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public PrepareResponseType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        throw new XMLStreamException(WSTLogger.i18NLogger.get_webservices_wsat_PrepareResponseType_1()) ;
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
        if (AtomicTransactionConstants.WSAT_ATTRIBUTE_NAMESPACE.equals(attributeName.getNamespaceURI()) &&
            AtomicTransactionConstants.WSAT_ATTRIBUTE_VOTE.equals(attributeName.getLocalPart()))
        {
            try
            {
                setVote(Vote.toVote(attributeValue)) ;
            }
            catch (final InvalidEnumerationException iee)
            {
                throw new XMLStreamException(iee) ;
            }
        }
        else
        {
            super.putAttribute(in, attributeName, attributeValue) ;
        }
    }
    
    /**
     * Set the vote of this element.
     * @param vote The vote of the element.
     */
    public void setVote(final Vote vote)
    {
        this.vote = vote ;
    }
    
    /**
     * Get the vote of this element.
     * @return The vote of the element or null if not set.
     */
    public Vote getVote()
    {
        return vote ;
    }
    
    /**
     * Write the attributes of the element.
     * @param out The output stream.
     */
    protected void writeAttributes(final XMLStreamWriter out)
        throws XMLStreamException
    {
        StreamHelper.writeAttribute(out, AtomicTransactionConstants.WSAT_ATTRIBUTE_VOTE_QNAME, vote.getValue()) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return (vote != null) && super.isValid() ;
    }
}
