/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.state.OutputObjectState;

import java.io.File;
import java.io.IOException;

/**
 * Test the filesystem is not changed when using a non file based store
 */
public class AltStoreTest
{
    String defaultStoreDir = null;

    @Before
    public void setPropertiesFileAndRemoveStore() {
        // set up a different properties file specific to this test
        // (the properties change the store type to one that does not touch the filesystem)
        System.setProperty("com.arjuna.ats.arjuna.common.propertiesFile", "alt-jbossts-properties.xml");

        defaultStoreDir = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
        assertFalse(defaultStoreDir == null);

        // delete the existing file based object store if it exists
        removeContents(new File(defaultStoreDir));

        System.out.println("Asserting that " + defaultStoreDir + " does not exist before the test");
        assertFalse(new File(defaultStoreDir).exists());
    }

    @Test
    public void test() throws IOException, ObjectStoreException
    {
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";

        // add a record to the object store
        assertTrue(StoreManager.getRecoveryStore().write_committed(new Uid(), tn, buff));

        // validate that there was no change to the filesystem
        System.out.println("Checking that " + defaultStoreDir + " was not created by the test");
        assertFalse(new File(defaultStoreDir).exists());
    }

    /**
     * Delete a directory hierarchy
     * @param directory the point at which to start deleting (directory is also removed)
     * @return
     */
    private boolean removeContents(File directory)
    {
        if (directory.isDirectory())
            for (String entry : directory.list())
                if (!removeContents(new File(directory, entry)))
                    return false;

        return directory.delete();
    }
}