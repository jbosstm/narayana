/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.utility;

import com.arjuna.wst.*;
import org.jboss.logging.Logger;

/**
 * Implementation of Volatile2PCParticipant for use in tx test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-03
 */
public class TestVolatileParticipant implements Volatile2PCParticipant {
    private static Logger log = Logger.getLogger(TestVolatileParticipant.class);

    /**
     * Perform any work necessary to allow it to either commit or rollback
     * the work performed by the Web service under the scope of the
     * transaction. The implementation is free to do whatever it needs to in
     * order to fulfill the implicit contract between it and the coordinator.
     *
     * @return an indication of whether it can prepare or not.
     * @see com.arjuna.wst.Vote
     */
    @Override
    public Vote prepare() throws WrongStateException, SystemException {
        log.trace("prepare()");

        return new Prepared(); // or Aborted()
    }

    /**
     * The participant should make permanent the work that it controls.
     */
    @Override
    public void commit() throws WrongStateException, SystemException {
        log.trace("commit()");
    }

    /**
     * The participant should undo the work that it controls. The participant
     * will then return an indication of whether or not it succeeded.
     */
    @Override
    public void rollback() throws WrongStateException, SystemException {
        log.trace("rollback()");
    }

    /**
     * During recovery the participant can enquire as to the status of the
     * transaction it was registered with. If that transaction is no longer
     * available (has rolled back) then this operation will be invoked by the
     * coordination service.
     */
    @Override
    public void unknown() throws SystemException {
        log.trace("unknown()");
    }

    /**
     * During recovery the participant can enquire as to the status of the
     * transaction it was registered with. If an error occurs (e.g., the
     * transaction service is unavailable) then this operation will be invoked.
     */
    @Override
    public void error() throws SystemException {
        log.trace("error()");
    }
}