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
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: TxOJNames.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.txoj;

import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.coordinator.RecordType;

/**
 * This class contains the ClassNames and ObjectName attributes that
 * may be used by implementations within this module.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TxOJNames.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 * @see com.arjuna.ats.arjuna.gandiva.ClassName
 * @see com.arjuna.ats.arjuna.gandiva.ObjectName
 */

public class TxOJNames
{

public static ClassName Implementation_LockStore_defaultStore ()
    {
	return TxOJNames.Implementation_LockStore_BasicLockStore();
    }
    
public static ClassName Implementation_LockStore_BasicLockStore ()
    {
	return new ClassName("BasicLockStore");
    }

public static ClassName Implementation_LockStore_BasicPersistentLockStore ()
    {
	return new ClassName("BasicPersistentLockStore");
    }

public static ClassName Implementation_Semaphore_BasicSemaphore ()
    {
	return new ClassName("BasicSemaphore");
    }

};
