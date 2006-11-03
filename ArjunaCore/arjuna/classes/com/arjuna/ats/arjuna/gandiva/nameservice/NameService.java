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
 * $Id: NameService.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.gandiva.nameservice;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.gandiva.inventory.Inventory;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.ObjectName;

import java.io.IOException;
import java.lang.NullPointerException;

/**
 * NameService implementations are typically used by ObjectName to
 * store and retrieve object data in a manner specific to the NameService.
 * So, for example, one such implementation may use a database, whereas
 * another may use a remote property service. These details are hidden
 * by the implementation.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: NameService.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 *
 * @message com.arjuna.ats.arjuna.gandiva.nameservice.NameService_1 [com.arjuna.ats.arjuna.gandiva.nameservice.NameService_1] - Implementation not set
 */

public class NameService
{

    /**
     * Create a new NameService using the specific implementation.
     */

public NameService (ClassName nameServiceImpleName)
    {
	Inventory invent = Inventory.inventory();

	if (invent != null)
	{
	    Object ptr = invent.createVoid(nameServiceImpleName);
	    
	    if (ptr instanceof NameServiceImple)
		_imple = (NameServiceImple) ptr;
	    else
		_imple = null;
	}
    }

    /**
     * Create a new NameService which is a copy of the one
     * specified.
     */

public NameService (NameService nameService)
    {
	if (nameService._imple != null)
	{
	    _imple = (NameServiceImple) nameService._imple.clone();
	}
	else
	    _imple = null;
    }

    /**
     * Create a new NameService using the specific implementation.
     */

public NameService (NameServiceImple nameServiceImple)
    {
	_imple = nameServiceImple;
    }

public void finalize ()
    {
	_imple = null;
    }

    /**
     * Return the type of the attribute. The ObjectName identity is given
     * to allow implementation specific lookups.
     */

public int attributeType (String objName, String attr) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.attributeType(objName, attr);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Return the first attribute. The ObjectName identity is given
     * to allow implementation specific lookups.
     */

public String firstAttributeName (String objName) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.firstAttributeName(objName);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Return the next attribute. The ObjectName identity is given
     * to allow implementation specific lookups.
     */

public String nextAttributeName (String objName, String attrName) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.nextAttributeName(objName, attrName);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Return the named long attribute. The ObjectName identity is given
     * to allow implementation specific lookups. If the specified attribute
     * is not of the correct type then an IOException will be thrown.
     */

public long getLongAttribute (String objName, String attrName) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.getLongAttribute(objName, attrName);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Return the named String attribute. The ObjectName identity is given
     * to allow implementation specific lookups. If the specified attribute
     * is not of the correct type then an IOException will be thrown.
     */

public String getStringAttribute (String objName, String attrName) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.getStringAttribute(objName, attrName);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Return the named ObjectName attribute. The ObjectName identity is given
     * to allow implementation specific lookups. If the specified attribute
     * is not of the correct type then an IOException will be thrown.
     */

public ObjectName getObjectNameAttribute (String objName, String attrName) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.getObjectNameAttribute(objName, attrName);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Return the named ClassName attribute. The ObjectName identity is given
     * to allow implementation specific lookups. If the specified attribute
     * is not of the correct type then an IOException will be thrown.
     */

public ClassName getClassNameAttribute (String objName, String attrName) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.getClassNameAttribute(objName, attrName);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Return the named Uid attribute. The ObjectName identity is given
     * to allow implementation specific lookups. If the specified attribute
     * is not of the correct type then an IOException will be thrown.
     */

public Uid getUidAttribute (String objName, String attrName) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.getUidAttribute(objName, attrName);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Set the named long attribute. The ObjectName identity is given
     * to allow implementation specific lookups. A potentially modified
     * ObjectName identity is returned.
     */

public String setLongAttribute (String objName, String attrName, long value) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.setLongAttribute(objName, attrName, value);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Set the named String attribute. The ObjectName identity is given
     * to allow implementation specific lookups. A potentially modified
     * ObjectName identity is returned.
     */

public String setStringAttribute (String objName, String attrName, String value) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.setStringAttribute(objName, attrName, value);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Set the named ObjectName attribute. The ObjectName identity is given
     * to allow implementation specific lookups. A potentially modified
     * ObjectName identity is returned.
     */

public String setObjectNameAttribute (String objName, String attrName, ObjectName value) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.setObjectNameAttribute(objName, attrName, value);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Set the named ClassName attribute. The ObjectName identity is given
     * to allow implementation specific lookups. A potentially modified
     * ObjectName identity is returned.
     */

public String setClassNameAttribute (String objName, String attrName, ClassName value) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.setClassNameAttribute(objName, attrName, value);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Set the named Uid attribute. The ObjectName identity is given
     * to allow implementation specific lookups. A potentially modified
     * ObjectName identity is returned.
     */

public String setUidAttribute (String objName, String attrName, Uid value) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.setUidAttribute(objName, attrName, value);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Remove the specified attribute and return a potentially modified
     * ObjectName identity.
     */

public String removeAttribute (String objName, String attrName) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.removeAttribute(objName, attrName);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Return a unique (within the scope of this implementation) attribute
     * name.
     */

public String uniqueAttributeName (String objName) throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.uniqueAttributeName(objName);
	else
	    throw new NullPointerException("Implementation not set");
    }

    /**
     * Return a unique (within the scope of this implementation) ObjectName,
     * which uses this NameService.
     */

public ObjectName uniqueObjectName () throws NullPointerException, IOException
    {
	if (_imple != null)
	    return _imple.uniqueObjectName();
	else
	    throw new NullPointerException("Implementation not set");
    }

public void copy (NameService toCopy)
    {
	if (this == toCopy)
	    return;
    
	_imple = (NameServiceImple) toCopy._imple.clone();
    }
	
public static ClassName name ()
    {
	return ArjunaNames.Interface_NameService();
    }

public ClassName className ()
    {
	return null;
    }

public ClassName impleClassName ()
    {
	if (_imple != null)
	    return _imple.className();
	else
	    return ClassName.invalid();
    }

public NameService castup (ClassName theType)
    {
	if (theType.equals(className()))
	    return this;
	else
	    return null;
    }
    
public static NameService create (ClassName nameServiceImpleName)
    {
	NameService res = null;
	Inventory invent = Inventory.inventory();

	if (invent != null)
	{
	    NameServiceImple nameServiceImple = null;
	    Object ptr = invent.createVoid(nameServiceImpleName);

	    if (ptr instanceof NameServiceImple)
		nameServiceImple = (NameServiceImple) ptr;
	    else
		nameServiceImple = null;
	
	    if (nameServiceImple != null)
		res = new NameService(nameServiceImple);
	}

	return res;
    }

private NameServiceImple _imple;
    
}
