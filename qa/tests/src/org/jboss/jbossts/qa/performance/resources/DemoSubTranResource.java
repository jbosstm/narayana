/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.performance.resources;

import org.omg.CosTransactions.*;

public class DemoSubTranResource extends SubtransactionAwareResourcePOA
{
    /**
     * @see org.omg.CosTransactions.SubtransactionAwareResourceOperations#rollback_subtransaction()
     */
    public void rollback_subtransaction()
    {
        System.out.println("Sub Transaction: rollback subtransaction called");
    }

    /**
     * @see org.omg.CosTransactions.SubtransactionAwareResourceOperations#commit_subtransaction(Coordinator)
     */
    public void commit_subtransaction(Coordinator arg0)
    {
        System.out.println("Sub Transaction: commit subtransaction called");
    }

    /**
     * @see org.omg.CosTransactions.ResourceOperations#forget()
     */
    public void forget()
    {
        System.out.println("Sub Transaction: forget called");
    }

    /**
     * @see org.omg.CosTransactions.ResourceOperations#commit_one_phase()
     */
    public void commit_one_phase() throws HeuristicHazard
    {
        System.out.println("Sub Transaction: commit one phase called");
    }

    /**
     * @see org.omg.CosTransactions.ResourceOperations#commit()
     */
    public void commit() throws NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
        System.out.println("Sub Transaction: commit called");
    }

    /**
     * @see org.omg.CosTransactions.ResourceOperations#rollback()
     */
    public void rollback() throws HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
        System.out.println("Sub Transaction: rollback called");
    }

    /**
     * @see org.omg.CosTransactions.ResourceOperations#prepare()
     */
    public Vote prepare() throws HeuristicMixed, HeuristicHazard
    {
        System.out.println("Sub Transaction: prepare called");
        return Vote.VoteCommit;
    }
}