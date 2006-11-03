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
 * $Id: KeyedList.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.template;

import com.arjuna.ats.arjuna.logging.tsLogger;

import java.util.Hashtable;

/*
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: KeyedList.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class KeyedList
{

public KeyedList ()
    {
	_theTable = new Hashtable();
    }

public void finalise ()
    {
	_theTable.clear();
	_theTable = null;
    }

public synchronized void add (Object obj, String id)
    {
	_theTable.put(id, new KeyedElement(obj, id));
    }
    
public synchronized Object get (String id)
    {
	KeyedElement ptr = (KeyedElement) _theTable.get(id);

	if (ptr != null)
	{
	    /*
	     * Now increment reference count.
	     */
	
	    ptr.ref();
	    
	    return ptr.object();
	}
	else
	    return null;
    }

    /*
     * Will delete if ref count == 0.
     */

public synchronized void unref (String id)
    {
	KeyedElement ptr = (KeyedElement) _theTable.get(id);

	if (ptr != null)
	{
	    if (ptr.unref())
	    {
		_theTable.remove(id);
	    }
	}
    }
    
private Hashtable _theTable;
    
}

class KeyedElement
{

public KeyedElement (Object obj, String key)
    {
	_obj = obj;
	_key = key;
	_refCount = 1;
    }

    /**
     * @message com.arjuna.ats.internal.arjuna.template.KeyedList_1 [com.arjuna.ats.internal.arjuna.template.KeyedList_1] - KeyedElement deleting object {0} with ref count {1}
     */
public void finalize ()
    {
	if (_refCount != 0)
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
				//TBR made _refCount as new new Integer(_refCount)
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.template.KeyedList_1",
					    new Object[]{_key, new Integer(_refCount)});
	    }
	}

	_obj = null;
    }

public int refCount ()
    {
	return _refCount;
    }

public Object object ()
    {
	return _obj;
    }

public String key ()
    {
	return _key;
    }

public void ref ()
    {
	_refCount++;
    }

public boolean unref ()
    {
	return (boolean) (--_refCount == 0);
    }

private Object _obj;
private String _key;
private int _refCount;


};










