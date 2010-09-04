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
 * $Id: RecoveryORBManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.*;

/**
 * Manage the recovery POA.
 */ 

public class RecoveryORBManager
{

    public static final com.arjuna.orbportability.ORB getORB ()
    {
       return ORBManager.getORB();
    }

    public static final boolean setORB (com.arjuna.orbportability.ORB theOrb)
    {
        return ORBManager.setORB(theOrb);
    }

    public static final com.arjuna.orbportability.OA getPOA ()
    {
        if (_thePoa == null)
            _thePoa = RootOA.getRootOA(ORBManager.getORB());

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
        return ORBManager.getServices();
    }

    public static final boolean isInitialised ()
    {
        return (ORBManager.isInitialised() || _thePoa != null);
    }

    private static com.arjuna.orbportability.OA _thePoa = null;
}
