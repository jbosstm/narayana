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
package com.arjuna.ats.arjuna.logging;

/**
 * The various levels of facility codes that are available.
 * Typically one for each major component in the system.
 *
 * <ul>
 * <li>FAC_ATOMIC_ACTION = 0x0000001 (atomic action core module).
 * <li>FAC_BUFFER_MAN = 0x00000004 (state management (buffer) classes).
 * <li>FAC_ABSTRACT_REC = 0x00000008 (abstract records).
 * <li>FAC_OBJECT_STORE = 0x00000010 (object store implementations).
 * <li>FAC_STATE_MAN = 0x00000020 (state management and StateManager).
 * <li>FAC_SHMEM = 0x00000040 (shared memory implementation classes).
 * <li>FAC_GENERAL = 0x00000080 (general classes).
 * <li>FAC_CRASH_RECOVERY = 0x00000800 (detailed trace of crash recovery module and classes).
 * <li>FAC_THREADING = 0x00002000 (threading classes).
 * <li>FAC_JDBC = 0x00008000 (JDBC 1.0 and 2.0 support).
 * <li>FAC_RECOVERY_NORMAL = 0x00040000 (normal output for crash recovery manager).
 * </ul>
 *
 */
 
public class FacilityCode extends com.arjuna.common.util.logging.FacilityCode
{

public static final long FAC_ATOMIC_ACTION = 0x00000001;
public static final long FAC_BUFFER_MAN = 0x00000004;
public static final long FAC_ABSTRACT_REC = 0x0000008;
public static final long FAC_OBJECT_STORE = 0x00000010;
public static final long FAC_STATE_MAN = 0x00000020;
public static final long FAC_SHMEM = 0x00000040;
public static final long FAC_GENERAL = 0x00000080;
public static final long FAC_CRASH_RECOVERY = 0x00000800;
public static final long FAC_THREADING = 0x00002000;
public static final long FAC_JDBC = 0x00008000;
public static final long FAC_RECOVERY_NORMAL = 0x00040000;
public static final long FAC_RESERVED1 = 0x00100000;
public static final long FAC_RESERVED2 = 0x00200000;
public static final long FAC_RESERVED3 = 0x00400000;
public static final long FAC_RESERVED4 = 0x00800000;
public static final long FAC_USER1 = 0x10000000;
public static final long FAC_USER2 = 0x20000000;
public static final long FAC_USER3 = 0x40000000;
public static final long FAC_USER4 = 0x80000000;

public long getLevel (String level)
    {
	if (level.equals("FAC_ATOMIC_ACTION"))
	    return FAC_ATOMIC_ACTION;
	if (level.equals("FAC_BUFFER_MAN"))
	    return FAC_BUFFER_MAN;
	if (level.equals("FAC_ABSTRACT_REC"))
	    return FAC_ABSTRACT_REC;
	if (level.equals("FAC_OBJECT_STORE"))
	    return FAC_OBJECT_STORE;
	if (level.equals("FAC_STATE_MAN"))
	    return FAC_STATE_MAN;
	if (level.equals("FAC_SHMEM"))
	    return FAC_SHMEM;
	if (level.equals("FAC_GENERAL"))
	    return FAC_GENERAL;
	if (level.equals("FAC_CRASH_RECOVERY"))
	    return FAC_CRASH_RECOVERY;
	if (level.equals("FAC_THREADING"))
	    return FAC_THREADING;
	if (level.equals("FAC_JDBC"))
	    return FAC_JDBC;
	if (level.equals("FAC_RECOVERY_NORMAL"))
	    return FAC_RECOVERY_NORMAL;
	if (level.equals("FAC_RESERVED1"))
	    return FAC_RESERVED1;
	if (level.equals("FAC_RESERVED2"))
	    return FAC_RESERVED2;
	if (level.equals("FAC_RESERVED3"))
	    return FAC_RESERVED3;
	if (level.equals("FAC_RESERVED4"))
	    return FAC_RESERVED4;
	if (level.equals("FAC_USER1"))
	    return FAC_USER1;
	if (level.equals("FAC_USER2"))
	    return FAC_USER2;
	if (level.equals("FAC_USER3"))
	    return FAC_USER3;
	if (level.equals("FAC_USER4"))
	    return FAC_USER4;
	if (level.equals("FAC_ALL"))
	    return FacilityCode.FAC_ALL;
	if (level.equals("FAC_USER1"))
	    return FAC_USER1;

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
	
	if ((level & FAC_ATOMIC_ACTION) != 0)
	    sLevel = ((sLevel == null) ? "FAC_ATOMIC_ACTION" : " & FAC_ATOMIC_ACTION");
	if ((level & FAC_BUFFER_MAN) != 0)
	    sLevel = ((sLevel == null) ? "FAC_BUFFER_MAN" : " & FAC_BUFFER_MAN");
	if ((level & FAC_ABSTRACT_REC) != 0)
	    sLevel = ((sLevel == null) ? "FAC_ABSTRACT_REC" : " & FAC_ABSTRACT_REC");
	if ((level & FAC_OBJECT_STORE) != 0)
	    sLevel = ((sLevel == null) ? "FAC_OBJECT_STORE" : " & FAC_OBJECT_STORE");
	if ((level & FAC_STATE_MAN) != 0)
	    sLevel = ((sLevel == null) ? "FAC_STATE_MAN" : " & FAC_STATE_MAN");
	if ((level & FAC_SHMEM) != 0)
	    sLevel = ((sLevel == null) ? "FAC_SHMEM" : " & FAC_SHMEM");
	if ((level & FAC_GENERAL) != 0)
	    sLevel = ((sLevel == null) ? "FAC_GENERAL" : " & FAC_GENERAL");
	if ((level & FAC_CRASH_RECOVERY) != 0)
	    sLevel = ((sLevel == null) ? "FAC_CRASH_RECOVERY" : " & FAC_CRASH_RECOVERY");
	if ((level & FAC_THREADING) != 0)
	    sLevel = ((sLevel == null) ? "FAC_THREADING" : " & FAC_THREADING");
	if ((level & FAC_JDBC) != 0)
	    sLevel = ((sLevel == null) ? "FAC_JDBC" : " & FAC_JDBC");

	if ((level & FAC_RECOVERY_NORMAL) != 0)
	    sLevel = ((sLevel == null) ? "FAC_RECOVERY_NORMAL" : " & FAC_RECOVERY_NORMAL");
	if ((level & FAC_RESERVED1) != 0)
	    sLevel = ((sLevel == null) ? "FAC_RESERVED1" : " & FAC_RESERVED1");
	if ((level & FAC_RESERVED2) != 0)
	    sLevel = ((sLevel == null) ? "FAC_RESERVED2" : " & FAC_RESERVED2");
	if ((level & FAC_RESERVED3) != 0)
	    sLevel = ((sLevel == null) ? "FAC_RESERVED3" : " & FAC_RESERVED3");
	if ((level & FAC_RESERVED4) != 0)
	    sLevel = ((sLevel == null) ? "FAC_RESERVED4" : " & FAC_RESERVED4");
	if ((level & FAC_USER1) != 0)
	    sLevel = ((sLevel == null) ? "FAC_USER1" : " & FAC_USER1");
	if ((level & FAC_USER2) != 0)
	    sLevel = ((sLevel == null) ? "FAC_USER2" : " & FAC_USER2");
	if ((level & FAC_USER3) != 0)
	    sLevel = ((sLevel == null) ? "FAC_USER3" : " & FAC_USER3");
	if ((level & FAC_USER4) != 0)
	    sLevel = ((sLevel == null) ? "FAC_USER4" : " & FAC_USER4");

	return ((sLevel == null) ? "FAC_NONE" : sLevel);
    }
		
}

