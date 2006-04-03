/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * $Id: ModifierFactory.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc.drivers.modifiers;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;

import java.util.*;

/**
 * Keep track of any "modifiers" we may require to run
 * transactions over specific databases.
 * A modifier implementation has two interfaces, one for XA processing
 * and is used by the JTA layer, and one for connection processing and
 * is used by the JDBC layer. A user needs to know which interface they
 * require and cast appropriately.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ModifierFactory.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class ModifierFactory
{

    public static synchronized void putModifier (String dbName, int major, int minor, String modclass)
    {
	try {
		Object mod = Thread.currentThread().getContextClassLoader().loadClass(modclass).newInstance();
	_modifiers.put(dbName+"_"+major+"_"+minor, mod);
	} catch (Exception e) {
	}
    }

    /*
     * Convert input to lower case first.
     */
    
    public static synchronized Object getModifier (String dbName, int major, int minor)
    {
	String exactMatch = null;
	String majorMatch = null;
	String driverMatch = null;
	Enumeration e = _modifiers.keys();
	
	dbName = dbName.toLowerCase();

	while (e.hasMoreElements())
	{
	    String s = (String) e.nextElement();

	    if (s.equalsIgnoreCase(dbName + "_" + major + "_" + minor))
		exactMatch = s;
	    if (s.equalsIgnoreCase(dbName + "_" + major + "_-1"))
		majorMatch = s;
	    if (s.equalsIgnoreCase(dbName + "_-1_-1"))
		driverMatch = s;
	}

	if (exactMatch != null)
		return _modifiers.get(exactMatch);
	if (majorMatch != null)
		return _modifiers.get(majorMatch);
	if (driverMatch != null)
		return _modifiers.get(driverMatch);

	return null;
    }    

    private static Hashtable _modifiers = new Hashtable();

    static
    {
	Object o = new list();
    }
    
}
