/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.resources;

import java.io.File;

import org.junit.After;
import org.junit.Before;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

/**
 * handy utility functions for unit tests.
 */
public class TestBase
{
    @Before
    public void setUp()
	{
        emptyObjectStore();
	}

	@After
    public void tearDown()
	{
        emptyObjectStore();
    }

    private void emptyObjectStore()
    {
        String objectStoreDirName = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();

        System.out.println("Emptying " + objectStoreDirName);

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
}