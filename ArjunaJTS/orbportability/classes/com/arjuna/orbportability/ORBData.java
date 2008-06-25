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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ORBData.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability;

/**
 * ORB specific data objects implement this interface to provide
 * runtime information about the ORB.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ORBData.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public interface ORBData
{
    /**
     * Retrieve the XML fragment which contains runtime information about the orb.
     *
     * @return A string containing the XML fragment.
     */
    public String getORBdata();
}

