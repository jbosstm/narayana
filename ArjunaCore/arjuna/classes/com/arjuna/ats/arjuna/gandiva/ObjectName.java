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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ObjectName.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.gandiva;

import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.gandiva.inventory.Inventory;
import com.arjuna.ats.arjuna.gandiva.nameservice.NameService;
import java.io.PrintWriter;

import java.lang.NullPointerException;
import java.io.IOException;

/**
 * Instances of this class can be used to convey arbitrary configuration
 * information to classes. Simply by changing the data stored within an
 * instance, the configuration of a class (or entire application) may
 * be changed, without requiring it to be recompiled.
 *
 * Data is stored in a tuple-space <attribute name, attribute value>.
 * How data is stored and manipulated is also specified in a configurable
 * manner via the NameService object. The name of an ObjectName is of
 * the format: NS://<name>[data], where [data] may be optionally encoded
 * within the ObjectName string, or (more usual) stored elsewhere.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectName.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 *
 * @message com.arjuna.ats.arjuna.gandiva.ObjectName_1 [com.arjuna.ats.arjuna.gandiva.ObjectName_1] - no name service given.
 * @message com.arjuna.ats.arjuna.gandiva.ObjectName_2 [com.arjuna.ats.arjuna.gandiva.ObjectName_2] - no object name given.
 * @message com.arjuna.ats.arjuna.gandiva.ObjectName_3 [com.arjuna.ats.arjuna.gandiva.ObjectName_3] - NameService implementation not set
 */

public class ObjectName
{
    /**
     * All numbers are signed, but we leave this in for compatibility.
     */
    
public static final int SIGNED_NUMBER = 0;
public static final int UNSIGNED_NUMBER = 1;
public static final int STRING = 2;
public static final int OBJECTNAME = 3;
public static final int CLASSNAME = 4;
public static final int UID = 5;

    /**
     * Create a new ObjectName.
     */

public ObjectName (String objectName)
    {
	_mech = null;
	_name = null;
	_nameService = null;

	if (objectName == null)
	{
	    _mech = _invalidString;
	    _name = _invalidString;
	}
	else
	{
	    int mechLength = objectName.indexOf(nameDelim);

	    if (mechLength != -1)
	    {
		int nameLength = objectName.length() - mechLength - nameDelim.length();

		_mech = new String(objectName.substring(0, mechLength));

		if (nameLength > 0)
		    _name = objectName.substring((mechLength+nameDelim.length()), objectName.length());
		else
		    _name = new String("");
	    }
	}

	initNameService();
    }

    /**
     * Create a copy of an existing ObjectName.
     */

public ObjectName (ObjectName objectName)
    {
	_mech = null;
	_name = null;
	_nameService = null;

	this.copy(objectName);
    }

    /**
     * Create a new ObjectName using the specified NameService implementation,
     * and using the specified name.
     */

public ObjectName (NameService nameService, String objectName)
    {
	_mech = null;
	_name = null;
	_nameService = null;
	
	if ((nameService != null) && (objectName != null))
	{
	    _nameService = nameService;

	    String fullMechName = _nameService.impleClassName().stringForm();
	    int appendedString = fullMechName.indexOf(nameServiceString);

	    _mech = new String(fullMechName.substring(0, appendedString-fullMechName.length()));
	    _name = new String(objectName);
	}
	else
	{
	    if (nameService == null)
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.gandiva.ObjectName_1");
	
	    if (objectName == null)
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.gandiva.ObjectName_2");

	    _mech = _invalidString;
	    _name = _invalidString;
	}
    }

public void finalize ()
    {
	_mech = null;
	_name = null;
	_nameService = null;
    }

    /**
     * Return the type of the specified attribute.
     */

public int attributeType (String attrName) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    return _nameService.attributeType(_name, attrName);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Return the first attribute.
     */

public String firstAttributeName () throws NullPointerException, IOException
    {
	if (_nameService != null)
	    return _nameService.firstAttributeName(_name);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Return the next attribute after the specified attribute.
     */

public String nextAttributeName (String attrName) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    return _nameService.nextAttributeName(_name, attrName);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Return the long value of the specified attribute. If the
     * specified attribute is of the wrong type, then an IOException
     * will be thrown.
     */

public long getLongAttribute (String attrName) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    return _nameService.getLongAttribute(_name, attrName);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Return the String value of the specified attribute. If the
     * specified attribute is of the wrong type, then an IOException
     * will be thrown.
     */

public String getStringAttribute (String attrName) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    return _nameService.getStringAttribute(_name, attrName);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Return the ObjectName value of the specified attribute. If the
     * specified attribute is of the wrong type, then an IOException
     * will be thrown.
     */

public ObjectName getObjectNameAttribute (String attrName) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    return _nameService.getObjectNameAttribute(_name, attrName);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Return the ClassName value of the specified attribute. If the
     * specified attribute is of the wrong type, then an IOException
     * will be thrown.
     */

public ClassName getClassNameAttribute (String attrName) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    return _nameService.getClassNameAttribute(_name, attrName);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Return the Uid value of the specified attribute. If the
     * specified attribute is of the wrong type, then an IOException
     * will be thrown.
     */

public Uid getUidAttribute (String attrName) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    return _nameService.getUidAttribute(_name, attrName);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Set the value of the specified attribute to be the provided long
     * value.
     */

public void setLongAttribute (String attrName, long value) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    _name = _nameService.setLongAttribute(_name, attrName, value);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Set the value of the specified attribute to be the provided String
     * value.
     */

public void setStringAttribute (String attrName, String value) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    _name = _nameService.setStringAttribute(_name, attrName, value);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Set the value of the specified attribute to be the provided ObjectName
     * value.
     */

public void setObjectNameAttribute (String attrName, ObjectName value) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    _name = _nameService.setObjectNameAttribute(_name, attrName, value);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Set the value of the specified attribute to be the provided ClassName
     * value.
     */

public void setClassNameAttribute (String attrName, ClassName value) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    _name = _nameService.setClassNameAttribute(_name, attrName, value);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Set the value of the specified attribute to be the provided Uid
     * value.
     */

public void setUidAttribute (String attrName, Uid value) throws NullPointerException, IOException
    {
	if (_nameService != null)
	    _name = _nameService.setUidAttribute(_name, attrName, value);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Remove the specified attribute. Returns true if the attribute
     * was not null, false otherwise.
     */

public boolean removeAttribute (String attrName) throws IOException
    {
	if (_nameService != null)
	{
	    _name = _nameService.removeAttribute(_name, attrName);

	    if (_name != null)
		return true;
	}
	
	return false;
    }

    /**
     * Returns a new, unique attribute name. This name is only guaranteed
     * to be unique within the NameService implementation used by this
     * ObjectName instance.
     */

public String uniqueAttributeName () throws NullPointerException, IOException
    {
	if (_nameService != null)
	    return _nameService.uniqueAttributeName(_name);
	else
	    throw new NullPointerException(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.gandiva.ObjectName_3"));
    }

    /**
     * Returns a new, unique ObjectName, using the NameService mechanism
     * specified.
     */

public static ObjectName uniqueObjectName (String mech) throws NullPointerException, IOException
    {
	NameService nameService = null;
	ObjectName objectName = ObjectName.invalid();

	if ((mech != null) && (mech.compareTo(_invalidString) != 0))
	{
	    String nameServiceMech = mech + nameServiceString;
	    ClassName nameServiceMechClassName = new ClassName(nameServiceMech);
	    nameService = NameService.create(nameServiceMechClassName);

	    if (nameService != null)
		objectName = nameService.uniqueObjectName();
	    
	    nameService = null;
	}

	return objectName;
    }

    /**
     * Copy the ObjectName provided into the current ObjectName.
     */

public synchronized void copy (ObjectName objectName)
    {
	if (objectName == this)
	    return;
	
	_nameService = null;
        _mech = null;
        _name = null;

	if (objectName._mech != null)
	{
	    _mech = new String(objectName._mech);

	    if (objectName._nameService != null)
		_nameService = new NameService(objectName._nameService);
	}

	if (objectName._name != null)
	    _name = new String(objectName._name);

	initNameService();
    }

    /**
     * Overrides Object.equals()
     */

public boolean equals (Object o)
    {
	if (o instanceof ObjectName)
	    return equals((ObjectName) o);
	else
	    return false;
    }
    
    /**
     * Are the two ObjectNames equal?
     */

public boolean equals (ObjectName objectName)
    {
	return (((_name == objectName._name) || 
		((_name != null) && (objectName._name != null) && (_name.compareTo(objectName._name) == 0))) &&
	    ((_mech == objectName._mech) ||
	     ((_mech != null) && (objectName._mech != null) && (_mech.compareTo(objectName._mech) == 0))));
    }

    /**
     * Are the two ObjectNames not equal?
     */

public boolean notEquals (ObjectName objectName)
    {
	return (((_name != objectName._name) && 
		((_name == null) || (objectName._name == null) || (_name.compareTo(objectName._name) != 0))) ||
	    ((_mech != objectName._mech) &&
	     ((_mech == null) || (objectName._mech == null) || (_mech.compareTo(objectName._mech) != 0))));
    }

    /**
     * Return the type of the NameService implementation used by this
     * ObjectName instance.
     */

public String getMechanismName ()
    {
	return _mech;
    }

    /**
     * Provide a string form of the ObjectName.
     */

public String stringForm ()
    {
	return new String(_mech + nameDelim + _name);
    }

    /**
     * Is the ObjectName valid?
     */

public static ObjectName invalid ()
    {
	return _invalid;
    }

public void print (PrintWriter strm)
    {
	strm.print("<ObjectName:"+_mech+nameDelim+_name+">");
    }

public String toString ()
    {
	return stringForm();
    }

    /**
     * Print a human-readable for of the various attribute types.
     */

public static void print (int t)
    {
	switch (t)
	{
	case ObjectName.SIGNED_NUMBER:
	    System.out.print("SIGNED_NUMBER");
	    break;
	case ObjectName.UNSIGNED_NUMBER:
	    System.out.print("UNSIGNED_NUMBER");
	    break;
	case ObjectName.STRING:
	    System.out.print("STRING");
	    break;
	case ObjectName.OBJECTNAME:
	    System.out.print("OBJECTNAME");
	    break;
	case ObjectName.CLASSNAME:
	    System.out.print("CLASSNAME");
	    break;
	case ObjectName.UID:
	    System.out.print("UID");
	    break;
	default:
	    System.out.print("Unknown");
	    break;
	}
    }
    
private final void initNameService ()
    {
	if (_nameService != null)
	    return;

	if ((_mech != null) && (_mech.compareTo(_invalidString) != 0))
	{
	    String nameServiceMech = new String(_mech + nameServiceString);
	    ClassName nameServiceMechClassName = new ClassName(nameServiceMech);
	    
	    _nameService = NameService.create(nameServiceMechClassName);
	    
	    nameServiceMech = null;
	}
	else
	    _nameService = null;
    }

private String      _mech;
private String      _name;
private NameService _nameService;
    
private static final String _invalidString = "invalid";
private static final String nameServiceString = "NameServiceImple";
private static final String nameDelim = ":";
private static final String nullString = "";
private static final ObjectName _invalid = new ObjectName("invalid:invalid");

}
