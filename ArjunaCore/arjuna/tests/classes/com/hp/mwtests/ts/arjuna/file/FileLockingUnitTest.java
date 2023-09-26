/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.file;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.arjuna.ats.arjuna.utils.FileLock;

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
        
        ofile.close();

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