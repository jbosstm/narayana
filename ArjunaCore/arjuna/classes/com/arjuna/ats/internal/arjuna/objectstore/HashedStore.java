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
 * $Id: HashedStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import java.io.File;
import java.io.IOException;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

/*
 * Should be derived from FragmentedStore, but we currently
 * don't have such an implementation in Java.
 */

/**
 * The basic shadowing store implementations store the object states in
 * a separate file within the same directory in the object store, determined
 * by the object's type. However, as the number of file entries within the
 * directory increases, so does the search time for finding a specific file.
 * The HashStore implementation hashes object states over many different
 * sub-directories to attempt to keep the number of files in a given
 * directory low, thus improving performance as the number of object states
 * grows.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: HashedStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class HashedStore extends ShadowNoFileLockStore
{
    /**
     * Given a type name initialise <code>state</code> to contains all of the
     * Uids of objects of that type
     */

    public boolean allObjUids (String tName, InputObjectState state, int match) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("HashedStore.allObjUids(" + tName + ", " + state + ", " + match + ")");
        }

        /*
         * Directory ALWAYS has a trailing '/'
         */

        String directory = locateStore(getStoreName());
        OutputObjectState store = new OutputObjectState();

        /* Does typename start with a '/' if so skip over */

        if ((tName != null) && (tName.length() > 0) && (tName.charAt(0) == File.separatorChar))
            directory = directory + tName.substring(1, tName.length());
        else
            directory = directory + tName;

        if (!directory.endsWith(File.separator))
            directory = directory + File.separator;

        File f = new File(directory);
        String[] entry = f.list();

        if ((entry != null) && (entry.length > 0))
        {
            for (int i = 0; i < entry.length; i++)
            {
                if ( Character.isDigit(entry[i].charAt(1)) || entry[i].startsWith(HASH_SEPARATOR) )
                {
                    File dir = new File(directory + entry[i]);

                    if (dir.isDirectory())
                    {
                        String[] dirEnt = dir.list();

                        for (int j = 0; j < dirEnt.length; j++)
                        {
                            try
                            {
                                Uid aUid = new Uid(dirEnt[j], true);

                                if (!aUid.valid() || (aUid.equals(Uid.nullUid())))
                                {
                                    String revealed = revealedId(dirEnt[j]);

                                    // don't want to give the same id twice.

                                    if (present(revealed, dirEnt))
                                        aUid = null;
                                    else
                                        aUid = new Uid(revealed);
                                }

                                if ((aUid.notEquals(Uid.nullUid())) && ((match == StateStatus.OS_UNKNOWN) ||
                                    (isType(aUid, tName, match))))
                                {
                                    if(scanZeroLengthFiles || new File(dir, dirEnt[j]).length() > 0) {
                                        UidHelper.packInto(aUid, store);
                                    }
                                }
                            }
                            catch (NumberFormatException e)
                            {
                                /*
                                 * Not a number at start of file.
                                 */
                            }
                            catch (IOException e)
                            {
                                throw new ObjectStoreException(tsLogger.i18NLogger.get_objectstore_HashedStore_5(), e);
                            }
                        }
                    }
                }
                else
                {
                    // ignore
                }
            }
        }

        /* terminate list */

        try
        {
            UidHelper.packInto(Uid.nullUid(), store);
        }
        catch (IOException e)
        {
            throw new ObjectStoreException(tsLogger.i18NLogger.get_objectstore_HashedStore_6(), e);
        }

        state.setBuffer(store.buffer());

        store = null;

        return true;
    }

    public HashedStore (ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException
    {
        super(objectStoreEnvironmentBean);
    }

    protected String truncate (String value)
    {
        int lastIndex = value.lastIndexOf(HashedStore.HASH_SEPARATOR);
        String toReturn = value;

        if (lastIndex != -1)
        {
            int nextIndex = value.lastIndexOf(HashedStore.HASH_SEPARATOR, lastIndex - 1);

            if (nextIndex != -1)
            {
                char[] bitInbetween = new char[lastIndex - nextIndex - 1];
                boolean isDigit = true;

                value.getChars(nextIndex + 1, lastIndex, bitInbetween, 0);

                for (int i = 0; (i < bitInbetween.length) && isDigit; i++)
                {
                    if (!Character.isDigit(bitInbetween[i]))
                    {
                        isDigit = false;
                    }
                }

                if (isDigit)
                    toReturn = value.substring(lastIndex + 1);
            }
        }

        return toReturn;
    }

    /**
     * @return the file name for the state of the object
     * identified by the Uid and TypeName.
     */

    protected String genPathName (Uid objUid, String tName, int otype) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("HashedStore.genPathName(" + objUid + ", " + tName + ", " + StateType.stateTypeString(otype) + ")");
        }

        String storeName = locateStore(getStoreName());
        String fname = null;
        String cPtr = null;
        int uidHash = objUid.hashCode();
        String os = objUid.fileStringForm();
        String hashDir = HashedStore.HASH_SEPARATOR + uidHash % HashedStore.NUMBEROFDIRECTORIES + HashedStore.HASH_SEPARATOR + File.separator;  // make sure hash value is unique in the string

        if ((tName == null) || (tName.length() == 0))
            cPtr = "";
        else
        {
            cPtr = tName;

            /*
             * Convert Unix separators to 'other', i.e., Windows!
             */

            if (FileSystemStore.rewriteSeparator && (cPtr.indexOf(FileSystemStore.unixSeparator) != -1))
            {
                cPtr = cPtr.replace(FileSystemStore.unixSeparator, File.separatorChar);
            }
        }

        /*
         * storeName always ends in '/' so we can remove any
         * at the start of the type name.
         */

        if (cPtr.charAt(0) == File.separatorChar)
            cPtr = cPtr.substring(1, cPtr.length());

        if (cPtr.charAt(cPtr.length() -1) != File.separatorChar)
            fname = storeName + cPtr + File.separator + hashDir + os;
        else
            fname = storeName + cPtr + hashDir + os;

        /*
         * Make sure we don't end in a '/'.
         */

        if (fname.charAt(fname.length() -1) == File.separatorChar)
            fname = fname.substring(0, fname.length() -2);

        // mark the shadow copy distinctly
        if (otype == StateType.OS_SHADOW)
            fname = fname + SHADOWCHAR;

        return fname;
    }

    public static final char SHADOWCHAR = '!';

    private final boolean present (String id, String[] list)
    {
        for (int i = 0; i < list.length; i++)
        {
            if (list[i].equals(id))
                return true;
        }

        return false;
    }

    public static final int DEFAULT_NUMBER_DIRECTORIES = 255;
    private static final String HASH_SEPARATOR = "#";

    private static final int NUMBEROFDIRECTORIES = arjPropertyManager.getObjectStoreEnvironmentBean().getHashedDirectories();

}

