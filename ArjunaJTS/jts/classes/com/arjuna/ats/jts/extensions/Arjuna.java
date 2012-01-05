/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Arjuna.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.extensions;

/**
 * To get the formatID used to represent JBoss transactions
 * to the system.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Arjuna.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class Arjuna
{
    
public static final int XID ()
    {
	return 131072;
    }

public static final int strictXID ()
    {
	return 131073;
    }

public static final int restrictedXID ()
    {
	return 131074;
    }
    
public static final String arjunaXID ()
    {
	return "ArjunaXID";
    }

public static final String arjunaStrictXID ()
    {
	return "ArjunaStrictXID";
    }
  
public static final String arjunaRestrictedXID ()
    {
	return "ArjunaRestrictedXID";
    }

public static final String osiXID ()
    {
	return "OSI";
    }
    
public static final int nameToXID (String name)
    {
	if (name == null)
	    return -1;
	else
	{
	    if (name.compareTo(Arjuna.arjunaXID()) == 0)
		return Arjuna.XID();
	    else
	    {
		if (name.compareTo(Arjuna.arjunaStrictXID()) == 0)
		    return Arjuna.strictXID();
		else
		{
		    if (name.compareTo(Arjuna.arjunaRestrictedXID()) == 0)
			return Arjuna.restrictedXID();
		    else
		    {
			if (name.compareTo(Arjuna.osiXID()) == 0)
			    return 0; // osi tp
			else
			    return -1;
		    }
		}
	    }
	}
    }
 
}
