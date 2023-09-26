/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.resources;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import org.junit.After;
import org.junit.Before;
import org.omg.CORBA.BAD_INV_ORDER;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

import java.io.File;

public class TestBase
{  
	public void beforeSetupClass() {} 
	
    @Before
    public void setUp () throws Exception
    {
    	beforeSetupClass();
    	emptyObjectStore();

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
    }
    
    @After
    public void tearDown () throws Exception
    {
        myOA.destroy();
        myORB.shutdown(true);
		// JBTM-1829 as the orbportability version of shutdown does not wait for
		// completion some jacorb tests will fail due to possible race conditions
        try {
        	myORB.orb().shutdown(true);
        } catch (BAD_INV_ORDER bio) {
        	// ignore - IDLJ will not tolerate the second call to shutdown
        }
        emptyObjectStore();
    }

    private void emptyObjectStore()
    {
        String objectStoreDirName = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();

        System.out.printf("Emptying %s\n", objectStoreDirName);

        File objectStoreDir = new File(objectStoreDirName);

        removeContents(objectStoreDir);
    }

    public void removeContents(File directory)
    {
        if ((directory != null) &&
                directory.isDirectory() &&
                (!directory.getName().equals("")) &&
                (!directory.getName().equals("/")) &&
                (!directory.getName().equals("\\")) &&
                (!directory.getName().equals(".")) &&
                (!directory.getName().equals("..")))
        {
            File[] contents = directory.listFiles();

            for (int index = 0; index < contents.length; index++)
            {
                if (contents[index].isDirectory())
                {
                    removeContents(contents[index]);
                    contents[index].delete();
                }
                else
                {
                    contents[index].delete();
                }
            }
        }
    }

    private ORB myORB = null;
    private RootOA myOA = null;
}