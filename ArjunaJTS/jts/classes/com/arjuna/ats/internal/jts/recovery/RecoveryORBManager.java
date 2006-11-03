/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: RecoveryORBManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery;

import com.arjuna.orbportability.*;

/**
 * Manage the default ORB and POA used by the transaction service for
 * the non-recoverable CORBA objects it creates.
 */

// TODO error checking

public class RecoveryORBManager
{

    public static final com.arjuna.orbportability.ORB getORB ()
    {
	if (_theOrb == null)
	    _theOrb = ORB.getInstance(ORB_NAME);
	
	return _theOrb;
    }
    
    public static final boolean setORB (com.arjuna.orbportability.ORB theOrb)
    {
	if (_theOrb == null)
	{
	    _theOrb = theOrb;
	    
	    return true;
	}
	
	return false;
    }

    public static final com.arjuna.orbportability.OA getPOA ()
    {
	if (_thePoa == null)
	    _thePoa = RootOA.getRootOA(_theOrb);
	
	return _thePoa;
    }

    public static final boolean setPOA (com.arjuna.orbportability.OA thePoa)
    {
	if (_thePoa == null)
	{
	    _thePoa = thePoa;
	    
	    return true;
	}
	
	return false;
    }

    public static final com.arjuna.orbportability.Services getServices ()
    {
	return new com.arjuna.orbportability.Services(_theOrb);
    }

    public static final boolean isInitialised()
    {
        return( _theOrb != null || _thePoa != null );
    }

    private static com.arjuna.orbportability.ORB _theOrb = null;
    private static com.arjuna.orbportability.OA  _thePoa = null;

    private static final String ORB_NAME = "RecoveryORB";
    private static final String POA_NAME = "RecoveryPOA";

}
