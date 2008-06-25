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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ORBType.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability;

/**
 * The various types of ORB that are supported.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ORBType.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class ORBType
{

    /**
     * Returns the enumerated value for the orb with the given name.
     * If this ORB is not known it will return -1.
     *
     * @param name The name of the ORB to find.
     * @return The enumerated value for this ORB.
     */
    static int getORBEnum(String name)
    {
        for (int count=0;count<ORB_NAME.length;count++)
        {
            if ( ORB_NAME[count].equals( name ) )
            {
                return ORB_ENUM[count];
            }
        }

        return -1;
    }


    public static final int ORBIX2000 = 0;
    public static final int HPORB = 2;
    public static final int VISIBROKER = 3;
    public static final int JAVAIDL = 1;
	public static final int JACORB = 4;

    public static final String orbix2000 = "ORBIX2000";
    public static final String hporb = "HPORB";
    public static final String visibroker = "VISIBROKER";
    public static final String javaidl = "JAVAIDL";
	public static final String jacorb = "JACORB";

    private final static String[]           ORB_NAME = { orbix2000, hporb, visibroker, javaidl, jacorb };
    private final static int[]              ORB_ENUM = { ORBIX2000, HPORB, VISIBROKER, JAVAIDL, JACORB };
}
