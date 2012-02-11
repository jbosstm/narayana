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
 * Copyright (C) 2004, 2005,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: NullActionStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * A null implementation. Useful for performance tuning.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: NullActionStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class NullActionStore extends ShadowNoFileLockStore
{
    /**
     * @return current state of object. Assumes that genPathName allocates
     *         enough extra space to allow extra chars to be added. Action
     *         stores only store committed objects
     */

    public int currentState (Uid objUid, String tName)
            throws ObjectStoreException
    {
        return StateStatus.OS_UNKNOWN;
    }

    /**
     * Commit a previous write_state operation which was made with the SHADOW
     * StateType argument. This is achieved by renaming the shadow and removing
     * the hidden version.
     */

    public boolean commit_state (Uid objUid, String tName)
            throws ObjectStoreException
    {
        return true;
    }

    public boolean hide_state (Uid u, String tn) throws ObjectStoreException
    {
        return false;
    }

    public boolean reveal_state (Uid u, String tn) throws ObjectStoreException
    {
        return false;
    }

    public InputObjectState read_committed (Uid storeUid, String tName)
            throws ObjectStoreException
    {
        return null;
    }

    public InputObjectState read_uncommitted (Uid u, String tn)
            throws ObjectStoreException
    {
        return null;
    }

    public boolean remove_committed (Uid storeUid, String tName)
            throws ObjectStoreException
    {
        return true;
    }

    public boolean remove_uncommitted (Uid u, String tn)
            throws ObjectStoreException
    {
        return false;
    }

    public boolean write_committed (Uid storeUid, String tName,
            OutputObjectState state) throws ObjectStoreException
    {
        return true;
    }

    public boolean write_uncommitted (Uid u, String tn, OutputObjectState s)
            throws ObjectStoreException
    {
        return false;
    }
    
    public NullActionStore(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException
    {
        super(objectStoreEnvironmentBean);
    }
}
