/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
package com.arjuna.ats.txoj.logging;

/**
 * The various levels of facility codes that are available.
 * Typically one for each major component in the system.
 *
 * <ul>
 * <li>FAC_CONCURRENCY_CONTROL = 0x0000002 (concurrency control classes).
 * <li>FAC_LOCK_STORE = 0x00010000 (lock store implementations).
 * </ul>
 *
 */
 
public class FacilityCode extends com.arjuna.common.util.logging.FacilityCode
{

public static final long FAC_CONCURRENCY_CONTROL = 0x00000002;
public static final long FAC_LOCK_STORE = 0x00010000;

public long getLevel (String level)
    {
	if (level.equals("FAC_CONCURRENCY_CONTROL"))
	    return FAC_CONCURRENCY_CONTROL;
	if (level.equals("FAC_LOCK_STORE"))
	    return FAC_LOCK_STORE;

	return FacilityCode.FAC_NONE;
    }

    /**
     * @return the string representation of the facility level. Note, this
     * string is intended only for debugging purposes, and cannot be fed
     * back into the debug system to obtain the facility level that it
     * represents.
     *
     * @since JTS 2.1.2.
     */

public String printString (long level)
    {
	if (level == FacilityCode.FAC_ALL)
	    return "FAC_ALL";
	
	if (level == FacilityCode.FAC_NONE)
	    return "FAC_NONE";
	
	String sLevel = null;
	
	if ((level & FAC_CONCURRENCY_CONTROL) != 0)
	    sLevel = ((sLevel == null) ? "FAC_CONCURRENCY_CONTROL" : " & FAC_CONCURRENCY_CONTROL");
	if ((level & FAC_LOCK_STORE) != 0)
	    sLevel = ((sLevel == null) ? "FAC_LOCK_STORE" : " & FAC_LOCK_STORE");

	return ((sLevel == null) ? "FAC_NONE" : sLevel);
    }
		
}

