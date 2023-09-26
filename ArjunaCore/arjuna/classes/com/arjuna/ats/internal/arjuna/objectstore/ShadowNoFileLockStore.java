/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore;

import java.io.File;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/**
 * Almost the same as the ShadowingStore implementation, but assumes all
 * concurrency control is provided by the object. Therefore, there is no need to
 * set/release locks on the file representation in the object store. Saves time.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ShadowNoFileLockStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class ShadowNoFileLockStore extends ShadowingStore
{
    public ShadowNoFileLockStore(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException
    {
        super(objectStoreEnvironmentBean);
    }

    /**
     * Override the default lock/unlock implementations to do nothing.
     */

    protected boolean lock (File fd, int lmode, boolean create)
    {
        return true;
    }

    protected boolean unlock (File fd)
    {
        return true;
    }

}