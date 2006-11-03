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
 * $Id: ObjectModel.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna;

import java.io.PrintWriter;

/**
 * An enumeration of the types of object model supported.
 * Based upon the model type, certain optimisations may be
 * used.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ObjectModel.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ObjectModel
{

    /**
     * In the SINGLE model, it is assumed that only a single instance
     * of the object will exist within a single JVM.
     */

public static final int SINGLE = 0;

    /**
     * In the MULTIPLE model, it is assumed that multiple instances of
     * the object may exist in different JVMs concurrently.
     */

public static final int MULTIPLE = 1;

    /**
     * Print out a human-readable form of the model type.
     */

public static void print (PrintWriter strm, int os)
    {
	switch (os)
	{
	case SINGLE:
	    strm.print("SINGLE");
	    break;
	case MULTIPLE:
	    strm.print("MULTIPLE");
	    break;
	}
    }
    
}
