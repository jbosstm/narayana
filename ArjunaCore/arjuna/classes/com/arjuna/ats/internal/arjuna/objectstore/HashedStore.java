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

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

import java.io.*;
import java.io.File;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.NumberFormatException;

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
 *
 * @message com.arjuna.ats.internal.arjuna.objectstore.HashedStore_1 [com.arjuna.ats.internal.arjuna.objectstore.HashedStore_1] - HashedStore.create caught: {0}
 * @message com.arjuna.ats.internal.arjuna.objectstore.HashedStore_2 [com.arjuna.ats.internal.arjuna.objectstore.HashedStore_2] - invalid number of hash directories: {0}. Will use default.
 * @message com.arjuna.ats.internal.arjuna.objectstore.HashedStore_3 [com.arjuna.ats.internal.arjuna.objectstore.HashedStore_3] - invalid number of hash directories: {0}
 * @message com.arjuna.ats.internal.arjuna.objectstore.HashedStore_4 [com.arjuna.ats.internal.arjuna.objectstore.HashedStore_4] -  caught exception: {0}
 * @message com.arjuna.ats.internal.arjuna.objectstore.HashedStore_5 [com.arjuna.ats.internal.arjuna.objectstore.HashedStore_5] - HashedStore.allObjUids - could not pack Uid.
 * @message com.arjuna.ats.internal.arjuna.objectstore.HashedStore_6 [com.arjuna.ats.internal.arjuna.objectstore.HashedStore_6] - HashedStore.allObjUids - could not pack end of list Uid.
 */

public class HashedStore extends ShadowNoFileLockStore
{

    public int typeIs ()
    {
	return ObjectStoreType.HASHED;
    }

    public ClassName className ()
    {
	return ArjunaNames.Implementation_ObjectStore_HashedStore();
    }

    public static ClassName name ()
    {
	return ArjunaNames.Implementation_ObjectStore_HashedStore();
    }

    /*
     * Have to return as a ShadowingStore because of
     * inheritence.
     */

    public static ShadowingStore create ()
    {
	return new HashedStore("");
    }

    public static ShadowingStore create (Object[] param)
    {
	if (param == null)
	    return null;

	String location = (String) param[0];
	Integer shareStatus = (Integer) param[1];
	int ss = ObjectStore.OS_UNSHARED;

	if (shareStatus != null)
	{
	    try
	    {
		if (shareStatus.intValue() == ObjectStore.OS_SHARED)
		    ss = ObjectStore.OS_SHARED;
	    }
	    catch (Exception e)
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.HashedStore_1",
						new Object[]{e});
		}
	    }
	}

	return new HashedStore(location, ss);
    }

    public static ShadowingStore create (ObjectName param)
    {
	if (param == null)
	    return null;
	else
	    return new HashedStore(param);
    }

    /**
     * Given a type name initialise <code>state</code> to contains all of the
     * Uids of objects of that type
     */

    public boolean allObjUids (String tName, InputObjectState state, int match) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, 
				     "HashedStore.allObjUids("+tName+", "+state+", "+match+")");
	}

	/*
	 * Directory ALWAYS has a trailing '/'
	 */

	String directory = locateStore(getStoreName());
	OutputObjectState store = new OutputObjectState();

	/* Does typename start with a '/' if so skip over */

	if ((tName != null) && (tName.charAt(0) == File.separatorChar))
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

				if ((aUid.notEquals(Uid.nullUid())) && ((match == ObjectStore.OS_UNKNOWN) ||
				    (isType(aUid, tName, match))))
				{
				    aUid.pack(store);
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
				throw new ObjectStoreException(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.objectstore.HashedStore_5"));
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
	    Uid.nullUid().pack(store);
	}
	catch (IOException e)
	{
	    throw new ObjectStoreException(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.objectstore.HashedStore_6"));
	}

	state.setBuffer(store.buffer());

	store = null;

	return true;
    }

    /*
     * Protected constructors and destructor
     */

    protected HashedStore ()
    {
	this(ObjectStore.OS_UNSHARED);
    }

    protected HashedStore (int shareStatus)
    {
	super(shareStatus);

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE, "HashedStore.HashedStore( "+shareStatus+" )");
	}
    }

    protected HashedStore (String locationOfStore)
    {
	this(locationOfStore, ObjectStore.OS_UNSHARED);
    }

    protected HashedStore (String locationOfStore, int shareStatus)
    {
	super(shareStatus);

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE, "HashedStore.HashedStore("+locationOfStore+")");
	}
	
	try
	{
	    setupStore(locationOfStore);
	}
	catch (ObjectStoreException e)
	{
	    tsLogger.arjLogger.warn(e.getMessage());

	    throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString());
	}
    }

    protected HashedStore (ObjectName objName)
    {
	super(objName);
	
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE, "HashedStore.HashedStore("+objName+")");
	}
	
	try
	{
	    setupStore("");
	}
	catch (ObjectStoreException e)
	{
	    tsLogger.arjLogger.warn(e.getMessage());

	    throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString());
	}
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
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE, 
				     "HashedStore.genPathName("+objUid+", "+tName+", "+ObjectStore.stateTypeString(otype)+")");
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
	if (otype == ObjectStore.OS_SHADOW)
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
    
    private static final int DEFAULT_NUMBER_DIRECTORIES = 255;
    private static final String HASH_SEPARATOR = "#";
    
    private static int NUMBEROFDIRECTORIES = DEFAULT_NUMBER_DIRECTORIES;

    static
    {
	String numberOfDirs = arjPropertyManager.propertyManager.getProperty(com.arjuna.ats.arjuna.common.Environment.HASHED_DIRECTORIES);

	if (numberOfDirs != null)
	{
	    try
	    {
		Integer i = new Integer(numberOfDirs);
		
		NUMBEROFDIRECTORIES = i.intValue();

		if (NUMBEROFDIRECTORIES <= 0)
		{
		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		    {
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.HashedStore_2",
						    new Object[]{numberOfDirs});
		    }
		    
		    NUMBEROFDIRECTORIES = DEFAULT_NUMBER_DIRECTORIES;
		}
	    }
	    catch (NumberFormatException e)
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.HashedStore_3",
						new Object[]{numberOfDirs});
		}
		
		throw new com.arjuna.ats.arjuna.exceptions.FatalError("Invalid hash directory number: "+numberOfDirs);
	    }
	    catch (Exception e)
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.HashedStore_4",
						new Object[]{e});
		}
		
		throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString());
	    }
	}
    }

}

