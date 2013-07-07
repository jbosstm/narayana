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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryStatus.java 2342 2006-03-30 13:06:17Z  $
 */

/*
 * Status flags for recovered transactions.
 * <P>
 * @author Dave Ingham (dave@arjuna.com)
 * @version $Id: RecoveryStatus.java 2342 2006-03-30 13:06:17Z  $ */

package com.arjuna.ats.internal.jts.recovery.transactions;

public class RecoveryStatus
{
    public static final int NEW = 0;
    public static final int ACTIVATED = 1;
    public static final int ACTIVATE_FAILED = 2;
    public static final int REPLAYING = 3;
    public static final int REPLAYED = 4;
    public static final int REPLAY_FAILED = 5; 
    
    /**
     * @return <code>String</code> representation of the status.
     */

    public static String stringForm (int res)
    {
        switch (res)
        {
        case NEW:
            return "RecoveryStatus.NEW";
        case ACTIVATED:
            return "RecoveryStatus.ACTIVATED";
        case ACTIVATE_FAILED:
            return "RecoveryStatus.ACTIVATE_FAILED";
        case REPLAYING:
            return "RecoveryStatus.REPLAYING";
        case REPLAYED:
            return "RecoveryStatus.REPLAYED";
        case REPLAY_FAILED:
            return "RecoveryStatus.REPLAY_FAILED";
        default:
            return "Unknown";
        }
    }
}

