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
 * $Id: PNS.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.gandiva.nameservice;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import com.arjuna.ats.arjuna.gandiva.nameservice.*;
import java.util.*;
import java.io.*;

import java.io.IOException;
import java.lang.NumberFormatException;
import java.lang.StringIndexOutOfBoundsException;

/**
 * A naming service implementation based on Java Properties.
 *
 * Note: because Properties are basically hash tables, we have no
 * control over how they are internally laid out. So the notion of
 * 'first' and 'next' attributes is tenuous and can change from one
 * call to the next. So, this implementation is really only of use
 * for those ObjectNames which do not care about the order of attributes.
 *
 * Could examine ways of returning the actual object name object inserted
 * into another object name by storing pointer/uid and looking this up
 * in some table rather than recreating a copy of the object name as we
 * tend to do now.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: PNS.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class PNS extends NameServiceImple
{

public PNS ()
    {
	_properties = new Properties();
	_name = null;
	_initialized = false;
    }

public PNS (PNS impl)
    {
	_properties = new Properties(impl._properties);

	if (impl._name != null)
	    _name = new String(impl._name);
	else
	    _name = null;

	_initialized = impl._initialized;
    }
    
public int attributeType (String objName, String attrName) throws IOException
    {
	String attr = _properties.getProperty(attrName, null);

	if (attr == null)
	    throw new IOException("No such attribute.");
	else
	{
	    if (attr.charAt(0) == PNS.SIGNED_NUMBER)
		return ObjectName.SIGNED_NUMBER;
	    else if (attr.charAt(0) == PNS.UNSIGNED_NUMBER)
		return ObjectName.UNSIGNED_NUMBER;
	    else if (attr.charAt(0) == PNS.STRING)
		return ObjectName.STRING;
	    else if (attr.charAt(0) == PNS.OBJECTNAME)
		return ObjectName.OBJECTNAME;
	    else if (attr.charAt(0) == PNS.CLASSNAME)
		return ObjectName.CLASSNAME;
	    else if (attr.charAt(0) == PNS.UID)
		return ObjectName.UID;
	    else
		throw new IOException("Unknown attribute type.");
	}
    }

public synchronized String firstAttributeName (String objName) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (_properties.isEmpty())
	    throw new IOException("No attributes.");	    
	else
	{
	    Enumeration keys = _properties.keys();
	    String firstAttr = (String) keys.nextElement();

	    keys = null;

	    return firstAttr;
	}
    }
    
public synchronized String nextAttributeName (String objName, String attrName) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	/*
	 * Find the current attribute. This can change from one call to the
	 * next if we keep adding things. We have no control over how the
	 * property (hash) table is maintained.
	 */

	if (_properties.isEmpty())
	    throw new IOException("No attributes.");	    
	else
	{
	    Enumeration keys = _properties.keys();

	    while (keys.hasMoreElements())
	    {
		String attr = (String) keys.nextElement();

		if (attr.compareTo(attrName) == 0)
		{
		    if (keys.hasMoreElements())
		    {
			attr = (String) keys.nextElement();

			keys = null;

			return attr;
		    }
		}
	    }

	    throw new IOException("No more attributes.");
	}
    }

public long getLongAttribute (String objName, String attrName) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (_properties.isEmpty())
	    throw new IOException("No attributes.");
	
	String attr = _properties.getProperty(attrName, null);

	if (attr != null)
	{
	    if (attr.charAt(0) == PNS.SIGNED_NUMBER)
	    {
		try
		{
		    return Long.parseLong(new String(attr.substring(1)));
		}
		catch (NumberFormatException e)
		{
		    throw new IOException("Not a number.");
		}
		catch (StringIndexOutOfBoundsException e)
		{
		    throw new IOException("Not a number.");
		}
	    }
	    else
		throw new IOException("Not a signed number.");
	}
	else
	    throw new IOException("No such attribute.");
    }
    
public String getStringAttribute (String objName, String attrName) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (_properties.isEmpty())
	    throw new IOException("No attributes.");
	
	String attr = _properties.getProperty(attrName, null);

	if (attr != null)
	{
	    if (attr.charAt(0) == PNS.STRING)
	    {
		try
		{
		    return new String(attr.substring(1));
		}
		catch (StringIndexOutOfBoundsException e)
		{
		    throw new IOException("No string.");
		}
	    }
	    else
		throw new IOException("Not a string.");
	}
	else
	    throw new IOException("No such attribute.");
    }

public ObjectName getObjectNameAttribute (String objName, String attrName) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (_properties.isEmpty())
	    throw new IOException("No attributes.");
	
	String attr = _properties.getProperty(attrName, null);

	if (attr != null)
	{
	    if (attr.charAt(0) == PNS.OBJECTNAME)
	    {
		try
		{
		    return new ObjectName(new String(attr.substring(1)));
		}
		catch (StringIndexOutOfBoundsException e)
		{
		    throw new IOException("No ObjectName.");
		}
	    }
	    else
		throw new IOException("Not an ObjectName.");
	}
	else
	    throw new IOException("No such attribute.");	
    }
    
public ClassName getClassNameAttribute (String objName, String attrName) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (_properties.isEmpty())
	    throw new IOException("No attributes.");
	
	String attr = _properties.getProperty(attrName, null);

	if (attr != null)
	{
	    if (attr.charAt(0) == PNS.CLASSNAME)
	    {
		try
		{
		    return new ClassName(new String(attr.substring(1)));
		}
		catch (StringIndexOutOfBoundsException e)
		{
		    throw new IOException("No ClassName.");
		}
	    }
	    else
		throw new IOException("Not a ClassName.");
	}
	else
	    throw new IOException("No such attribute.");	
    }
    
public Uid getUidAttribute (String objName, String attrName) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (_properties.isEmpty())
	    throw new IOException("No attributes.");
	
	String attr = _properties.getProperty(attrName, null);

	if (attr != null)
	{
	    if (attr.charAt(0) == PNS.UID)
	    {
		try
		{
		    return new Uid(new String(attr.substring(1)));
		}
		catch (StringIndexOutOfBoundsException e)
		{
		    throw new IOException("No Uid.");
		}
	    }
	    else
		throw new IOException("Not a Uid.");
	}
	else
	    throw new IOException("No such attribute.");	
    }

    /**
     * For all set attributes we check that there is not an attribute
     * with the same name already in the table. If so we throw an
     * exception.
     */
    
public String setLongAttribute (String objName, String attrName, long value) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (!_properties.isEmpty())
	{
	    if (_properties.get(attrName) != null)
		throw new IOException("Attribute "+attrName+" already present.");
	}

	Long l = new Long(value);
	String s = new String(PNS.SIGNED_NUMBER+l.toString());
	
	_properties.put(attrName, s);

	l = null;
	s = null;

	return saveState();
    }
    
public String setStringAttribute (String objName, String attrName, String value) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (!_properties.isEmpty())
	{
	    if (_properties.get(attrName) != null)
		throw new IOException("Attribute "+attrName+" already present.");
	}

	String s = new String(PNS.STRING+value);
	
	_properties.put(attrName, s);

	s = null;

	return saveState();
    }
    
public String setObjectNameAttribute (String objName, String attrName, ObjectName value) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (!_properties.isEmpty())
	{
	    if (_properties.get(attrName) != null)
		throw new IOException("Attribute "+attrName+" already present.");
	}

	String s = new String(PNS.OBJECTNAME+value.stringForm());
	
	_properties.put(attrName, s);

	s = null;

	return saveState();
    }
    
public String setClassNameAttribute (String objName, String attrName, ClassName value) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (!_properties.isEmpty())
	{
	    if (_properties.get(attrName) != null)
		throw new IOException("Attribute "+attrName+" already present.");
	}

	String s = new String(PNS.CLASSNAME+value.stringForm());
	
	_properties.put(attrName, s);

	s = null;

	return saveState();
    }
    
public String setUidAttribute (String objName, String attrName, Uid value) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (!_properties.isEmpty())
	{
	    if (_properties.get(attrName) != null)
		throw new IOException("Attribute "+attrName+" already present.");
	}

	String s = new String(PNS.UID+value.stringForm());
	
	_properties.put(attrName, s);

	s = null;

	return saveState();
    }

public String removeAttribute (String objName, String attrName) throws IOException
    {
	if (!_initialized)
	    loadState(objName);
	
	if (_properties.isEmpty())
	    throw new IOException("Attribute "+attrName+" not present.");
	else
	{
	    _properties.remove(attrName);
	    
	    return saveState();
	}
    }
    
public String uniqueAttributeName (String objName) throws IOException
    {
	Uid u = new Uid();

	return u.stringForm();
    }
    
public ObjectName uniqueObjectName () throws IOException
    {
	Uid uid = new Uid();
	ObjectName uniqueName = new ObjectName(PNS.pnsName+uid.stringForm());

	return uniqueName;
    }

public Object clone ()
    {
	return new PNS(this);
    }

public ClassName className ()
    {
	return ArjunaNames.Implementation_NameService_PNS();
    }

public static ClassName name ()
    {
	return ArjunaNames.Implementation_NameService_PNS();
    }

private void loadState (String objName) throws IOException
    {
	if (!_initialized)
	{
	    _initialized = true;

	    if (objName != null)
	    {
		int namePoint = objName.indexOf(nameStart);

		if (namePoint == -1)
		    _name = new String(objName);
		else
		{
		    _name = objName.substring(0, namePoint-1);

		    String nameString = objName.substring(namePoint, objName.length());
		    DataInputStream input = new DataInputStream(new ByteArrayInputStream(nameString.getBytes()));

		    _properties.load(input);

		    nameString = null;
		    input = null;
		}
	    }
	    else
		_name = "";
	}
    }

private String saveState () throws IOException
    {
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	DataOutputStream output = new DataOutputStream(os);

	_properties.store(output, null);
	
	output = null;
	
	return new String(_name+nameStart+os.toString());
    }
    
private Properties _properties;
private String     _name;
private boolean    _initialized;
private Uid        _id;
    
private static final char SIGNED_NUMBER = '#';
private static final char UNSIGNED_NUMBER = '~';
private static final char STRING = '^';
private static final char OBJECTNAME = '%';
private static final char CLASSNAME = '-';
private static final char UID = '+';

private static final char   nameStart = '!';
private static final String pnsName = "PNS:";
    
};
