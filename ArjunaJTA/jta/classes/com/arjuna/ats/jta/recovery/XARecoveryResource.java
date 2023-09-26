/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.recovery;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;

public interface XARecoveryResource
{

    /**
     * Results of performing recovery.
     * 
     * Remember to update com.arjuna.ats.jta.utils.XARecoveryResourceHelper
     */

    public static final int RECOVERED_OK = 1;
    public static final int FAILED_TO_RECOVER = 2;
    public static final int WAITING_FOR_RECOVERY = 3;
    public static final int TRANSACTION_NOT_PREPARED = 4;

    /**
     * Responses to whether or not the instance is recoverable.
     * 
     * Remember to update com.arjuna.ats.jta.utils.XARecoveryResourceHelper
     */

    public static final int INCOMPLETE_STATE = 10;
    public static final int INFLIGHT_TRANSACTION = 11;
    public static final int RECOVERY_REQUIRED = 12;
    
    /**
     * If we don't have an XAResource then we cannot recover at
     * this stage. The XAResource will have to be provided for
     * us and then we can retry.
     *
     * Because recovery happens periodically, it is possible that it takes
     * a snapshot of a transaction that is still running and will vanish
     * from the log anyway. If that happens, then we don't need to (and
     * can't) run recovery on it.
     */

    public int recoverable ();

    /**
     * Attempt the recovery. Return one of the status values above.
     */

    public int recover ();

    /**
     * @return the Xid that was used to manipulate this state.
     */

    public Xid getXid ();

    /**
     * @return the Uid for this instance.
     */

    public Uid get_uid ();

    /**
     * @return the type for this instance.
     */

    public String type ();

}