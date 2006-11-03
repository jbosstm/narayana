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
package com.arjuna.ats.tsmx.logging;

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

public class tsmxFacilityCode extends com.arjuna.common.util.logging.FacilityCode
{
	public static final long FAC_TS_MX= 0x00000001;

	public long getLevel(String level)
	{
		if (level.equals("FAC_TS_MX"))
			return FAC_TS_MX;

		return com.arjuna.common.util.logging.FacilityCode.FAC_NONE;
	}

	/**
	 * @return the string representation of the facility level. Note, this
	 * string is intended only for debugging purposes, and cannot be fed
	 * back into the debug system to obtain the facility level that it
	 * represents.
	 *
	 * @since JTS 2.1.2.
	 */

	public String printString(long level)
	{
		if (level == com.arjuna.common.util.logging.FacilityCode.FAC_ALL)
			return "FAC_ALL";

		if (level == com.arjuna.common.util.logging.FacilityCode.FAC_NONE)
			return "FAC_NONE";

		String sLevel = null;

		if ((level & FAC_TS_MX) != 0)
			sLevel = ((sLevel == null) ? "FAC_TS_MX" : " & FAC_TS_MX");
		return ((sLevel == null) ? "FAC_NONE" : sLevel);
	}

}

