/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2008,
 * @author JBoss Inc.
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

