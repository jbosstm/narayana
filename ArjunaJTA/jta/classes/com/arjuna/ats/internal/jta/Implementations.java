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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Implementations.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.internal.jta.resources.arjunacore.CommitMarkableResourceRecord;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;

class CommitMarkableResourceRecordMap implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return CommitMarkableResourceRecord.class;
    }
    
    public int getType ()
    {
        return RecordType.COMMITMARKABLERESOURCE;
    }
}

class XAResourceRecordMap implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return XAResourceRecord.class;
    }
    
    public int getType ()
    {
        return RecordType.JTA_RECORD;
    }
}

public class Implementations
{
    public static synchronized boolean added ()
    {
	return _added;
    }
    
    public static synchronized void initialise ()
    {
	if (!_added)
	{
	    /*
	     * Now add various abstract records which crash recovery needs.
	     */

	    RecordTypeManager.manager().add(new CommitMarkableResourceRecordMap());
	    RecordTypeManager.manager().add(new XAResourceRecordMap());
	    
	    _added = true;
	}
    }

    private Implementations ()
    {
    }

    private static boolean _added = false;

    static
    {
	initialise();
    }
    
}
