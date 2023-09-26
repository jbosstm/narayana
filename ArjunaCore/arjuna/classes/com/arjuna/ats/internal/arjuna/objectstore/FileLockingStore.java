/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore;

import java.io.File;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.utils.FileLock;

/**
 * An refinement of the basic FileSystemStore which provides file-level locking.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: FileLockingStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public abstract class FileLockingStore extends FileSystemStore
{

    protected abstract InputObjectState read_state (Uid u, String tn, int s)
            throws ObjectStoreException;

    protected abstract boolean remove_state (Uid u, String tn, int s)
            throws ObjectStoreException;

    protected abstract boolean write_state (Uid u, String tn,
            OutputObjectState buff, int s) throws ObjectStoreException;

    public FileLockingStore(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException
    {
        super(objectStoreEnvironmentBean);
    }

    /**
     * Lock files as we would do on a Unix system.
     */

    protected synchronized boolean lock (File fd, int lmode, boolean create)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("FileLockingStore.lock(" + fd + ", " + FileLock.modeString(lmode) + ", " + create + ")");
        }

        FileLock fileLock = new FileLock(fd);

        return fileLock.lock(lmode, create);
    }

    protected synchronized boolean unlock (File fd)
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("FileLockingStore.unlock(" + fd + ")");
        }

        FileLock fileLock = new FileLock(fd);

        return fileLock.unlock();
    }

}