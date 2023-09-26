/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.outbound.utility;

import com.arjuna.wst.*;
import org.jboss.logging.Logger;

import java.io.Serializable;

/**
 * Implementation of Durable2PCParticipant for use in tx test cases.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-03
 */
public class TestDurableParticipant implements Durable2PCParticipant, Serializable {
    private static Logger log = Logger.getLogger(TestDurableParticipant.class);

    /*
     * Uniq String used to prefix ids at participant registration,
     * so that the recovery module can identify relevant instances.
     */
    public static final String TYPE_IDENTIFIER = "TestDurableParticipant_";

    private String prepareOutcome = "prepared";

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

        if ("aborted".equals(prepareOutcome)) {
            log.trace("prepare returning Aborted");
            return new Aborted();
        } else if ("readonly".equals(prepareOutcome)) {
            log.trace("prepare returning ReadOnly");
            return new ReadOnly();
        } else {
            return new Prepared();
        }
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