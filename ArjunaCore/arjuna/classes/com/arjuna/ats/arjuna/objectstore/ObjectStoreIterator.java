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
 * $Id: ObjectStoreIterator.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.InputObjectState;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import java.io.IOException;

/**
 * Class that allows us to iterate through instances of objects
 * that may be stored within a specific object store.
 */

public class ObjectStoreIterator
{

public ObjectStoreIterator (ObjectStore store, String tName)
    {
	try
	{
	    store.allObjUids(tName, uidList);
	}
	catch (ObjectStoreException e)
	{
	}
    }
    
    /**
     * return the Uids from the list one at a time. ObjStore returns either
     * null list or a list terminated by the NIL_UID. Use the latter to return
     * 0 (for end of list)
     */

public final synchronized Uid iterate ()
    {
	Uid newUid = null;
	
	try
	{
	    newUid = new Uid(Uid.nullUid());

	    newUid.unpack(uidList);
	}
	catch (IOException e)
	{
	}

	return newUid;
    }

private InputObjectState uidList;
    
}
    
