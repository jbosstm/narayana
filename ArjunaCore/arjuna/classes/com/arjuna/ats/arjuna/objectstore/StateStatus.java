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
 * The status of states in the ObjectStore.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class StateStatus
{
    /**
     * StateStatus
     */
    
    public static final int OS_UNKNOWN = -1;  // means no state present.
    
    public static final int OS_COMMITTED = 1;
    public static final int OS_UNCOMMITTED = 2;
    public static final int OS_HIDDEN = 4;
    public static final int OS_COMMITTED_HIDDEN = StateStatus.OS_COMMITTED | StateStatus.OS_HIDDEN;
    public static final int OS_UNCOMMITTED_HIDDEN = StateStatus.OS_UNCOMMITTED | StateStatus.OS_HIDDEN;

    public static void printStateStatus (PrintWriter strm, int res)
    {
        strm.print(stateStatusString(res));
    }

    public static String stateStatusString (int res)
    {
        switch (res)
        {
        case StateStatus.OS_UNKNOWN:
            return "StateStatus.OS_UNKNOWN";
        case StateStatus.OS_COMMITTED:
            return "StateStatus.OS_COMMITTED";
        case StateStatus.OS_UNCOMMITTED:
            return "StateStatus.OS_UNCOMMITTED";
        case StateStatus.OS_HIDDEN:
            return "StateStatus.OS_HIDDEN";
        case StateStatus.OS_COMMITTED_HIDDEN:
            return "StateStatus.OS_COMMITTED_HIDDEN";
        case StateStatus.OS_UNCOMMITTED_HIDDEN:
            return "StateStatus.OS_UNCOMMITTED_HIDDEN";
        default:
            return "Illegal";
        }
    }
    
    private StateStatus ()
    {
    }
}

