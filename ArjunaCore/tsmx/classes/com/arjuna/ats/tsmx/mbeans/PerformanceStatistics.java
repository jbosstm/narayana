/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.common.Environment;
import com.arjuna.ats.tsmx.mbeans.PerformanceStatisticsMBean;

import java.util.ArrayList;

public class PerformanceStatistics implements PerformanceStatisticsMBean
{
	private final static String ICON_FILENAME="performance-icon.gif";

	/**
	 * Get the number of transactions created so far (includes nested and top-level transactions).
	 *
	 * @return The number of transactions created.
	 */
	public int getNumberOfTransactions()
	{
		return TxStats.numberOfTransactions();
	}

	/**
	 * Get the number of nested (sub) transactions created so far.
	 *
	 * @return The number of nested transactions created so far.
	 */
	public int getNumberOfNestedTransactions()
	{
		return TxStats.numberOfNestedTransactions();
	}

	/**
	 * Get the number of transactions which have terminated with
	 * a heuristic outcome.
	 *
	 * @return The number of transactions which have terminated with a heuristic outcome.
	 */
	public int getNumberOfHeuristics()
	{
		return TxStats.numberOfHeuristics();
	}

	/**
	 * Get the number of transactions which have been committed.
	 *
	 * @return The number of transactions which have been committed.
	 */
	public int getNumberOfCommittedTransactions()
	{
		return TxStats.numberOfCommittedTransactions();
	}

	/**
	 * Get the number of transactions which have been aborted.
	 *
	 * @return The number of transactions which have been aborted.
	 */
	public int getNumberOfAbortedTransactions()
	{
		return TxStats.numberOfAbortedTransactions();
	}

	public String getIconFilename()
	{
		return ICON_FILENAME;
	}
}
