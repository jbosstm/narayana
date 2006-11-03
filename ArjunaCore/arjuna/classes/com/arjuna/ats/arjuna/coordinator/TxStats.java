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

/**
 * This class is used to maintain statistics on transactions that
 * have been created. This includes the number of transactions, their
 * termination status (committed or rolled back), ...
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TxStats.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public class TxStats
{

    /**
     * @return the number of transactions (top-level and nested)
     * created so far.
     */

    public static int numberOfTransactions ()
    {
	synchronized (_ntxLock)
	{
	    return _numberOfTransactions;
	}
    }

    /**
     * @return the number of nested (sub) transactions created so far.
     */

    public static int numberOfNestedTransactions ()
    {
	synchronized (_nntxLock)
	{
	    return _numberOfNestedTransactions;
	}
    }
    
    /**
     * @return the number of transactions which have terminated with
     * heuristic outcomes.
     */

    public static int numberOfHeuristics ()
    {
	synchronized (_nhLock)
	{
	    return _numberOfHeuristics;
	}
    }

    /**
     * @return the number of committed transactions.
     */

    public static int numberOfCommittedTransactions ()
    {
	synchronized (_ncmLock)
	{
	    return _numberOfCommittedTransactions;
	}
    }

    /**
     * @return the number of transactions which have rolled back.
     */

    public static int numberOfAbortedTransactions ()
    {
	synchronized (_nabLock)
	{
	    return _numberOfAbortedTransactions;
	}
    }

    public static void printStatus (java.io.PrintWriter pw)
    {
	pw.println("JBoss Transaction Service statistics.");
	pw.println(java.util.Calendar.getInstance().getTime()+"\n");
	
	pw.println("Number of transactions: "+numberOfTransactions());
	pw.println("Number of nested transactions: "+numberOfNestedTransactions());
	pw.println("Number of heuristics: "+numberOfHeuristics());
	pw.println("Number of committed transactions: "+numberOfCommittedTransactions());
	pw.println("Number of rolled back transactions: "+numberOfAbortedTransactions());
    }
    
    static void incrementTransactions ()
    {
	synchronized (_ntxLock)
	{
	    _numberOfTransactions++;
	}
    }

    static void incrementNestedTransactions ()
    {
	synchronized (_nntxLock)
	{
	    _numberOfNestedTransactions++;
	}
    }

    static void incrementAbortedTransactions ()
    {
	synchronized (_nabLock)
	{
	    _numberOfAbortedTransactions++;
	}
    }

    static void incrementCommittedTransactions ()
    {
	synchronized (_ncmLock)
	{
	    _numberOfCommittedTransactions++;
	}
    }

    static void incrementHeuristics ()
    {
	synchronized (_nhLock)
	{
	    _numberOfHeuristics++;
	}
    }
    
    private static int _numberOfTransactions = 0;
    private static java.lang.Object _ntxLock = new Object();
    private static int _numberOfNestedTransactions = 0;
    private static java.lang.Object _nntxLock = new Object();
    private static int _numberOfCommittedTransactions = 0;
    private static java.lang.Object _ncmLock = new Object();
    private static int _numberOfAbortedTransactions = 0;
    private static java.lang.Object _nabLock = new Object();
    private static int _numberOfHeuristics = 0;
    private static java.lang.Object _nhLock = new Object();
    
}
