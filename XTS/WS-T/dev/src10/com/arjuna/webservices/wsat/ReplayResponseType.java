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
 * <xsd:element name="ReplayResponse">
 *   <xsd:complexType>
 *     <xsd:attribute name="outcome" type="wsat:Outcome"/>
 *   </xsd:complexType>
 * </xsd:element>
 */
/**
 * Representation of the ReplayResponse element.
 * @author kevin
 */
public class ReplayResponseType extends ElementContent
{
    /**
     * The value of the outcome.
     */
    private Outcome outcome ;
    
    /**
     * Default constructor.
     */
    public ReplayResponseType()
    {
    }
    
    /**
     * Construct the ReplayResponse from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public ReplayResponseType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
    
    /**
     * Add the element.
     * @param in The current input stream.
     * @param elementName The qualified element name.
     * @message com.arjuna.webservices.wsat.ReplayResponseType_1 [com.arjuna.webservices.wsat.ReplayResponseType_1] - ReplayResponse elements cannot have embedded elements.
     */
    protected void putElement(final XMLStreamReader in,
        final QName elementName)
        throws XMLStreamException
    {
        throw new XMLStreamException(WSTLogger.arjLoggerI18N.getString("com.arjuna.webservices.wsat.ReplayResponseType_1")) ;
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
            AtomicTransactionConstants.WSAT_ATTRIBUTE_OUTCOME.equals(attributeName.getLocalPart()))
        {
            try
            {
                setOutcome(Outcome.toOutcome(attributeValue)) ;
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
     * Set the outcome of this element.
     * @param outcome The outcome of the element.
     */
    public void setOutcome(final Outcome outcome)
    {
        this.outcome = outcome ;
    }
    
    /**
     * Get the outcome of this element.
     * @return The outcome of the element or null if not set.
     */
    public Outcome getOutcome()
    {
        return outcome ;
    }
    
    /**
     * Write the attributes of the element.
     * @param out The output stream.
     */
    protected void writeAttributes(final XMLStreamWriter out)
        throws XMLStreamException
    {
        StreamHelper.writeAttribute(out, AtomicTransactionConstants.WSAT_ATTRIBUTE_OUTCOME_QNAME, outcome.getValue()) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return (outcome != null) && super.isValid() ;
    }
}
