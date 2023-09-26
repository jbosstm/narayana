/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.extensions;

import com.arjuna.ats.internal.arjuna.FormatConstants;
import com.arjuna.ats.jts.logging.jtsLogger;

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
	return FormatConstants.JTS_FORMAT_ID;
    }

public static final int strictXID ()
    {
	return FormatConstants.JTS_STRICT_FORMAT_ID;
    }

public static final int restrictedXID ()
    {
    return FormatConstants.JTS_RESTRICTED_FORMAT_ID;
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
	    return Arjuna.XID();
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
            {
                jtsLogger.i18NLogger.warn_orbspecific_coordinator_ipunknown("Arjuna.nameToXID", name);
			    return Arjuna.XID();
            }
		    }
		}
	    }
	}
    }
 
}