/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: TxStats.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.arjuna.ats.arjuna.common.CoordinatorEnvironmentBean;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.internal.TxCommitStatistic;

/**
 * This class is used to maintain statistics on transactions that have been
 * created. This includes the number of transactions, their termination status
 * (committed or rolled back), ...
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TxStats.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 2.1.
 */

public class TxStats implements TxStatsMBean
{
    private static TxStats _instance = new TxStats();

    private static CoordinatorEnvironmentBean _environmentBean;

    private TxStats() {
    }

    public static boolean enabled() {
      //not thread safe but not sure we require thread safety as long as eventually all threads stop setting the bean
      if(_environmentBean==null){
        _environmentBean=arjPropertyManager.getCoordinatorEnvironmentBean();
      }
        return _environmentBean.isEnableStatistics();
    }

    public static TxStats getInstance() {
        return _instance;
    }

    /**
	 * @return the number of transactions (top-level and nested) created so far.
	 */
	public long getNumberOfTransactions()
	{
        return numberOfTransactions.longValue();
	}
    
    /**
	 * @return the number of nested (sub) transactions created so far.
	 */
	public long getNumberOfNestedTransactions()
	{
        return numberOfNestedTransactions.longValue();
	}

	/**
	 * @return the number of transactions which have terminated with heuristic
	 *         outcomes.
	 */
	public long getNumberOfHeuristics()
	{
        return numberOfHeuristics.get();
	}

	/**
	 * @return the number of committed transactions.
	 */
	public long getNumberOfCommittedTransactions()
	{
        return commitStatistic.get().getNumberOfCommittedTransactions();
	}

	/**
	 * @return the average time, in nanoseconds, it has taken to commit a transaction.
	 */
	public long getAverageCommitTime() {
		return commitStatistic.get().getAverageCommitTime();
	}

	/**
	 * @return the total number of transactions which have rolled back.
	 */
	public long getNumberOfAbortedTransactions()
	{
        return numberOfAbortedTransactions.get();
	}
	
	/**
	 * @return total number of inflight (active) transactions.
	 */
	public long getNumberOfInflightTransactions()
	{
		return ActionManager.manager().getNumberOfInflightTransactions();
	}

	/**
	 * @return the number of transactions that have rolled back due to timeout.
	 */
	public long getNumberOfTimedOutTransactions()
	{
        return numberOfTimeouts.get();
	}
	
	/**
	 * @return the number of transactions that been rolled back by the application.
	 */
	public long getNumberOfApplicationRollbacks()
	{
        return numberOfApplicationAborts.get();
	}

	/**
	 * @return the number of transactions that been rolled back due to internal system errors including
	 * failure to create log storage and failure to write a transaction log.
	 */
	public long getNumberOfSystemRollbacks()
	{
		return numberOfSystemAborts.get();
	}

	/**
	 * @return the number of transactions that have been rolled back by participants.
	 */
	public long getNumberOfResourceRollbacks()
	{
        return numberOfResourceAborts.get();
	}

	/**
	 * Print all of the current statistics information.
	 * 
	 * @param pw the writer to use.
	 */
	
	public void printStatus(java.io.PrintWriter pw)
	{
		pw.println("JBoss Transaction Service statistics.");
		pw.println(java.util.Calendar.getInstance().getTime() + "\n");

		pw.println("Number of created transactions: " + getNumberOfTransactions());
		pw.println("Number of nested transactions: "
				+ getNumberOfNestedTransactions());
		pw.println("Number of heuristics: " + getNumberOfHeuristics());
		pw.println("Number of committed transactions: "
				+ getNumberOfCommittedTransactions());
		pw.println("Number of rolled back transactions: "
				+ getNumberOfAbortedTransactions());
		pw.println("Number of inflight transactions: "
				+ getNumberOfInflightTransactions());
		pw.println("Number of timed-out transactions: "
				+ getNumberOfTimedOutTransactions());
		pw.println("Number of application rolled back transactions: "
				+ getNumberOfApplicationRollbacks());
		pw.println("Number of resource rolled back transactions: "
				+ getNumberOfResourceRollbacks());
		pw.println("Average time (in nanosecs) to commit a transaction: "
				+ getAverageCommitTime());
	}

	void incrementTransactions()
	{
        numberOfTransactions.incrementAndGet();
	}

	void incrementNestedTransactions()
	{
        numberOfNestedTransactions.incrementAndGet();
	}

	void incrementAbortedTransactions()
	{
        numberOfAbortedTransactions.incrementAndGet();
	}

	/**
	 * @deprecated as of 5.0.5.Final use {@link #incrementCommittedTransactions(long)}} instead
	 */
	@Deprecated
	void incrementCommittedTransactions()
	{
        incrementCommittedTransactions(0L);
	}

	/**
	 * Calculate a moving average:
	 *
	 * @param duration the new datum for updating the average
	 * @param prevCount the previous number of datums
	 * @param prevAvg the average of the previous prevCount datums
	 * @return the new average that includes the new datum according to the
	 * standard formula:
	 *   nextAvg = (duration  + prevCount * prevAvg) / (prevCount + 1)
	 */
	private long nextAverage(long duration, long prevCount, long prevAvg) {
		long nCount = prevCount + 1;
		double d1 = duration / nCount;
		double d2 = (prevAvg / nCount) * prevCount;

		return Math.round(d1 + d2);
	}

	/**
	 * @param duration the time in nanoseconds it took for the 2PC phase to complete. The averaged commit
	 *                   time is available by calling {@link #getAverageCommitTime()}
	 */
	void incrementCommittedTransactions(long duration) {
		TxCommitStatistic prev, next;

		do {
			prev = commitStatistic.get();

			long prevCount = prev.getNumberOfCommittedTransactions();
			long nextAvg = nextAverage(duration, prevCount, prev.getAverageCommitTime());

			next = new TxCommitStatistic(prevCount + 1, nextAvg);
		} while (!commitStatistic.compareAndSet(prev, next));
	}

	void incrementHeuristics()
	{
        numberOfHeuristics.incrementAndGet();
	}
	
	void incrementTimeouts ()
	{
        numberOfTimeouts.incrementAndGet();
	}

	void incrementApplicationRollbacks ()
	{
        numberOfApplicationAborts.incrementAndGet();
	}

	void incrementSystemRollbacks ()
	{
		numberOfSystemAborts.incrementAndGet();
	}

	void incrementResourceRollbacks ()
	{
        numberOfResourceAborts.incrementAndGet();
	}

	private AtomicLong numberOfTransactions = new AtomicLong(0);
	private AtomicLong numberOfNestedTransactions = new AtomicLong(0);
	private AtomicLong numberOfAbortedTransactions = new AtomicLong(0);
	private AtomicLong numberOfHeuristics = new AtomicLong(0);
	private AtomicLong numberOfTimeouts = new AtomicLong(0);
	private AtomicLong numberOfApplicationAborts = new AtomicLong(0);
	private AtomicLong numberOfSystemAborts = new AtomicLong(0);
	private AtomicLong numberOfResourceAborts = new AtomicLong(0);
	private AtomicReference<TxCommitStatistic> commitStatistic =
			new AtomicReference<>(new TxCommitStatistic(0, 0));
}
