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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: FileLock.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.utils;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import java.io.*;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.InterruptedException;

import com.arjuna.common.util.logging.*;

/**
 * Sometimes it is necessary to lock a file at the disk level.
 *
 * Since there is no native Java way of locking a file, we
 * have to implement our own. Unfortunately, it appears as though
 * we can only assume that rename is atomic. We base the locking on
 * this then: rename the lock file and update it with the lock owners.
 *
 * How it works:
 *
 * for every file we want to lock we create an _lock file. This file
 * contains information about who is locking the file, and in what mode.
 * (Single writer, multiple readers.) To guarantee atomicity of update, we
 * move (rename) the actual file each time we want to lock it and update the
 * lock file. When this is done, we move (rename) it back. Almost like a
 * two-phase commit protocol! Currently we don't support re-entrant locking.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: FileLock.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 *
 * @message com.arjuna.ats.arjuna.utils.FileLock_1 [com.arjuna.ats.arjuna.utils.FileLock_1] - FileLock.lock called for {0}
 * @message com.arjuna.ats.arjuna.utils.FileLock_2 [com.arjuna.ats.arjuna.utils.FileLock_2] - FileLock.unlock called for {0}
 * @message com.arjuna.ats.arjuna.utils.FileLock_3 [com.arjuna.ats.arjuna.utils.FileLock_3] - FileLock.createFile called for {0}
 * @message com.arjuna.ats.arjuna.utils.FileLock_4 [com.arjuna.ats.arjuna.utils.FileLock_4] - An error occurred while creating file {0}
 * @message com.arjuna.ats.arjuna.utils.FileLock_5 [com.arjuna.ats.arjuna.utils.FileLock_5] - FileLock.lockFile called for {0}
 * @message com.arjuna.ats.arjuna.utils.FileLock_6 [com.arjuna.ats.arjuna.utils.FileLock_6] - FileLock.unlockFile called for {0}
 */

public class FileLock
{

public static final int F_RDLCK = 0;
public static final int F_WRLCK = 1;

public static final int defaultTimeout = 10;  // milliseconds
public static final int defaultRetry = 10;
     
public FileLock (File name)
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS,
				     VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_GENERAL, "FileLock ( "+name+" )");
	}
	
	_theFile = name;
	_lockFile = new File(name.toString() + "_lock");
	_lockFileLock = new File(name.toString() + "_lock.lock");
	_timeout = FileLock.defaultTimeout;
	_retry = FileLock.defaultRetry;
    }

public FileLock (File name, long timeout, long retry)
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS,
				     VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_GENERAL, "FileLock ( "+name+", "+timeout+", "+retry+" )");
	}
	
	_theFile = name;
	_lockFile = new File(name.toString() + "_lock");
	_lockFileLock = new File(name.toString() + "_lock.lock");
	_timeout = timeout;
	_retry = retry;
    }

public void finalize ()
    {
	_theFile = null;
    }
    
    /**
     * @since JTS 2.1.1.
     */

public boolean lock (int lmode)
    {
	return lock(lmode, false);
    }
    
public boolean lock (int lmode, boolean create)
    {
	if (tsLogger.arjLoggerI18N.debugAllowed())
	{
	    tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					 FacilityCode.FAC_GENERAL, "com.arjuna.ats.arjuna.utils.FileLock_1", 
					 new Object[]{_lockFile});
	}

	if (create && !_theFile.exists())
	{
	    createFile();
	}
	
	/*
	 * If the lock file exists, and the mode is exclusive, then we can
	 * immediately return false.
	 *
	 * Currently we do not implement re-entrant locking, which requires
	 * some owner id.
	 */

	if (_lockFile.exists() && (lmode == FileLock.F_WRLCK))
	    return false;

	int number = 0;

	if (lockFile())  // have we moved the file (if it exists)?
	{
	    try
	    {
		DataInputStream ifile = new DataInputStream(new FileInputStream(_lockFile));
		int value = ifile.readInt();
	    
		/*
		 * Already exclusively locked.
		 */
	    
		if (value == FileLock.F_WRLCK)
		{
		    ifile.close();
		    unlockFile();
		    
		    return false;
		}
		else
		    number = ifile.readInt();

		ifile.close();
	    }
	    catch (FileNotFoundException e)
	    {
	    }
	    catch (IOException e)
	    {
		/*
		 * Something went wrong. Abandon.
		 */

		unlockFile();

		return false;
	    }

	    try
	    {
		DataOutputStream ofile = new DataOutputStream(new FileOutputStream(_lockFile));

		number++;
	
		ofile.writeInt(lmode);
		ofile.writeInt(number);

		ofile.close();

		unlockFile();
	
		return true;
	    }
	    catch (IOException e)
	    {
		/*
		 * Something went wrong. Abandon.
		 * Lock file is ok since we haven't touched it.
		 */

		unlockFile();

		return false;
	    }
	}

	return false;
    }

public boolean unlock ()
    {
	if (tsLogger.arjLoggerI18N.debugAllowed())
	{
	    tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					 FacilityCode.FAC_GENERAL, "com.arjuna.ats.arjuna.utils.FileLock_2", 
					 new Object[]{_lockFile});
	}
	
	if (!_lockFile.exists())
	    return false;

	if (lockFile())
	{
	    int number = 0, mode = 0;
	    
	    try
	    {
		DataInputStream ifile = new DataInputStream(new FileInputStream(_lockFile));

		mode = ifile.readInt();
		number = ifile.readInt();
		ifile.close();

		number--;

		if (number == 0)
		{
		    _lockFile.delete();

		    unlockFile();

		    return true;
		}
	    }
	    catch (FileNotFoundException e)
	    {
		unlockFile();

		return false;
	    }
	    catch (IOException e)
	    {
		unlockFile();

		return false;
	    }
	    
	    try
	    {
		DataOutputStream ofile = new DataOutputStream(new FileOutputStream(_lockFile));

		ofile.writeInt(mode);
		ofile.writeInt(number);
		ofile.close();
		
		unlockFile();
		
		return true;
	    }
	    catch (IOException e)
	    {
		unlockFile();
		
		return false;
	    }
	}

	return false;
    }

public static String modeString (int mode)
    {
	switch (mode)
	{
	case FileLock.F_RDLCK:
	    return "FileLock.F_RDLCK";
	case FileLock.F_WRLCK:
	    return "FileLock.F_WRLCK";
	default:
	    return "Unknown";
	}
    }

private final boolean createFile ()
    {
	if (tsLogger.arjLoggerI18N.debugAllowed())
	{
	    tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					 FacilityCode.FAC_GENERAL, "com.arjuna.ats.arjuna.utils.FileLock_3", 
					 new Object[]{_lockFile});
	}

	byte b[] = new byte[1];

	try
	{
	    if (!_theFile.exists())
	    {
		_theFile.createNewFile();

		return true;
	    }
	    else
		return false;
	}
	catch (IOException e)
	{
	    
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.utils.FileLock_4",  new Object[]{_lockFile});
	    }

	    return false;
	}	
    }
    
private final boolean lockFile ()
    {
	if (tsLogger.arjLoggerI18N.debugAllowed())
	{
	    tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					 FacilityCode.FAC_GENERAL, "com.arjuna.ats.arjuna.utils.FileLock_5", 
					 new Object[]{_lockFile});
	}

	for (int i = 0; i < _retry; i++)
	{
	    try
	    {
		if (_lockFileLock.createNewFile())
		    return true;
		else
		{
		    try
		    {
			Thread.sleep(_timeout);
		    }
		    catch (InterruptedException e)
		    {
		    }
		}
	    }
	    catch (IOException ex)
	    {
		// already created, so locked!
	    }
	}

	return false;
    }

private final boolean unlockFile ()
    {
	if (tsLogger.arjLoggerI18N.debugAllowed())
	{
	    tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					 FacilityCode.FAC_GENERAL, "com.arjuna.ats.arjuna.utils.FileLock_6", 
					 new Object[]{_lockFile});
	}
	return _lockFileLock.delete();
    }
    
    private File _theFile;
    private File _lockFile;
    private File _lockFileLock;
    private long _timeout;
    private long _retry;
    
}
