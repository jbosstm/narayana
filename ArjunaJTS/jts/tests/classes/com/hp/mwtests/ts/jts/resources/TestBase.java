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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id$
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
/*        try {
        	myORB.orb().shutdown(true);
        } catch (BAD_INV_ORDER bio) {
        	// ignore - jacorb can tolerate the second call to shutdown, IDLJ will not
        }*/
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
