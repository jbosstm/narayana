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
 * $Id: FileLockingStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.utils.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ObjectName;

import java.io.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import java.lang.NumberFormatException;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

/**
 * An refinement of the basic FileSystemStore which provides file-level
 * locking.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: FileLockingStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public abstract class FileLockingStore extends FileSystemStore
{

protected abstract InputObjectState read_state (Uid u, String tn, int s) throws ObjectStoreException;
protected abstract boolean remove_state (Uid u, String tn, int s) throws ObjectStoreException;
protected abstract boolean write_state (Uid u, String tn, OutputObjectState buff, int s) throws ObjectStoreException;
    
public FileLockingStore (String locationOfStore, int ss)
    {
	super(locationOfStore, ss);

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "FileLockingStore.FileLockingStore("+locationOfStore+")");
	}
    }

protected FileLockingStore (ObjectName objName)
    {
	super(objName);

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE, "FileLockingStore.FileLockingStore("+objName+")");
	}
    }

    /**
     * Lock files as we would do on a Unix system.
     */
    
protected synchronized boolean lock (File fd, int lmode, boolean create)
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE, 
				     "FileLockingStore.lock("+fd+", "+FileLock.modeString(lmode)+", "+create+")");
	}

	FileLock fileLock = new FileLock(fd);

	return fileLock.lock(lmode, create);
    }

protected synchronized boolean unlock (File fd)
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE, "FileLockingStore.unlock("+fd+")");
	}

	FileLock fileLock = new FileLock(fd);

	return fileLock.unlock();
    }
    
}
