/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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