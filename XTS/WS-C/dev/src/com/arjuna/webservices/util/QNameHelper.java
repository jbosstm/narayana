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
package com.arjuna.webservices.util;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * Helper class for manipulating QNames.
 * @author kevin
 */
public class QNameHelper
{
    /**
     * Return a qualified representation of the qname.
     * @param qname The qname.
     * @return The qualified name.
     */
    public static String toQualifiedName(final QName qname)
    {
        return toQualifiedName(qname.getPrefix(), qname.getLocalPart()) ;
    }
    
    /**
     * Return a qualified representation of the prefix and local name.
     * @param prefix The prefix.
     * @param localName The local name.
     * @return The qualified name.
     */
    public static String toQualifiedName(final String prefix, final String localName)
    {
        if ((prefix == null) || (prefix.length() == 0))
        {
            return localName ;
        }
        else
        {
            return prefix + ":" + localName ;
        }
    }
    
    /**
     * Return the qname represented by the qualified name.
     * @param namespaceContext The namespace context.
     * @param qualifiedName The qualified name.
     * @return The qname.
     */
    public static QName toQName(final NamespaceContext namespaceContext, final String qualifiedName)
    {
        final int index = qualifiedName.indexOf(':') ;
        if (index == -1)
        {
            return new QName(qualifiedName) ;
        }
        else
        {
            final String prefix = qualifiedName.substring(0, index) ;
            final String localName = qualifiedName.substring(index+1) ;
            
            final String namespaceURI = getNormalisedValue(namespaceContext.getNamespaceURI(prefix)) ;
            return new QName(namespaceURI, localName, prefix) ;
        }
    }

    /**
     * Get the normalised value of the string.
     * @param value The string value.
     * @return The normalised value.
     */
    public static String getNormalisedValue(final String value)
    {
        if (value == null)
        {
            return "" ;
        }
        else
        {
            return value ;
        }
    }
}
