/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.ats.txoj.common;

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.ats.txoj.TxOJNames;

import java.io.File;

/**
 * TODO javadoc
 * TODO test case
 */
@PropertyPrefix(prefix = "com.arjuna.ats.txoj.lockstore.")
public class TxojEnvironmentBean
{
    @FullPropertyName(name = "com.arjuna.ats.txoj.common.propertiesFile")
    private String propertiesFile = "";

    private String lockStoreDir = System.getProperty("user.dir") + File.separator + "LockStore";
    private String lockStoreType;
    private String multipleLockStore = null;
    private String singleLockStore = TxOJNames.Implementation_LockStore_defaultStore().stringForm();
    private boolean allowNestedLocking = true;

//    public static final String PROPERTIES_FILE = "com.arjuna.ats.txoj.common.propertiesFile";
    public String getPropertiesFile()
    {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

//    public static final String LOCKSTORE_DIR = "com.arjuna.ats.txoj.lockstore.lockStoreDir";
    public String getLockStoreDir()
    {
        return lockStoreDir;
    }

    public void setLockStoreDir(String lockStoreDir)
    {
        this.lockStoreDir = lockStoreDir;
    }

//    public static final String LOCKSTORE_TYPE = "com.arjuna.ats.txoj.lockstore.lockStoreType";
    public String getLockStoreType()
    {
        return lockStoreType;
    }

    public void setLockStoreType(String lockStoreType)
    {
        this.lockStoreType = lockStoreType;
    }

//    public static final String MULTIPLE_LOCKSTORE = "com.arjuna.ats.txoj.lockstore.multipleLockStore";
    public String getMultipleLockStore()
    {
        return multipleLockStore;
    }

    public void setMultipleLockStore(String multipleLockStore)
    {
        this.multipleLockStore = multipleLockStore;
    }

//    public static final String SINGLE_LOCKSTORE = "com.arjuna.ats.txoj.lockstore.singleLockStore";
    public String getSingleLockStore()
    {
        return singleLockStore;
    }

    public void setSingleLockStore(String singleLockStore)
    {
        this.singleLockStore = singleLockStore;
    }

//    public static final String ALLOW_NESTED_LOCKING = "com.arjuna.ats.txoj.lockstore.allowNestedLocking";
    public boolean isAllowNestedLocking()
    {
        return allowNestedLocking;
    }

    public void setAllowNestedLocking(boolean allowNestedLocking)
    {
        this.allowNestedLocking = allowNestedLocking;
    }



}
