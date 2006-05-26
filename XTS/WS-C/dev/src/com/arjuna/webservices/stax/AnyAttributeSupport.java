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
package com.arjuna.webservices.stax;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Utility class providing support for AnyAttributes.
 * @author kevin
 */
public abstract class AnyAttributeSupport extends AnyContentAnyAttributeSupport
{
    /**
     * Add a content element to the list of any elements.
     * @param content The content element.
     */
    public void putAnyContent(final NamedElement content)
    {
        // Ignore content.
    }
    
    /**
     * Copy the any elements from another source.
     * @param source The source of the any data.
     */
    public void copyAnyContents(final AnyContentAnyAttributeSupport source)
    {
        // Ignore content.
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
        // count start/end elements
        int count = 1 ;
        while (count > 0)
        {
            final int type = in.next() ;
            if (XMLStreamConstants.START_ELEMENT == type)
            {
                count++ ;
            }
            else if (XMLStreamConstants.END_ELEMENT == type)
            {
                count-- ;
            }
        }
    }
}
