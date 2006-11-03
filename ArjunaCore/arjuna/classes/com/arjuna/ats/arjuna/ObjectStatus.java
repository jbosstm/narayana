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
 * $Id: ObjectStatus.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna;

import java.io.PrintWriter;

/**
 * A transactional object may go through a number of different states
 * once it has been created.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectStatus.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ObjectStatus
{

public static final int PASSIVE = 0;
public static final int PASSIVE_NEW = 1;
public static final int ACTIVE = 2;
public static final int ACTIVE_NEW = 3;
public static final int DESTROYED = 4;
public static final int UNKNOWN_STATUS = 5;
    
public static void print (PrintWriter strm, int os)
    {
	switch (os)
	{
	case PASSIVE:
	    strm.print("PASSIVE");
	    break;
	case PASSIVE_NEW:
	    strm.print("PASSIVE_NEW");
	    break;
	case ACTIVE:
	    strm.print("ACTIVE");
	    break;
	case ACTIVE_NEW:
	    strm.print("ACTIVE_NEW");
	    break;
	case DESTROYED:
	    strm.print("DESTROYED");
	    break;
	default:
	    strm.print("UNKNOWN_STATUS");
	    break;
	}
    }
    
}
