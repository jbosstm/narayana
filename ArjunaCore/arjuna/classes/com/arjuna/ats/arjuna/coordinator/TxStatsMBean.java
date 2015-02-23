/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.ats.arjuna.coordinator;

/**
 * MBean interface for monitoring transaction statistics.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface TxStatsMBean
{
    /**
     * Returns the total number of transactions (top-level and nested) created
     * @return the total number of transactions created
     */
    long getNumberOfTransactions();

    /**
     * Returns the total number of nested (sub) transactions created.
     * Note: in JTA environments will normally be 0, since JTA disallows nested tx by default.
     * @return the total number of nested (sub) transactions created
     */
    long getNumberOfNestedTransactions();

    /**
     * Returns the number of transactions which have terminated with heuristic outcomes.
     * @return the transactions which have terminated with heuristic outcomes
     */
    long getNumberOfHeuristics();

    /**
     * Returns the number of committed transactions
     * @return rhe number of committed transactions
     */
    long getNumberOfCommittedTransactions();

    /**
     * Returns the average time, in nanoseconds, it is taking to commit a transaction. This time is
     * measured from the moment the client calls commit until the transaction manager determines that the
     * commit attempt was successful (ie that all participants successfully committed). This includes cases where:
     *
     * <ul>
     *   <li>there are no transaction participants;
     *   <li>the transaction only contains readonly participants;
     * </ul>
     *
     * The average will not be updated if any participants failed to commit.
     *
     * Note that a small number of stuck transactions can skew the overall average. Similarly the average time
     * will be reduced if there are many transactions without participants or with only readonly participants.
     *
     * @return the average time, in nanoseconds, it has taken to commit a transaction.
     */
    long getAverageCommitTime();

    /**
     * Returns the number of aborted (i.e. rolledback) transactions
     * @return The number of rolledback transactions.
     */
    long getNumberOfAbortedTransactions();

    /**
     * Get the number of transactions that have begun but not yet terminated.
     * Note: This count is approximate, particularly in recovery situations.
     * @return the number of transactions that have begun but not yet terminated
     */
    long getNumberOfInflightTransactions();

    /**
     * Returns the number of transactions that have rolled back due to timeout.
     * @return the number of transactions that have rolled back due to timeout.
     */
    long getNumberOfTimedOutTransactions();

    /**
     * Returns the number of transactions that have been rolled back by application request.
     * This includes those that timeout, since the timeout behaviour is considered an
     * attribute of the application configuration.
     * @return the number of transactions that have been rolled back by application request.
     */
    long getNumberOfApplicationRollbacks();

    /**
     * Returns the number of transactions that have been rolled back due to internal system errors including
     * failure to create log storage and failure to write a transaction log. It does not include rollbacks
     * caused by resource failures.
     * @return the number of transactions that been rolled back due to internal system errors
     */
    long getNumberOfSystemRollbacks();

    /**
     * Returns the number of transactions that rolled back due to resource (participant) failure.
     * @return the number of transactions that rolled back due to resource (participant) failure.
     */
    long getNumberOfResourceRollbacks();
}
