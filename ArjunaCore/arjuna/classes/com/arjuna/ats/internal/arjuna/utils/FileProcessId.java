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
 * Copyright (C) 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: FileProcessId.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.utils;

import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.common.util.propertyservice.PropertyManager;

import com.arjuna.ats.arjuna.utils.Process;
import com.arjuna.ats.arjuna.utils.Utility;
import java.io.*;
import java.net.InetAddress;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import java.net.UnknownHostException;
import java.lang.NumberFormatException;
import java.lang.StringIndexOutOfBoundsException;
import java.io.IOException;
import java.io.FileNotFoundException;

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
     *
     * @message com.arjuna.ats.internal.arjuna.utils.FileProcessId_1 [com.arjuna.ats.internal.arjuna.utils.FileProcessId_1] - FileProcessId.getpid - could not locate temporary directory.
     * @message com.arjuna.ats.internal.arjuna.utils.FileProcessId_2 [com.arjuna.ats.internal.arjuna.utils.FileProcessId_2] - FileProcessId.getpid could not create unique file.
     */

public int getpid ()
    {
	if (FileProcessId.processId == 0)
	{
	    synchronized (FileProcessId.hexStart)
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
		    String dir = arjPropertyManager.propertyManager.getProperty(Environment.VAR_DIR);
		    
		    if (dir == null || dir.length() == 0)
			dir = System.getProperty("user.dir") + File.separator + "var" + File.separator + "tmp";
		    else
			dir = dir + File.separator + "tmp";

		    File tmpDir = new File(dir);

		    if (tmpDir.isDirectory() == false && tmpDir.mkdirs() == false)
			throw new FatalError(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.utils.FilePocessId_1"));

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
			throw new FatalError(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.utils.FilePocessId_2"));
		}
	    }
	}

	return processId;
    }

private static int processId = 0;
    
private static final String hexStart = "0x";
    
}
