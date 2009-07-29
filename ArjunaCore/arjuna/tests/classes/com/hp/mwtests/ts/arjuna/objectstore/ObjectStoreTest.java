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
package com.hp.mwtests.ts.arjuna.objectstore;

/*
 * Copyright (C) 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ObjectStoreTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import com.arjuna.ats.arjuna.gandiva.ClassName;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

public class ObjectStoreTest
{
    @Test
    public void test() throws IOException
    {
        ClassName imple = ArjunaNames.Implementation_ObjectStore_defaultStore();
        String localOSRoot = "foo";
        String objectStoreDir = "/bar";
        String shareStatus = "OS_SHARED";

        ObjectName objName = new ObjectName("JNS:myname");

        objName.setClassNameAttribute(Environment.OBJECTSTORE_TYPE, imple);
        arjPropertyManager.getCoordinatorEnvironmentBean().setTransactionLog(true);
        objName.setStringAttribute(Environment.LOCALOSROOT, localOSRoot);
        objName.setStringAttribute(Environment.OBJECTSTORE_DIR, objectStoreDir);
        objName.setStringAttribute(Environment.OBJECTSTORE_SHARE, shareStatus);

        ObjectStore objStore = new ObjectStore(objName);

        assertTrue(validate(objStore));


        // check with a known invalid implementation
        objStore = new ObjectStore();
        assertFalse(validate(objStore));
    }

    private static final boolean validate(ObjectStore objStore)
    {
        boolean passed = false;

        if (objStore.className().equals(imple)) {
            if (objStore.shareState() == ObjectStore.OS_SHARED) {
                if (objStore.storeDir().equals(objectStoreDir)) {
                    if (objStore.storeRoot().equals(localOSRoot))
                        passed = true;
                    else
                        System.err.println("ObjectStore root wrong: " + objStore.storeRoot());
                } else
                    System.err.println("ObjectStore dir wrong: " + objStore.storeDir());
            } else
                System.err.println("Share state wrong: " + objStore.shareState());
        } else
            System.err.println("Implementation wrong: " + objStore.className());

        return passed;
    }

    private static ClassName imple = ArjunaNames.Implementation_ObjectStore_defaultStore();
    private static String localOSRoot = "foo";
    private static String objectStoreDir = "/bar";
    private static String shareStatus = "OS_SHARED";

}
