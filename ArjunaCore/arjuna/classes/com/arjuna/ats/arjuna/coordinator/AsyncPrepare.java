/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import java.util.concurrent.Callable;

/**
 * Instances of this class are responsible for performing asynchronous
 * prepare on a specific AbstractRecord associated with a transaction.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: AsyncPrepare.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

/*
 * Default visibility.
 */
class AsyncPrepare implements Callable<Integer> {
    public Integer call() throws Exception {
        /*
                   * This is a transient thread so we don't want to register it
                   * with the action it is preparing, only change its notion of
                   * the current transaction so that any abstract records that
                   * need that information can still have it.
                   */

        ThreadActionData.pushAction(_theAction, false);

        _outcome = _theAction.doPrepare(_reportHeuristics, _theRecord);

        ThreadActionData.popAction(false);

        return _outcome;
    }

    protected AsyncPrepare(BasicAction act, boolean reportHeuristics, AbstractRecord rec) {
        _theAction = act;
        _outcome = TwoPhaseOutcome.PREPARE_NOTOK;
        _reportHeuristics = reportHeuristics;
        _theRecord = rec;
    }

    private BasicAction _theAction;
    private int _outcome;
    private boolean _reportHeuristics;
    private AbstractRecord _theRecord;
};