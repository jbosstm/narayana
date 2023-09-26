/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.recovery;

import javax.transaction.xa.Xid;

/**
 * Interface used by the XARecoveryModule to allow plugins to vote in the handling of in-doubt Xids.
 *
 *  @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-03
 */
public interface XAResourceOrphanFilter
{
    public enum Vote { ABSTAIN, ROLLBACK, LEAVE_ALONE }

    /**
     * Called by the XARecoveryModule for each in-doubt Xid.
     * Implementations should return
     *   Vote.ROLLBACK if they recognize the xid and believe it should be aborted.
     *   Vote.LEAVE_ALONE if they recognize the xid and do not want the XARecovery module to roll it back.
     *   Vote.ABSTAIN if they do not recognize the xid.
     * Each registered XAResourceOrphanFilter will be consulted before any rollback on each recovery pass,
     * so they may change their mind over time e.g. if new information becomes available due to other recovery
     * activity.
     *
     * @param xid The in-doubt xid.
     * @return a Vote in accordance with the guidelines above.
     */
    public Vote checkXid(Xid xid);
}