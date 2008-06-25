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
package com.arjuna.webservices.wsba;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.arjuna.webservices.stax.AnyContentAnyAttributeSupport;
import com.arjuna.webservices.util.StreamHelper;
/*
 * <xsd:complexType name="StatusType">
 *   <xsd:sequence>
 *     <xsd:element name="State" type="wsba:StateType"/>
 *     <xsd:any namespace="##other" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *   </xsd:sequence>
 *   <xsd:anyAttribute namespace="##other" processContents="lax"/>
 * </xsd:complexType>
 */
/**
 * Representation of a StatusType
 * @author kevin
 */
public class StatusType extends AnyContentAnyAttributeSupport
{
    /**
     * The state.
     */
    private StateType state ;

    /**
     * Default constructor.
     */
    public StatusType()
    {
    }

    /**
     * Construct the status type with the specified state.
     * @param state the state.
     */
    public StatusType(final State state)
    {
        this.state = new StateType(state) ;
    }
    
    /**
     * Construct the state from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public StatusType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }

    /**
     * Set the state.
     * @param state The state.
     */
    private void setState(final StateType state)
    {
        this.state = state ;
    }
    
    /**
     * Get the state.
     * @return The state.
     */
    public State getState()
    {
        return (state == null ? null : state.getState()) ;
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
        if (BusinessActivityConstants.WSBA_NAMESPACE.equals(elementName.getNamespaceURI()))
        {
            final String localPart = elementName.getLocalPart() ;
            if (BusinessActivityConstants.WSBA_ELEMENT_STATE.equals(localPart))
            {
                setState(new StateType(in)) ;
            }
            else
            {
                throw new XMLStreamException("Unexpected element name: " + elementName) ;
            }
        }
        else
        {
            super.putElement(in, elementName) ;
        }
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        StreamHelper.writeElement(out, BusinessActivityConstants.WSBA_ELEMENT_STATE_QNAME, state) ;
        super.writeChildContent(out) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return ((state != null) && state.isValid()) &&
            super.isValid() ;
    }
}
