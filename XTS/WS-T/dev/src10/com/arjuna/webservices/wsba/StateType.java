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

import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.stax.ElementContent;
import com.arjuna.webservices.util.InvalidEnumerationException;
import com.arjuna.webservices.util.QNameHelper;
import com.arjuna.webservices.util.StreamHelper;

/**
 * Representation of a State element.
 * @author kevin
 */
class StateType extends ElementContent
{
    /**
     * The state.
     */
    private State state ;
    
    /**
     * Construct the state with the specified state.
     * @param state The state.
     */
    StateType(final State state)
    {
        this.state = state ;
    }
    
    /**
     * Construct the State from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    StateType(final XMLStreamReader in)
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
        throw new XMLStreamException(WSTLogger.i18NLogger.get_webservices_wsba_StateType_1()) ;
    }
    
    /**
     * Set the state of this element.
     * @param state The state of the element.
     */
    void setState(final State state)
    {
        this.state = state ;
    }
    
    /**
     * Get the state of this element.
     * @return The state of the element or null if not set.
     */
    State getState()
    {
        return state ;
    }
    
    /**
     * Set the text value of this element.
     * @param in The current input stream.
     * @param value The text value of this element.
     */
    protected void putValue(final XMLStreamReader in, final String value)
        throws XMLStreamException
    {
        final QName qName = QNameHelper.toQName(in.getNamespaceContext(), value) ;
        try
        {
            state = State.toState(qName) ;
        }
        catch (final InvalidEnumerationException iee)
        {
            throw new XMLStreamException(iee) ;
        }
    }
    
    /**
     * Write the child content of the element.
     * @param out The output stream.
     */
    protected void writeChildContent(final XMLStreamWriter out)
        throws XMLStreamException
    {
        StreamHelper.writeQualifiedName(out, state.getValue()) ;
    }
    
    /**
     * Is the configuration of this element valid?
     * @return true if valid, false otherwise.
     */
    public boolean isValid()
    {
        return (state != null) && super.isValid() ;
    }
}
