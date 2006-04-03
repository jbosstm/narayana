/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
package com.arjuna.orbportability.internal;

import com.arjuna.orbportability.ORB;

import java.util.Hashtable;

/*
 * Copyright (C) 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: InternalORB.java 2342 2006-03-30 13:06:17Z  $
 */

public class InternalORB extends ORB
{
    protected InternalORB(String orbName)
    {
        super(orbName);
    }

    public static ORB getInstance(String uniqueId)
    {
        /**
         * Try and find this ORB in the hashmap first if
         * its not there then create one and add it
         */
        ORB orb = (ORB)_orbMap.get(uniqueId);

        if (orb == null)
        {
            orb = new InternalORB(uniqueId);

            _orbMap.put(uniqueId, orb);
        }

        return(orb);
    }

    private static Hashtable    _orbMap = new Hashtable();
}
