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

import com.arjuna.ats.arjuna.utils.*;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;

import java.io.IOException;
import java.lang.InterruptedException;

/*
 * A simple test of file locking. Create 2 threads and have them
 * contend to exclusively lock the same file.
 */

class FileContender extends Thread
{

    public FileContender(File file, int id, int lmode)
    {
        _theFile = file;
        _id = id;
        _mode = lmode;
    }

    public void run()
    {
        FileLock fileLock = new FileLock(_theFile);

        for (int i = 0; i < 100; i++) {
            if (fileLock.lock(_mode, false)) {
                System.out.println("thread " + _id + " locked file.");

                Thread.yield();

                fileLock.unlock();

                System.out.println("thread " + _id + " unlocked file.");

                Thread.yield();
            } else {
                System.out.println("thread " + _id + " could not lock file.");

                Thread.yield();
            }
        }
    }

    private File _theFile;
    private int _id;
    private int _mode;

}

public class FileLockingUnitTest
{
    @Test
    public void test() throws IOException
    {
        int lmode = FileLock.F_WRLCK;

        /*
       * Create the file to contend over.
       */

        File theFile = new File("foobar");
        theFile.deleteOnExit();

        DataOutputStream ofile = new DataOutputStream(new FileOutputStream(theFile));

        ofile.writeInt(0);

        /*
       * Now create the threads.
       */

        FileContender thread1 = new FileContender(theFile, 1, lmode);
        FileContender thread2 = new FileContender(theFile, 2, lmode);

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        }
        catch (InterruptedException e) {
            fail("Test did not complete successfully.");
        }
    }

    @Test
    public void testMultipleLock () throws Exception
    {
        FileLock fl = new FileLock(System.getProperty("java.io.tmpdir")+"/barfoo");
        
        assertTrue(fl.lock(FileLock.F_RDLCK, true));
        assertTrue(fl.lock(FileLock.F_RDLCK));
        
        assertTrue(fl.unlock());
        assertTrue(fl.unlock());
        
        assertEquals(FileLock.modeString(FileLock.F_RDLCK), "FileLock.F_RDLCK");
        assertEquals(FileLock.modeString(FileLock.F_WRLCK), "FileLock.F_WRLCK");
        assertEquals(FileLock.modeString(-1), "Unknown");
    }
    
}
