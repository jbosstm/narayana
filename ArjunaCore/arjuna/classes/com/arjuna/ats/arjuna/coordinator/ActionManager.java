/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import java.lang.reflect.Field;
import java.util.Map;

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

    @Deprecated
    // This method will be removed at the next major release
    public void put(BasicAction act) {
        putIntoBasicActionMap(act);
    }

    public BasicAction get(Uid id) {
        return BasicAction.getAllActions().get(id);
    }

    @Deprecated
    // This method will be removed at the next major release
    public void remove(Uid id) {
        removeFromBasicActionMap(id);
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
        return (int) BasicAction.getAllActions().entrySet()
                .parallelStream()
                .filter(entry -> entry.getValue().status() == ActionStatus.RUNNING)
                .count();
    }

    private ActionManager() {
    }

    /**
     * Workaround to put BasicAction into BasicAction._allActions,
     * which is a private static field.
     * @param txn is the BasicAction to put back into _allActions
     */
    @Deprecated
    // This will be removed as soon as the dependent method gets deprecated
    private static void putIntoBasicActionMap(BasicAction txn) {
        try {
            Field allActions = BasicAction.class.getDeclaredField("_allActions");
            allActions.setAccessible(true);
            ((Map<Uid, BasicAction>) allActions.get(null)).put(txn.get_uid(), txn);
        } catch (NoSuchFieldException NSFEx) {
            throw new RuntimeException("_allActions doesn't exist");
        } catch (IllegalAccessException IAEx) {
            throw new RuntimeException("problems getting _allActions with reflection");
        }
    }

    /**
     * Workaround to remove BasicAction from BasicAction._allActions,
     * which is a private static field.
     * @param uid is the BasicAction's uid to remove from _allActions
     */
    @Deprecated
    // This will be removed as soon as the dependent method gets deprecated
    private static void removeFromBasicActionMap(Uid uid) {
        try {
            Field allActions = BasicAction.class.getDeclaredField("_allActions");
            allActions.setAccessible(true);
            ((Map<Uid, BasicAction>) allActions.get(null)).remove(uid);
        } catch (NoSuchFieldException NSFEx) {
            throw new RuntimeException("_allActions doesn't exist");
        } catch (IllegalAccessException IAEx) {
            throw new RuntimeException("problems getting _allActions with reflection");
        }
    }

    private static final ActionManager _theManager = new ActionManager();
}