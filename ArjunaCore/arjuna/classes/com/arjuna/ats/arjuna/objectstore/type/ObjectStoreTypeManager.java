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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: RecordType.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.objectstore.type;

import java.util.ArrayList;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;

/**
 * This allows users to define a mapping between record type integers
 * and specific Class-es. This replaces Gandiva from previous releases.
 */

public class ObjectStoreTypeManager
{
    public synchronized Class<? extends ObjectStore> getObjectStoreClass (int type)
    {
        /*
         * Stop at first hit.
         */
        
        for (int i = 0; i < _map.size(); i++)
        {
            if (_map.get(i).getType() == type)
                return _map.get(i).getObjectStoreClass();
        }
        
        return null;
    }
    
    public synchronized int getType (Class<? extends ObjectStore> c)
    {
        for (int i = 0; i < _map.size(); i++)
        {
            if (_map.get(i).getObjectStoreClass().equals(c))
                return _map.get(i).getType();
        }
        
        return -1;
    }
    
    public synchronized void add (ObjectStoreTypeMap map)
    {
        _map.add(map);
    }
    
    public static ObjectStoreTypeManager manager ()
    {
        return _instance;
    }
    
    private ObjectStoreTypeManager ()
    {
        _map = new ArrayList<ObjectStoreTypeMap>();
    }
    
    private final ArrayList<ObjectStoreTypeMap> _map;
    
    private static final ObjectStoreTypeManager _instance = new ObjectStoreTypeManager();
    
    static
    {
        /*
         * TODO
         * At present all record types that we need are known at compile time or can be
         * added programmatically. We may want to allow them to be specified dynamically,
         * e.g., on the command line or in a configuration file, but when that requirement
         * happens we can fill in this block ...
         */
    }
}
