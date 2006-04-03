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
package com.hp.mwtests.ts.arjuna.file;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: FileLocking.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.utils.*;
import com.arjuna.mwlabs.testframework.unittest.Test;

import java.io.*;

import java.io.IOException;
import java.lang.InterruptedException;

/*
 * A simple test of file locking. Create 2 threads and have them
 * contend to exclusively lock the same file.
 */

class FileContender extends Thread
{

public FileContender (File file, int id, int lmode)
    {
	_theFile = file;
	_id = id;
	_mode = lmode;
    }

public void run ()
    {
	FileLock fileLock = new FileLock(_theFile);

	for (int i = 0; i < 100; i++)
	{
	    if (fileLock.lock(_mode, false))
	    {
		System.out.println("thread "+_id+" locked file.");

		Thread.yield();

		fileLock.unlock();

		System.out.println("thread "+_id+" unlocked file.");

		Thread.yield();
	    }
	    else
	    {
		System.out.println("thread "+_id+" could not lock file.");

		Thread.yield();
	    }
	}
    }

private File _theFile;
private int _id;
private int _mode;

};

public class FileLocking extends Test
{

public void run(String[] args)
    {
	int lmode = FileLock.F_WRLCK;

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-read") == 0)
		lmode = FileLock.F_RDLCK;
	}

	/*
	 * Create the file to contend over.
	 */

	File theFile = new File("foobar");

	try
	{
	    DataOutputStream ofile = new DataOutputStream(new FileOutputStream(theFile));

	    ofile.writeInt(0);
	}
	catch (IOException e)
	{
	    logInformation("An error occurred while creating file.");
            e.printStackTrace(System.err);
	    assertFailure();
	}

	/*
	 * Now create the threads.
	 */

	FileContender thread1 = new FileContender(theFile, 1, lmode);
	FileContender thread2 = new FileContender(theFile, 2, lmode);

	thread1.start();
	thread2.start();

	try
	{
	    thread1.join();
	    thread2.join();

	    logInformation("Test completed successfully.");

            assertSuccess();
	}
	catch (InterruptedException e)
	{
	    logInformation("Test did not complete successfully.");
            e.printStackTrace(System.err);
            assertFailure();
	}

	/*
	 * Now tidy up.
	 */

	theFile.delete();
    }

    public static void main(String[] args)
    {
        FileLocking fileTest = new FileLocking();

    	fileTest.initialise(null, null, args, new com.arjuna.mwlabs.testframework.unittest.LocalHarness());

    	fileTest.run(args);
    }
}
