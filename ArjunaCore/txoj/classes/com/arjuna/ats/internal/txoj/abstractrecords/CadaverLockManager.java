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
 * $Id: CadaverLockManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.txoj.abstractrecords;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.logging.txojLogger;

/*
 *
 * Lock concurrency controller
 *
 * Lock-base concurrency control management system
 * Instances of this class are created by CadaverLockRecord class
 * instances for the sole purpose of lock cleanup due to a locked
 * object going out of scope prior to action termination. 
 * Serialisability prevents locks being released as scope is exited
 * thus they must be cleaned up later.
 *
 */

class CadaverLockManager extends LockManager
{

    public CadaverLockManager(Uid objUid, String tName)
    {
        super(objUid);

        objectTypeName = new String(tName);

        if (txojLogger.logger.isDebugEnabled()) {
            txojLogger.logger.debug("CadaverLockManager::CadaverLockManager(" + objUid + ")");
        }
    }

    /*
     * Publically inherited functions
     */

    public boolean restore_state (InputObjectState os, int t)
    {
        return false;
    }

    public boolean save_state (OutputObjectState os, int t)
    {
        return false;
    }

    public String type ()
    {
        return objectTypeName;
    }

    private String objectTypeName;

}
