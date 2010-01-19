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
 * $Id: ObjectStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.objectstore;

import java.io.PrintWriter;

/**
 * The type of the state in the ObjectStore.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class StateType
{
    /**
     * StateType.
     */

    public static final int OS_SHADOW = 10;
    public static final int OS_ORIGINAL = 11;
    public static final int OS_INVISIBLE = 12;

    public static final int OS_SHARED = 13;
    public static final int OS_UNSHARED = 14;
    
    public static void printStateType (PrintWriter strm, int res)
    {
        strm.print(stateTypeString(res));
    }

    public static String stateTypeString (int res)
    {
        switch (res)
        {
        case StateType.OS_SHADOW:
            return "StateType.OS_SHADOW";
        case StateType.OS_ORIGINAL:
            return "StateType.OS_ORIGINAL";
        case StateType.OS_INVISIBLE:
            return "StateType.OS_INVISIBLE";
        default:
            return "Illegal";
        }
    }
    
    private StateType ()
    {
    }
}

