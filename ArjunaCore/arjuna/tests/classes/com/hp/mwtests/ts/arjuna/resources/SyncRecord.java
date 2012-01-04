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
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SyncRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.resources;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.SynchronizationRecord;

public class SyncRecord implements SynchronizationRecord
{
    public boolean beforeCompletion()
    {
        return true;
    }

    public boolean afterCompletion(int status)
    {
        _called = true;
        
        return true;
    }

    public boolean called ()
    {
        return _called;
    }
    
    public Uid get_uid()
    {
        return _theUid;
    }

    private Uid _theUid = new Uid();
    private boolean _called = false;

    public int compareTo(Object o)
    {
        SynchronizationRecord sr = (SynchronizationRecord) o;
        if (_theUid.equals(sr.get_uid())) {
            return 0;
        } else {
            return _theUid.lessThan(sr.get_uid()) ? -1 : 1;
        }
    }
}

