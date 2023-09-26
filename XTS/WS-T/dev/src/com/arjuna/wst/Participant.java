/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.wst;

import com.arjuna.wst.SystemException;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;

/**
 * The base Participant.
 */
public interface Participant
{

    /**
     * Perform any work necessary to allow it to either commit or rollback
     * the work performed by the Web service under the scope of the
     * transaction. The implementation is free to do whatever it needs to in
     * order to fulfill the implicit contract between it and the coordinator.
     *
     * @return an indication of whether it can prepare or not.
     * @see com.arjuna.wst.Vote
     */

    public Vote prepare () throws WrongStateException, SystemException;

    /**
     * The participant should make permanent the work that it controls.
     */

    public void commit () throws WrongStateException, SystemException;

    /**
     * The participant should undo the work that it controls. The participant
     * will then return an indication of whether or not it succeeded.
     */

    public void rollback () throws WrongStateException, SystemException;

    /**
     * During recovery the participant can enquire as to the status of the
     * transaction it was registered with. If that transaction is no longer
     * available (has rolled back) then this operation will be invoked by the
     * coordination service.
     */

    public void unknown () throws SystemException;

    /**
     * During recovery the participant can enquire as to the status of the
     * transaction it was registered with. If an error occurs (e.g., the
     * transaction service is unavailable) then this operation will be invoked.
     */
    
    void error () throws SystemException;
    
}