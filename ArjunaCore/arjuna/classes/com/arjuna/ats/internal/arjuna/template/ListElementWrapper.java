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
 * $Id: ListElementWrapper.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.template;

import com.arjuna.ats.arjuna.common.Uid;

/**
 * For those objects which cannot provide get_uid, but which
 * need to be added to a list.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ListElementWrapper.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ListElementWrapper implements ListElement
{

    /**
     * Create a new instance with the specified Uid.
     */

public ListElementWrapper (Object o, Uid u)
    {
	_value = o;
	_uid = u;
    }

    /**
     * Create a new instance. A new Uid will be automatically created
     * for this item.
     */

public ListElementWrapper (Object o)
    {
	_value = o;
 	_uid = new Uid();
    }

    /**
     * Return the Uid for the item.
     */

public Uid get_uid ()
    {
	return _uid;
    }

    /**
     * Return the item.
     */

public Object value ()
    {
	return _value;
    }

private Object _value;
private Uid    _uid;
    
}
