/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
package com.arjuna.ats.tools.objectstorebrowser.stateviewers.viewers.atomicaction;

import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AtomicActionWrapper.java 2342 2006-03-30 13:06:17Z  $
 */

public class AtomicActionWrapper extends AtomicAction
{
    public AtomicActionWrapper(Uid objUid)
    {
        super(objUid);
    }

    public RecordList getFailedList()
    {
        return failedList;
    }

    public RecordList getHeuristicList()
    {
        return heuristicList;
    }

    public RecordList getPendingList()
    {
        return pendingList;
    }

    public RecordList getPreparedList()
    {
        return preparedList;
    }

    public RecordList getReadOnlyList()
    {
        return readonlyList;
    }
}
