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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SyncFailedException;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.utils.FileLock;

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
 */

public class ShadowingStore extends FileSystemStore
{
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

        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ShadowingStore.currentState("+objUid+", "+tName+") - returning "+
                    StateStatus.stateStatusString(theState));
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
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ShadowingStore.commit_state(" + objUid + ", " + tName + ")");
        }

        boolean result = false;

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

                if (!result) {
                    tsLogger.i18NLogger.warn_objectstore_ShadowingStore_2(shadow, filename);
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
                            + tsLogger.i18NLogger.get_objectstore_notypenameuid()
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
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ShadowingStore.hide_state(" + objUid + ", " + tName + ")");
        }

        boolean hiddenOk = true;

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
                else {
                    tsLogger.i18NLogger.warn_objectstore_ShadowingStore_3(newState.getName(), oldState.getName());
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
                else {
                    tsLogger.i18NLogger.warn_objectstore_ShadowingStore_3(newState.getName(), oldState.getName());
                }

                newState = null;
                oldState = null;

                break;
            }
            default:
                hiddenOk = false;
        }

        return hiddenOk;
    }

    public boolean reveal_state (Uid objUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ShadowingStore.reveal_state(" + objUid + ", " + tName + ")");
        }

        boolean revealedOk = true;

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
                else {
                    tsLogger.i18NLogger.warn_objectstore_ShadowingStore_4(newState.getName(), oldState.getName());
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
                else {
                    tsLogger.i18NLogger.warn_objectstore_ShadowingStore_4(newState.getName(), oldState.getName());
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
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ShadowingStore.genPathName(" + objUid + ", " + tName + ", " + StateType.stateTypeString(ft) + ")");
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
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ShadowingStore.read_state(" + objUid + ", " + tName + ", " + StateType.stateTypeString(ft) + ")");
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

                        tsLogger.logger.info("ObjectStore record was deleted during restoration, users should not deleted records manually: " + fd.getAbsolutePath(), e);
                        return null;
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
                        else {
                            tsLogger.i18NLogger.warn_objectstore_ShadowingStore_7();
                        }
                    }
                    catch (IOException e)
                    {
                        closeAndUnlock(fd, ifile, null);

                        throw new ObjectStoreException(
                                "ShadowingStore::read_state failed: " + e, e);
                    }

                    if (!closeAndUnlock(fd, ifile, null)) {
                        tsLogger.i18NLogger.warn_objectstore_ShadowingStore_8(fname);
                    }
                }
            }
        }
        else
            throw new ObjectStoreException(
                    "ShadowStore::read_state - "
                            + tsLogger.i18NLogger.get_objectstore_notypenameuid()
                            + objUid);

        return new_image;
    }

    protected boolean remove_state (Uid objUid, String name, int ft)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ShadowingStore.remove_state(" + objUid + ", " + name + ", " + StateType.stateTypeString(ft) + ")");
        }

        boolean removeOk = true;

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

                        if (ft == StateType.OS_ORIGINAL) {
                            tsLogger.i18NLogger.warn_objectstore_ShadowingStore_9(objUid, name);

                            if (!fd.exists()) {
                                tsLogger.i18NLogger.warn_objectstore_ShadowingStore_10(objUid, name);
                            }
                        }
                    }
                    else
                    {
                        if (!fd.delete())
                        {
                            removeOk = false;

                            if (ft == StateType.OS_ORIGINAL) {
                                tsLogger.i18NLogger.warn_objectstore_ShadowingStore_11(fname);
                            }
                        }
                    }

                    closeAndUnlock(fd, null, null);
                }
                else {
                    tsLogger.i18NLogger.warn_objectstore_ShadowingStore_12(objUid);

                    removeOk = false;
                }

                if (removeOk)
                    super.removeFromCache(fname);
            }
            else
            {
                removeOk = false;

                if (state == StateStatus.OS_UNKNOWN)
                    tsLogger.i18NLogger.info_objectstore_ShadowingStore_14(objUid, name);
                else
                    tsLogger.i18NLogger.info_objectstore_ShadowingStore_15(objUid, name);
            }
        }
        else {
            removeOk = false;

            tsLogger.i18NLogger.warn_objectstore_ShadowingStore_17(objUid);
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
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ShadowingStore.write_state(" + objUid + ", " + tName + ", " + StateType.stateTypeString(ft) + ")");
        }

        if (tName != null)
        {
            String fname = genPathName(objUid, tName, ft);
            File fd = openAndLock(fname, FileLock.F_WRLCK, true);
            int imageSize = (int) state.length();

            if (fd == null) {
                tsLogger.i18NLogger.warn_objectstore_ShadowingStore_18(fname);

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

            if (!closeAndUnlock(fd, null, ofile)) {
                tsLogger.i18NLogger.warn_objectstore_ShadowingStore_19(fname);
            }

            super.addToCache(fname);

            return true;
        }
        else
            throw new ObjectStoreException(
                    "ShadowStore::write_state - "
                            + tsLogger.i18NLogger.get_objectstore_notypenameuid()
                            + objUid);
    }

    public ShadowingStore(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException
    {
        super(objectStoreEnvironmentBean);
    }

    public static final char HIDDINGCHAR = '#';

    public static final char SHADOWCHAR = '!';
}
