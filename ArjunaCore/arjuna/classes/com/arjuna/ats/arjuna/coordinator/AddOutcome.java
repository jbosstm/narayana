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
 * $Id: AddOutcome.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import java.io.PrintWriter;

/**
 * The possible outcomes when trying to add an AbstractRecord as
 * a participant within a transaction.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: AddOutcome.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class AddOutcome
{
    
public static final int AR_ADDED = 2;
public static final int AR_REJECTED = 3;
public static final int AR_DUPLICATE = 4;

    /**
     * @since JTS 2.1.2.
     */

public static String printString (int res)
    {
	switch (res)
	{
	case AR_ADDED:
	    return "AddOutcome.AR_ADDED";
	case AR_REJECTED:
	    return "AddOutcome.AR_REJECTED";
	case AR_DUPLICATE:
	    return "AddOutcome.AR_DUPLICATE";
	default:
	    return "Unknown";
	}
    }

public static void print (PrintWriter strm, int res)
    {
	strm.print(printString(res));
    }
    
}
