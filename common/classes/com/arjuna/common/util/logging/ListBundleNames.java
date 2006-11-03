/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
package com.arjuna.common.util.logging;

import java.util.Vector;

/**
 * This class is used to define a list of Resource Bundle names that can be associated with a logger object
 *
 * @author Malik SAHEB - malik.saheb@arjuna.com
 * @since 1.1 
 * @version $Id: ListBundleNames.java 2342 2006-03-30 13:06:17Z  $
 *
 * @deprecated TR--seems to be unused in clf-2.0 -- can we remove it?
 */

public class ListBundleNames {

    /*
     * This class is used to manage the list of resource bundle names associated with a logger
     * It should be deprecated and replaced by a class managing a list of object provided by JDK 
     */
   
    /**
     * Constructs a new instance of ListBundleNames
     */
    public ListBundleNames() 
    {
	ListBundNames = new Vector();
    }

    /**
     * add a resource bundle name in the list
     */
    public void add(Object obj) 
    {
	if (!ListBundNames.contains(obj))
	    ListBundNames.addElement(obj);
    }
    
    /**
     * remove a resource bundle name from the list
     */
    public void remove(Object obj)
    {
	boolean b = ListBundNames.removeElement(obj);
    }

    /**
     * Determines if a resource bundle name is in the list
     * @return true if the bundle name is in the list
     */
    public boolean contains(Object obj)
    {
	return ListBundNames.contains(obj);
    }

    /**
     * Get the first resource bundle name from the list
     */
    public Object getFirst()
    {
	//index = 0;
	return ListBundNames.firstElement();
    }


    /**
     * Get the last resource bundle name from the list
     */
    public Object getLast()
    {
	return ListBundNames.lastElement();
    }
    
    
    /**
     * Get a resource bundle name from the list
     */
    public Object getElement(int index)
    {
	return ListBundNames.elementAt(index);
    }

    /** 
     * Determines if the list is empty
     */
    public boolean isEmpty()
    {
	return ListBundNames.isEmpty();
    }

    /** 
     * get the nombre of elements in the list
     */
    public int size()
    {
	return ListBundNames.size();
    }

    private Vector ListBundNames;
    private int index;

}
