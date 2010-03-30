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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ShadowingStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.utils.FileLock;
import java.io.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.SyncFailedException;

/**
 * A shadowing file store implementation. Each version of the object's state is
 * maintained in a separate file. So, the original is stored in one file, and
 * the shadow (the updated state) is stored in another. When the transaction
 * commits, the shadow is made the original. If the transaction rolls back then
 * the shadow is simply removed from the object store.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ShadowingStore.java 2342 2006-03-30 13:06:17Z $
 * @since 1.0
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_1
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_1] -
 *          ShadowingStore.commit_state - store invalid!
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_2
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_2] -
 *          ShadowStore::commit_state - failed to rename {0} to {1}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_3
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_3] -
 *          ShadowStore::hide_state - failed to rename {0} to {1}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_4
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_4] -
 *          ShadowStore::reveal_state - failed to rename {0} to {1}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_5
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_5] -
 *          ShadowingStore.create caught: {0}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_6
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_6] -
 *          ShadowingStore.read_state - store invalid!
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_7
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_7] -
 *          ShadowingStore::read_state() failed
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_8
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_8] -
 *          ShadowingStore::read_state - unlock or close of {0} failed
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_9
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_9] -
 *          ShadowingStore::remove_state() - access problems on {0} and {1}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_10
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_10] -
 *          ShadowingStore::remove_state() - state {0} does not exist for type
 *          {1}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_11
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_11] -
 *          ShadowingStore::remove_state() - unlink failed on {0}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_12
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_12] -
 *          ShadowingStore.remove_state() - fd error for {0}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_13
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_13] -
 *          ShadowingStore::remove_state() attempted removal of
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_14
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_14] -
 *          UNKNOWN state for object with uid {0} , type {1}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_15
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_15] -
 *          HIDDEN state for object with uid {0} , type {1}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_16
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_16] -
 *          state for object with uid {0} , type {1}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_17
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_17] -
 *          ShadowingStore.remove_state - type() operation of object with uid
 *          {0} returns NULL
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_18
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_18] -
 *          ShadowingStore::write_state() - openAndLock failed for {0}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_19
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_19] -
 *          ShadowingStore::write_state - unlock or close of {0} failed.
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_20
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_20] -
 *          ShadowingStore.renameFromTo - from {0} not present. Possibly renamed
 *          by crash recovery.
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_21
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_21] -
 *          ShadowingStore.renameFromTo - failed to lock: {0}
 * @message com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_22
 *          [com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_22] -
 *          ShadowingStore.currentState({0}, {1}) - returning {2}
 * @message com.arjuna.ats.internal.arjuna.objectstore.notypenameuid No typename
 *          for object:
 */

public class ShadowingStore extends FileSystemStore
{

    public int typeIs ()
    {
        return ObjectStoreType.SHADOWING;
    }

    /**
     * @return current state of object. Assumes that genPathName allocates
     *         enough extra space to allow extra chars to be added. State search
     *         is ordered OS_SHADOW, OS_UNCOMMITTED_HIDDEN, OS_ORIGINAL,
     *         OS_COMMITTED_HIDDEN.
     */

    public int currentState (Uid objUid, String tName)
            throws ObjectStoreException
    {
        int theState = StateStatus.OS_UNKNOWN;

        if (storeValid())
        {
            String path = genPathName(objUid, tName, StateType.OS_SHADOW);

            if (exists(path))
            {
                theState = StateStatus.OS_UNCOMMITTED;
            }
            else
            {
                path = path + HIDDINGCHAR;

                if (exists(path))
                {
                    theState = StateStatus.OS_UNCOMMITTED_HIDDEN;
                }
                else
                {
                    path = genPathName(objUid, tName, StateType.OS_ORIGINAL);

                    if (exists(path))
                    {
                        theState = StateStatus.OS_COMMITTED;
                    }
                    else
                    {
                        path = path + HIDDINGCHAR;

                        if (exists(path))
                        {
                            theState = StateStatus.OS_COMMITTED_HIDDEN;
                        }
                    }
                }
            }
        }    

        if (tsLogger.arjLoggerI18N.isDebugEnabled()) {
            tsLogger.arjLoggerI18N.debug("com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_22", new Object[]
                    {objUid, tName,
                            StateStatus.stateStatusString(theState)});
        }

        return theState;
    }

    /**
     * Commit a previous write_state operation which was made with the SHADOW
     * StateType argument. This is achieved by renaming the shadow and removing
     * the hidden version.
     */

    public boolean commit_state (Uid objUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ShadowingStore.commit_state(" + objUid + ", " + tName + ")");
        }

        boolean result = false;

        /* Bail out if the object store is not set up */

        if (!storeValid())
        {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N
                        .warn("com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_1");

            return false;
        }

        if (tName != null)
        {
            String shadow = null;
            String filename = null;
            int state = currentState(objUid, tName);

            if ((state == StateStatus.OS_UNCOMMITTED_HIDDEN)
                    || (state == StateStatus.OS_UNCOMMITTED))
            {
                shadow = genPathName(objUid, tName, StateType.OS_SHADOW);
                filename = genPathName(objUid, tName, StateType.OS_ORIGINAL);

                if (state == StateStatus.OS_UNCOMMITTED_HIDDEN)
                {
                    /* maintain hidden status on rename */

                    shadow = shadow + HIDDINGCHAR;
                    filename = filename + HIDDINGCHAR;
                }

                File shadowState = new File(shadow);
                File originalState = new File(filename);

                /*
                 * We need to do this because rename will not overwrite an
                 * existing file in Windows, as it will in Unix. It is safe to
                 * do so since we have written the shadow.
                 */

                result = renameFromTo(shadowState, originalState);

                if (!result)
                {
                    if (tsLogger.arjLoggerI18N.isWarnEnabled())
                    {
                        tsLogger.arjLoggerI18N
                                .warn(
                                        "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_2",
                                        new Object[]
                                        { shadow, filename });
                    }
                }
                else
                {
                    super.addToCache(filename);
                    super.removeFromCache(shadow);
                }

                shadowState = null;
                originalState = null;
            }
            else
                result = true;
        }
        else
            throw new ObjectStoreException(
                    "ShadowStore::commit_state - "
                            + tsLogger.arjLoggerI18N
                                    .getString("com.arjuna.ats.internal.arjuna.objectstore.notypenameuid")
                            + objUid);

        return result;
    }

    /**
     * Hide/reveal an object regardless of state. Hidden objects cannot be read
     * but they can be written (Crash recovery needs this).
     */

    public boolean hide_state (Uid objUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ShadowingStore.hide_state(" + objUid + ", " + tName + ")");
        }

        boolean hiddenOk = true;

        /* Bail out if the object store is not set up */

        if (storeValid())
        {
            int state = currentState(objUid, tName);
            String path1 = null;
            String path2 = null;

            switch (state)
            {
            case StateStatus.OS_UNCOMMITTED_HIDDEN:
            case StateStatus.OS_COMMITTED_HIDDEN:
                break;
            case StateStatus.OS_COMMITTED:
            {
                path1 = genPathName(objUid, tName, StateType.OS_ORIGINAL);
                path2 = new String(path1) + HIDDINGCHAR;

                File newState = new File(path1);
                File oldState = new File(path2);

                if (renameFromTo(newState, oldState))
                {
                    super.removeFromCache(path1);
                    super.addToCache(path2);
                }
                else
                {
                    if (tsLogger.arjLoggerI18N.isWarnEnabled())
                    {
                        tsLogger.arjLoggerI18N
                                .warn(
                                        "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_3",
                                        new Object[]
                                        { newState, oldState });
                    }
                }

                newState = null;
                oldState = null;

                break;
            }
            case StateStatus.OS_UNCOMMITTED:
            {
                path1 = genPathName(objUid, tName, StateType.OS_SHADOW);
                path2 = new String(path1) + HIDDINGCHAR;

                File newState = new File(path1);
                File oldState = new File(path2);

                if (renameFromTo(newState, oldState))
                {
                    super.removeFromCache(path1);
                    super.addToCache(path2);
                }
                else
                {
                    if (tsLogger.arjLoggerI18N.isWarnEnabled())
                    {
                        tsLogger.arjLoggerI18N
                                .warn(
                                        "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_3",
                                        new Object[]
                                        { newState, oldState });
                    }
                }

                newState = null;
                oldState = null;

                break;
            }
            default:
                hiddenOk = false;
            }
        }
        else
            hiddenOk = false;

        return hiddenOk;
    }

    public boolean reveal_state (Uid objUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ShadowingStore.reveal_state(" + objUid + ", " + tName + ")");
        }

        boolean revealedOk = true;

        if (storeValid())
        {
            int state = currentState(objUid, tName);
            String path1 = null;
            String path2 = null;

            switch (state)
            {
            case StateStatus.OS_UNCOMMITTED_HIDDEN:
            {
                path1 = genPathName(objUid, tName, StateType.OS_SHADOW);
                path2 = new String(path1) + HIDDINGCHAR;

                File newState = new File(path2);
                File oldState = new File(path1);

                if (renameFromTo(newState, oldState))
                {
                    super.removeFromCache(path2);
                    super.addToCache(path1);
                }
                else
                {
                    if (tsLogger.arjLoggerI18N.isWarnEnabled())
                    {
                        tsLogger.arjLoggerI18N
                                .warn(
                                        "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_4",
                                        new Object[]
                                        { newState, oldState });
                    }
                }

                newState = null;
                oldState = null;

                break;
            }
            case StateStatus.OS_COMMITTED_HIDDEN:
            {
                path1 = genPathName(objUid, tName, StateType.OS_ORIGINAL);
                path2 = new String(path1) + HIDDINGCHAR;

                File newState = new File(path2);
                File oldState = new File(path1);

                if (renameFromTo(newState, oldState))
                {
                    super.removeFromCache(path2);
                    super.addToCache(path1);
                }
                else
                {
                    if (tsLogger.arjLoggerI18N.isWarnEnabled())
                    {
                        tsLogger.arjLoggerI18N
                                .warn(
                                        "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_4",
                                        new Object[]
                                        { newState, oldState });
                    }
                }

                newState = null;
                oldState = null;

                break;
            }
            case StateStatus.OS_COMMITTED:
            case StateStatus.OS_UNCOMMITTED:
                break;
            default:
                revealedOk = false;
            }
        }
        else
            revealedOk = false;

        return revealedOk;
    }

    /**
     * @return the file name for the state of the object identified by the Uid
     *         and TypeName. If the StateType argument is OS_SHADOW then the Uid
     *         part of the name includes # characters. Builds on lower level
     *         genPathName which allocates enough slop to accomodate the extra
     *         chars.
     */

    protected String genPathName (Uid objUid, String tName, int ft)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ShadowingStore.genPathName(" + objUid + ", " + tName + ", " + StateType.stateTypeString(ft) + ")");
        }

        String fname = super.genPathName(objUid, tName, ft);

        if (ft == StateType.OS_SHADOW)
            fname = fname + SHADOWCHAR;

        return fname;
    }

    protected String revealedId (String name)
    {
        int index = name.indexOf(HIDDINGCHAR);

        if (index == -1)
            index = name.indexOf(SHADOWCHAR);

        if (index != -1)
            return name.substring(0, index);
        else
            return name;
    }

    protected InputObjectState read_state (Uid objUid, String tName, int ft)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ShadowingStore.read_state(" + objUid + ", " + tName + ", " + StateType.stateTypeString(ft) + ")");
        }

        if (!storeValid())
        {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N
                        .warn("com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_6");

            return null;
        }

        InputObjectState new_image = null;

        if (tName != null)
        {
            int state = currentState(objUid, tName);

            if ((state == StateStatus.OS_COMMITTED)
                    || (state == StateStatus.OS_UNCOMMITTED))
            {
                /*
                 * Is the current state the same as that requested?
                 */

                if (((state == StateStatus.OS_COMMITTED) && (ft != StateType.OS_ORIGINAL))
                        || ((state == StateStatus.OS_UNCOMMITTED) && (ft != StateType.OS_SHADOW)))
                {
                    return null;
                }

                String fname = genPathName(objUid, tName, ft);
                File fd = openAndLock(fname, FileLock.F_RDLCK, false);

                if (fd != null)
                {
                    int imageSize = (int) fd.length();
                    byte[] buffer = new byte[imageSize];
                    FileInputStream ifile = null;

                    try
                    {
                        ifile = new FileInputStream(fd);
                    }
                    catch (FileNotFoundException e)
                    {
                        closeAndUnlock(fd, ifile, null);

                        throw new ObjectStoreException(
                                "ShadowingStore::read_state error: " + e, e);
                    }

                    /* now try to read the actual image out of the store */

                    try
                    {
                        if ((buffer != null)
                                && (ifile.read(buffer, 0, imageSize) == imageSize))
                        {
                            new_image = new InputObjectState(objUid, tName,
                                    buffer);
                        }
                        else
                        {
                            tsLogger.arjLoggerI18N
                                    .warn("com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_7");
                        }
                    }
                    catch (IOException e)
                    {
                        closeAndUnlock(fd, ifile, null);

                        throw new ObjectStoreException(
                                "ShadowingStore::read_state failed: " + e, e);
                    }

                    if (!closeAndUnlock(fd, ifile, null))
                    {
                        if (tsLogger.arjLoggerI18N.isWarnEnabled())
                        {
                            tsLogger.arjLoggerI18N
                                    .warn(
                                            "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_8",
                                            new Object[]
                                            { fname });
                        }
                    }
                }
            }
        }
        else
            throw new ObjectStoreException(
                    "ShadowStore::read_state - "
                            + tsLogger.arjLoggerI18N
                                    .getString("com.arjuna.ats.internal.arjuna.objectstore.notypenameuid")
                            + objUid);

        return new_image;
    }

    protected boolean remove_state (Uid objUid, String name, int ft)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ShadowingStore.remove_state(" + objUid + ", " + name + ", " + StateType.stateTypeString(ft) + ")");
        }

        boolean removeOk = true;

        if (!storeValid())
            return false;

        if (name != null)
        {
            int state = currentState(objUid, name);

            if ((state == StateStatus.OS_COMMITTED)
                    || (state == StateStatus.OS_UNCOMMITTED))
            {
                String fname = genPathName(objUid, name, ft);
                File fd = openAndLock(fname, FileLock.F_WRLCK, false);

                if (fd != null)
                {
                    if (!fd.canWrite())
                    {
                        removeOk = false;

                        if (ft == StateType.OS_ORIGINAL)
                        {
                            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                            {
                                tsLogger.arjLoggerI18N
                                        .warn(
                                                "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_9",
                                                new Object[]
                                                { objUid, name });
                            }

                            if (!fd.exists())
                            {
                                if (tsLogger.arjLoggerI18N.isWarnEnabled())
                                {
                                    tsLogger.arjLoggerI18N
                                            .warn(
                                                    "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_10",
                                                    new Object[]
                                                    { objUid, name });
                                }
                            }
                        }
                    }
                    else
                    {
                        if (!fd.delete())
                        {
                            removeOk = false;

                            if (ft == StateType.OS_ORIGINAL)
                            {
                                if (tsLogger.arjLoggerI18N.isWarnEnabled())
                                {
                                    tsLogger.arjLoggerI18N
                                            .warn(
                                                    "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_11",
                                                    new Object[]
                                                    { fname });
                                }
                            }
                        }
                    }

                    closeAndUnlock(fd, null, null);
                }
                else
                {
                    if (tsLogger.arjLoggerI18N.isWarnEnabled())
                    {
                        tsLogger.arjLoggerI18N
                                .warn(
                                        "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_12",
                                        new Object[]
                                        { objUid });
                    }

                    removeOk = false;
                }

                if (removeOk)
                    super.removeFromCache(fname);
            }
            else
            {
                removeOk = false;

                if (state == StateStatus.OS_UNKNOWN)
                    tsLogger.arjLoggerI18N
                            .info(
                                    "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_14",
                                    new Object[]
                                    { objUid, name });
                else
                    tsLogger.arjLoggerI18N
                            .info(
                                    "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_15",
                                    new Object[]
                                    { objUid, name });
            }
        }
        else
        {
            removeOk = false;

            if (tsLogger.arjLoggerI18N.isWarnEnabled())
            {
                tsLogger.arjLoggerI18N
                        .warn(
                                "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_17",
                                new Object[]
                                { objUid });
            }
        }

        return removeOk;
    }

    /**
     * write_state saves the ObjectState in a file named by the type and Uid of
     * the ObjectState. If the second argument is SHADOW, then the file name is
     * different so that a subsequent commit_state invocation will rename the
     * file.
     */

    protected boolean write_state (Uid objUid, String tName,
            OutputObjectState state, int ft) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ShadowingStore.write_state(" + objUid + ", " + tName + ", " + StateType.stateTypeString(ft) + ")");
        }

        if (!storeValid())
            return false;

        if (tName != null)
        {
            String fname = genPathName(objUid, tName, ft);
            File fd = openAndLock(fname, FileLock.F_WRLCK, true);
            int imageSize = (int) state.length();

            if (fd == null)
            {
                if (tsLogger.arjLoggerI18N.isWarnEnabled())
                {
                    tsLogger.arjLoggerI18N
                            .warn(
                                    "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_18",
                                    new Object[]
                                    { fname });
                }

                return false;
            }

            FileOutputStream ofile = null;

            if (imageSize > 0)
            {
                try
                {
                    ofile = new FileOutputStream(fd);

                    ofile.write(state.buffer(), 0, imageSize);

                    if (synchronousWrites())
                    {
                        // must flush any in-memory buffering prior to sync

                        ofile.flush();

                        FileDescriptor fileDesc = ofile.getFD(); // assume it's
                                                                 // valid!
                        fileDesc.sync();
                    }
                }
                catch (SyncFailedException e)
                {
                    closeAndUnlock(fd, null, ofile);

                    throw new ObjectStoreException(
                            "ShadowingStore::write_state() - write failed to sync for "
                                    + fname, e);
                }
                catch (FileNotFoundException e)
                {
                    closeAndUnlock(fd, null, ofile);

                    e.printStackTrace();

                    throw new ObjectStoreException(
                            "ShadowingStore::write_state() - write failed to locate file "
                                    + fname + ": " + e, e);
                }
                catch (IOException e)
                {
                    closeAndUnlock(fd, null, ofile);

                    e.printStackTrace();

                    throw new ObjectStoreException(
                            "ShadowingStore::write_state() - write failed for "
                                    + fname + ": " + e, e);
                }
            }

            if (!closeAndUnlock(fd, null, ofile))
            {
                if (tsLogger.arjLoggerI18N.isWarnEnabled())
                {
                    tsLogger.arjLoggerI18N
                            .warn(
                                    "com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_19",
                                    new Object[]
                                    { fname });
                }
            }

            super.addToCache(fname);

            return true;
        }
        else
            throw new ObjectStoreException(
                    "ShadowStore::write_state - "
                            + tsLogger.arjLoggerI18N
                                    .getString("com.arjuna.ats.internal.arjuna.objectstore.notypenameuid")
                            + objUid);
    }

    public ShadowingStore(String locationOfStore)
    {
        this(locationOfStore, StateType.OS_SHARED);
    }

    public ShadowingStore(String locationOfStore, int shareStatus)
    {
        super(shareStatus);

        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ShadowingStore.ShadowingStore(" + locationOfStore + ")");
        }

        try
        {
            setupStore(locationOfStore);
        }
        catch (ObjectStoreException e)
        {
            tsLogger.arjLogger.fatal(e);

            throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(),
                    e);
        }
    }

    public ShadowingStore()
    {
        this(StateType.OS_SHARED);
    }

    public ShadowingStore(int shareStatus)
    {
        super(shareStatus);

        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ShadowingStore.ShadowingStore( " + shareStatus + " )");
        }

        try
        {
            setupStore(arjPropertyManager.getObjectStoreEnvironmentBean()
                    .getLocalOSRoot());
        }
        catch (ObjectStoreException e)
        {
            tsLogger.arjLogger.fatal(e);

            throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(),
                    e);
        }
    }

    public static final char HIDDINGCHAR = '#';

    public static final char SHADOWCHAR = '!';

}
