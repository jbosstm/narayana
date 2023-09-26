/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

/**
 * The basic action store implementations store the object states in a separate
 * file within the same directory in the object store, determined by the
 * object's type. However, as the number of file entries within the directory
 * increases, so does the search time for finding a specific file. The HashStore
 * implementation hashes object states over many different sub-directories to
 * attempt to keep the number of files in a given directory low, thus improving
 * performance as the number of object states grows. Currently the hash number
 * is set for both user hashed stores and action hashed stores.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: HashedActionStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 2.1.
 */

public class HashedActionStore extends HashedStore
{
    public HashedActionStore(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException
    {
        super(objectStoreEnvironmentBean);

        // overrides parents use of isObjectStoreSync
        doSync = objectStoreEnvironmentBean.isTransactionSync();
    }
}