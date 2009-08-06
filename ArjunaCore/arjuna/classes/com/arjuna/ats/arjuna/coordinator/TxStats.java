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

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.util.concurrent.atomic.AtomicLong;

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

    public static boolean enabled() {
        return arjPropertyManager.getCoordinatorEnvironmentBean().isEnableStatistics();
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
        return numberOfCommittedTransactions.get();
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
		return ActionManager.manager().inflightTransactions().size();
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

	void incrementCommittedTransactions()
	{
        numberOfCommittedTransactions.incrementAndGet();
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
	
	void incrementResourceRollbacks ()
	{
        numberOfResourceAborts.incrementAndGet();
	}
	
	private AtomicLong numberOfTransactions = new AtomicLong(0);
	private AtomicLong numberOfNestedTransactions = new AtomicLong(0);
	private AtomicLong numberOfCommittedTransactions = new AtomicLong(0);
	private AtomicLong numberOfAbortedTransactions = new AtomicLong(0);
	private AtomicLong numberOfHeuristics = new AtomicLong(0);
	private AtomicLong numberOfTimeouts = new AtomicLong(0);
	private AtomicLong numberOfApplicationAborts = new AtomicLong(0);
	private AtomicLong numberOfResourceAborts = new AtomicLong(0);
}
