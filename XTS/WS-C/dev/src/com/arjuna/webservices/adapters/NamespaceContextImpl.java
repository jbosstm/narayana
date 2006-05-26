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
package com.arjuna.webservices.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.NamespaceContext;

/**
 * Implementation of a namespace context.
 * @author kevin
 */
public class NamespaceContextImpl implements NamespaceContext
{
    /**
     * The parent context.
     */
    private final NamespaceContextImpl parent ;
    
    /**
     * The prefix mappings.
     */
    private final Map prefixMappings = new HashMap() ;
    /**
     * The prefix list.
     */
    private final List prefixList = new ArrayList() ;
    /**
     * The namespace mappings.
     */
    private final Map namespaceMappings = new HashMap() ;

    /**
     * Construct the initial namespace context.
     */
    public NamespaceContextImpl()
    {
        this(null) ;
    }
    
    /**
     * Construct the namespace context with the specified parent.
     * @param parent The specified parent.
     */
    public NamespaceContextImpl(final NamespaceContextImpl parent)
    {
        this.parent = parent ;
    }
    
    /**
     * Get the namespace URI for the specified prefix.
     * @param prefix The prefix.
     * @return the namespace URI for the prefix.
     */
    public String getNamespaceURI(final String prefix)
    {
        final String namespaceURI = (String)prefixMappings.get(prefix) ;
        if (namespaceURI != null)
        {
            return namespaceURI ;
        }
        return (parent != null ? parent.getNamespaceURI(prefix) : null) ;
    }

    /**
     * Get the prefix for the specified namespace URI.
     * @param namespaceURI The namespace URI.
     * @return the prefi for the namespace URI.
     */
    public String getPrefix(final String namespaceURI)
    {
        final Set prefixes = (Set)namespaceMappings.get(namespaceURI) ;
        if (prefixes != null)
        {
            return (String)prefixes.iterator().next();
        }
        return (parent != null ? parent.getPrefix(namespaceURI) : null) ;
    }
    
    /**
     * Get the iterator of prefixes for the specified namespace URI.
     * @param namespaceURI The namespace URI.
     * @return The set of prefixes.
     */
    public Iterator getPrefixes(final String namespaceURI)
    {
        return getPrefixSet(namespaceURI).iterator() ;
    }
    
    /**
     * Get the parent namespace context.
     * @return The parent namespace context.
     */
    public NamespaceContextImpl getParent()
    {
        return parent ;
    }
    
    /**
     * Set the default namespace URI.
     * @param namespaceURI The namespace URI.
     */
    public void setDefaultNamespace(final String namespaceURI)
    {
        setPrefix(null, namespaceURI) ;
    }
    
    /**
     * Set the prefix to the specified namespace URI.
     * @param prefix The prefix.
     * @param namespaceURI The namespace URI.
     */
    public void setPrefix(final String prefix, final String namespaceURI)
    {
        final String previousNamespaceURI = (String)prefixMappings.put(prefix, namespaceURI) ;
        if (previousNamespaceURI != null)
        {
            final Set previousSet = (Set)namespaceMappings.get(previousNamespaceURI) ;
            if (previousSet != null)
            {
                if (previousSet.size() == 1)
                {
                    namespaceMappings.remove(previousNamespaceURI) ;
                }
                else
                {
                    previousSet.remove(prefix) ;
                }
            }
        }
        else
        {
            prefixList.add(prefix) ;
        }
        final Set currentSet = (Set)namespaceMappings.get(namespaceURI) ;
        if (currentSet != null)
        {
            currentSet.add(prefix) ;
        }
        else
        {
            final Set newSet = new TreeSet() ;
            newSet.add(prefix) ;
            namespaceMappings.put(namespaceURI, newSet) ;
        }
    }
    
    /**
     * Get the number of namespaces in this context.
     * @return the number of namespaces.
     */
    public int getNamespaceCount()
    {
        return prefixList.size() ;
    }
    
    /**
     * Get the prefix at the specified index.
     * @param index The index.
     * @return The prefix.
     */
    public String getPrefix(final int index)
    {
        return (String)prefixList.get(index) ;
    }
    
    /**
     * Get the namespace URI at the specified index.
     * @param index The index.
     * @return The namespace URI.
     */
    public String getNamespaceURI(final int index)
    {
        final String prefix = getPrefix(index) ;
        return (String)(prefixMappings.get(prefix)) ;
    }

    /**
     * Get the set of prefixes for the specified namespace URI.
     * @param namespaceURI The namespace URI.
     * @return The set of prefixes.
     */
    private Set getPrefixSet(final String namespaceURI)
    {
        final Set set = (parent != null ? parent.getPrefixSet(namespaceURI) : new TreeSet()) ;
        final Set currentSet = (Set)namespaceMappings.get(namespaceURI) ;
        if (currentSet != null)
        {
            set.addAll(currentSet) ;
        }
        return set ;
    }
}
