/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2003, 2004
 * Arjuna Technologies Limited
 * Newcastle upon Tyne, UK
 *
 * $Id: AccountResource.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.demo.jts.localbank;

/**
 * The AccountResource shows a sample ResourcePOA implementation. It is provided as part of the JBoss Transactions
 * product trailmap showing a sample bank application built using JTS.
 *
 * Although the class does not demonstrate it, of particular interest is the Heuristic exceptions that may be raised
 * by various of the resource's methods. Briefly, if a resources fail for whatever reason the API is often verbose
 * enough to  return a brief explanation of why this resource is having to fail.
 *
 * For example, the HeuristicRollback/HeuristicCommit exceptions are raised by their reciprocol commit/rollback operations
 * if, while waiting for transaction completion, the resource had unilaterally decided to rollback (or commit). Alternatively,
 * if the resource is acting as a subordinate coodrinator, it may return either HeuristicMixed (if subresources return
 * a mix of HeuristicRollback/HeuristicCommit) or HeuristicHazard (if the subresources return a complete set of either
 * HeuristicRollback or HeuristicCommit but some are not able to return either for an unexpected reason).
 *
 * The implementations of ResourcePOA from the trailmap examples jts.explicitremotebank, jts.remotebank and
 * jts.localbank are all identical.
 */
public class AccountResource extends org.omg.CosTransactions.ResourcePOA
{
    /**
     * The name of this account.
     */
    private String _name = null;

    /**
     * This contains the last commited balance. The <CODE>Account</CODE> state balance is written at transaction commit
     * time.
     */
    private Account _account;

    /**
     * This is the last known good balance of the account object. It is the last commited state of the balance.
     */
    private float _initial_balance;

    /**
     * This is the current working balance of the account object. It is the uncommited state of the balance.
     */
    private float _current_balance;

    /**
     * Create a new account POA resource for the account object.
     *
     * @param account   The account this resource is created for.
     * @param name      The name of the account.
     */
    public AccountResource(Account account, String name)
    {
        _name = name;
        _account = account;
        _initial_balance = account._balance;
        _current_balance = _initial_balance;
    }

    /**
     * This will obtain the current balance of this account as seen within the scope of the current transaction.
     *
     * @return  The current balance in this transaction
     */
    public float balance()
    {
        return _current_balance;
    }

    /**
     * This will update the resources uncommited state to credit it.
     *
     * @param value The amount to increment the uncommited balance by.
     */
    public void credit(float value)
    {
        _current_balance += value;
    }

    /**
     * This will update the resources uncommited state to debit it.
     *
     * @param value The amount to decrement the uncommited balance by.
     */
    public void debit(float value)
    {
        _current_balance -= value;
    }

    /**
     * Transactional prepare for the resource to perform its work within the scope of the transaction pending
     * transactional completion.
     *
     * @return                                          Whether this resource is prepared to commit.
     *
     * @throws org.omg.CosTransactions.HeuristicMixed   If, while acting as a subordinate coordinator, the resource
     *                                                  is presented with an inconsistent return set of votes.
     * @throws org.omg.CosTransactions.HeuristicHazard  If, while acting as a subordinate coordinator, the resource
     *                                                  is presented with an consistent return set of votes but the set
     *                                                  included some unknown results.
     */
    public org.omg.CosTransactions.Vote prepare() throws org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
    {
        System.out.println("[ Resource for " + _name + " : Prepare ]");

        if (_initial_balance == _current_balance)
        {
            // No change
            disassociateThisWithAccount();
            return org.omg.CosTransactions.Vote.VoteReadOnly;
        }

        if (_current_balance < 0)
        {
            disassociateThisWithAccount();
            return org.omg.CosTransactions.Vote.VoteRollback;
        }
        return org.omg.CosTransactions.Vote.VoteCommit;
    }

    /**
     * Transactionally rolls back the updates. Although this implementation of ResourcePOA is rather trivial, the Javadoc
     * details some of the exceptions it could be expected to raise.
     *
     * @throws org.omg.CosTransactions.HeuristicCommit      If the resource had made a heuristic descision to commit.
     * @throws org.omg.CosTransactions.HeuristicMixed       If, acting as a subordinate coordinate, the resource's
     *                                                      subresources had made a heuristic split of commits,
     *                                                      rollbacks or some with no knowledge.
     * @throws org.omg.CosTransactions.HeuristicHazard      If all the subresources returned commit or all returned
     *                                                      rollback but some had no knowledge of the transaction.
     */
    public void rollback() throws org.omg.CosTransactions.HeuristicCommit, org.omg.CosTransactions.HeuristicMixed,
            org.omg.CosTransactions.HeuristicHazard
    {
        System.out.println("[ Resource for " + _name + " : Rollback ]");
        disassociateThisWithAccount();
    }

    /**
     * Transactionally commit the updates. Although this implementation of ResourcePOA is rather trivial, the Javadoc
     * details some of the exceptions it could be expected to raise.
     *
     * @throws org.omg.CosTransactions.NotPrepared          If the resource had not been prepared first.
     * @throws org.omg.CosTransactions.HeuristicRollback    If the resource had made a heuristic descision to rollback.
     * @throws org.omg.CosTransactions.HeuristicMixed       If, acting as a subordinate coordinate, the resource's
     *                                                      subresources had made a heuristic split of commits,
     *                                                      rollbacks or some with no knowledge.
     * @throws org.omg.CosTransactions.HeuristicHazard      If all the subresources returned commit or all returned
     *                                                      rollback but some had no knowledge of the transaction.
     */
    public void commit() throws org.omg.CosTransactions.NotPrepared, org.omg.CosTransactions.HeuristicRollback,
            org.omg.CosTransactions.HeuristicMixed, org.omg.CosTransactions.HeuristicHazard
    {
        System.out.println("[ Resource for " + _name + " : Commit ]");
        _account._balance = _current_balance;
        disassociateThisWithAccount();
    }

    /**
     * Commit with one phase semantics.
     *
     * @throws org.omg.CosTransactions.HeuristicHazard      If all the subresources returned commit or all returned
     *                                                      rollback but some had no knowledge of the transaction.
     */
    public void commit_one_phase() throws org.omg.CosTransactions.HeuristicHazard
    {
        System.out.println("[ Resource for " + _name + " : Commit one phase ]");
        _account._balance = _current_balance;
        disassociateThisWithAccount();
    }

    /**
     * Forget this transaction.
     */
    public void forget()
    {
        System.out.println("[ Resource for " + _name + " : Forget ]");
        disassociateThisWithAccount();
    }

    /**
     * This method breaks the link with the <CODE>Account</CODE> object. This implementation of XA Resource does not
     * use pooling so when this call returns the <CODE>AccountResource</CODE> is totally dereferenced and is ready
     * for garbage collection.
     */
    private void disassociateThisWithAccount()
    {
        _account.accRes = null;
    }
}
