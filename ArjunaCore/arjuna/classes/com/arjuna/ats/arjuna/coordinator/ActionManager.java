/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.arjuna.ats.arjuna.common.Uid;

/*
 * @author Mark Little (mark_little@hp.com)
 *
 * @version $Id: ActionManager.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 3.0
 */

public class ActionManager {

    public static ActionManager manager() {
        return _theManager;
    }

    public void put(BasicAction act) {
        _allActions.put(act.get_uid(), act);
    }

    public BasicAction get(Uid id) {
        return _allActions.get(id);
    }

    public void remove(Uid id) {
        _allActions.remove(id);
    }

    /**
     * <p>
     * This method works out the number of in-flight transactions currently in
     * the system.
     * <p>
     * Note: The definition of in-flight (a.k.a. active) transactions can be
     * found in the OMG OTS specification: "[Active is defined as] the state
     * of a transaction when processing is in progress and completion of the
     * transaction has not yet commenced." In addition to the OMG OTS
     * definition, transactions that are marked as abort-only are not
     * considered active.
     * In Arjuna, these considerations are distilled into the following:
     * in-flight transactions are those BasicActions with status() equal to
     * ActionStatus.RUNNING.
     *
     * @return the number of in-flight transactions currently in the system
     */
    public int getNumberOfInflightTransactions() {
        return (int) _allActions.entrySet()
                .parallelStream()
                .filter(entry -> entry.getValue().status() == ActionStatus.RUNNING)
                .count();
    }

    private ActionManager() {
    }

    private static final ActionManager _theManager = new ActionManager();

    private final Map<Uid, BasicAction> _allActions = new ConcurrentHashMap<>();
}