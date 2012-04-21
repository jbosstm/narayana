/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2012,
 * @author JBoss Inc.
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
        System.out.println("Chacking that " + defaultStoreDir + " was not created by the test");
        assertFalse(new File(defaultStoreDir).exists());
    }

    /**
     * Delete a directory hierarchy
     * @param directory the point at which to start deleting (directory is also removed)
     * @return
     */
    private boolean removeContents(File directory)
    {
        boolean emptied = true;

        if ((directory != null) &&
                directory.isDirectory() &&
                (!directory.getName().equals("")) &&
                (!directory.getName().equals("/")) &&
                (!directory.getName().equals("\\")) &&
                (!directory.getName().equals(".")) &&
                (!directory.getName().equals("src/test")))
        {
            File[] contents = directory.listFiles();

            for (File f : contents) {
                if (f.isDirectory()) {
                    removeContents(f);

                    if (emptied)
                        emptied = f.delete();
                } else {
                    if (emptied)
                        emptied = f.delete();
                }
            }
        }

        if (directory != null && emptied)
            emptied = directory.delete();

        return emptied;
    }
}
