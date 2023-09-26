/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.utils;

import java.io.File;
import java.io.IOException;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * Obtains a unique value to represent the process id via the filesystem.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: FileProcessId.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class FileProcessId implements com.arjuna.ats.arjuna.utils.Process
{

    /**
     * @return the process id. This had better be unique between processes
     * on the same machine. If not we're in trouble!
     *
     * @since JTS 2.1.
     */

public int getpid ()
    {
	if (FileProcessId.processId == 0)
	{
	    synchronized (FileProcessId.lock)
	    {
		/*
		 * All of this is just to ensure uniqueness!
		 */

		if (FileProcessId.processId == 0)	
		{
		    int retry = 1000;
		    int pid = (int) System.currentTimeMillis();

		    pid = Math.abs(pid);

		    /*
		     * Use the "var" directory location from the property file.
		     * If it is not set, create "./var/tmp".
		     */
		    String dir = arjPropertyManager.getCoreEnvironmentBean().getVarDir();
		    
		    if (dir == null || dir.length() == 0)
			dir = System.getProperty("user.dir") + File.separator + "var" + File.separator + "tmp";
		    else
			dir = dir + File.separator + "tmp";

		    File tmpDir = new File(dir);

		    if (tmpDir.isDirectory() == false && tmpDir.mkdirs() == false)
			throw new FatalError(tsLogger.i18NLogger.get_utils_FileProcessId_1());

		    for (int i = 0; i < retry; i++)
		    {
			try
			{
			    File f = new File(dir + File.separator+ "pid " + pid);

			    if (f.createNewFile())
			    {
				f.deleteOnExit();  // problem if we crash?

				processId = pid;
		    
				break;
			    }
			    else
				pid++;
			}
			catch (IOException e)
		        {
			}
		    }

		    if (processId == 0)
			throw new FatalError(tsLogger.i18NLogger.get_utils_FileProcessId_2());
		}
	    }
	}

	return processId;
    }

private static int processId = 0;
    
private static final String hexStart = "0x";

private static final Object lock = new Object();
    
}