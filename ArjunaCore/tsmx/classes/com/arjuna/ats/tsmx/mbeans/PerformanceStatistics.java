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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 * 
 * $Id: PerformanceStatistics.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.tsmx.mbeans;

import com.arjuna.ats.arjuna.coordinator.TxStats;

/**
 * @deprecated
 */
@Deprecated
public class PerformanceStatistics implements PerformanceStatisticsMBean
{
	private final static String ICON_FILENAME="performance-icon.gif";

	/**
	 * Get the number of transactions created so far (includes nested and top-level transactions).
	 *
	 * @return The number of transactions created.
	 */
	public long getNumberOfTransactions()
	{
		return TxStats.getInstance().getNumberOfTransactions();
	}

	/**
	 * Get the number of nested (sub) transactions created so far.
	 *
	 * @return The number of nested transactions created so far.
	 */
	public long getNumberOfNestedTransactions()
	{
		return TxStats.getInstance().getNumberOfNestedTransactions();
	}

	/**
	 * Get the number of transactions which have terminated with
	 * a heuristic outcome.
	 *
	 * @return The number of transactions which have terminated with a heuristic outcome.
	 */
	public long getNumberOfHeuristics()
	{
		return TxStats.getInstance().getNumberOfHeuristics();
	}

	/**
	 * Get the number of transactions which have been committed.
	 *
	 * @return The number of transactions which have been committed.
	 */
	public long getNumberOfCommittedTransactions()
	{
		return TxStats.getInstance().getNumberOfCommittedTransactions();
	}

	/**
	 * Get the number of transactions which have been aborted.
	 *
	 * @return The number of transactions which have been aborted.
	 */
	public long getNumberOfAbortedTransactions()
	{
		return TxStats.getInstance().getNumberOfAbortedTransactions();
	}

	public String getIconFilename()
	{
		return ICON_FILENAME;
	}
}
