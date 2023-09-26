/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.txoj.abstractrecords;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
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

        if (txojLogger.logger.isTraceEnabled()) {
            txojLogger.logger.trace("CadaverLockManager::CadaverLockManager(" + objUid + ")");
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