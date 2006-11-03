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
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: SimpleListIterator.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.template;

/**
 * An iterator for SimpleLists.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: SimpleListIterator.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class SimpleListIterator
{

    /**
     * Create a new iterator, which whill be used to iterate
     * over the provided list.
     */

public SimpleListIterator (SimpleList list)
    {
	nextEnt = list.head_;
    }

    /**
     * Return the next entry in the list.
     */

public synchronized ListElement iterate ()
    {
	ListElement th = null;

	if (nextEnt != null)
	{
	    th = nextEnt.theData;
	    nextEnt = nextEnt.next;
	}

	return th;
    }

private SimpleListEntry nextEnt;
    
};
