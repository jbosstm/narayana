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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.arjuna.webservices.stax.AnyContentSupport;

/*
 * <xs:complexType name="ReferencePropertiesType">
 *   <xs:sequence>
 *     <xs:any processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *   </xs:sequence>
 * </xs:complexType>
 */
/**
 * Representation of a ReferencePropertiesType
 * @author kevin
 */
public class ReferencePropertiesType extends AnyContentSupport
{
    /**
     * Default constructor.
     */
    public ReferencePropertiesType()
    {
    }
    
    /**
     * Construct the reference properties from the input stream.
     * @param in The input stream.
     * @throws XMLStreamException for parsing errors.
     */
    public ReferencePropertiesType(final XMLStreamReader in)
        throws XMLStreamException
    {
        parse(in) ;
    }
}
