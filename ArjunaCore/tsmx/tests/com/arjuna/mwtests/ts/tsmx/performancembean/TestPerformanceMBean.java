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
 * $Id: TestPerformanceMBean.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.mwtests.ts.tsmx.performancembean;

import com.arjuna.ats.tsmx.*;

import javax.management.ObjectName;

public class TestPerformanceMBean
{
	public static void main(String[] args)
	{
		TransactionServiceMX tsmx = TransactionServiceMX.getTransactionServiceMX();

		try
		{
			String objectName = TransactionServiceMX.getTransactionServiceMX().getObjectName("performancestatistics");

			System.out.println("Retrieving attribute 'NumberOfTransactions' from '"+objectName+"'");
			Object numberOfTransactions = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(new ObjectName(objectName), "NumberOfTransactions");
			System.out.println("Attribute 'NumberOfTransactions' = "+numberOfTransactions);

			System.out.println("Retrieving attribute 'NumberOfNestedTransactions' from '"+objectName+"'");
			Object numberOfNestedTransactions = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(new ObjectName(objectName), "NumberOfNestedTransactions");
			System.out.println("Attribute 'NumberOfNestedTransactions' = "+numberOfNestedTransactions);

			System.out.println("Retrieving attribute 'NumberOfHeuristics' from '"+objectName+"'");
			Object numberOfHeuristics = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(new ObjectName(objectName), "NumberOfHeuristics");
			System.out.println("Attribute 'NumberOfHeuristics' = "+numberOfHeuristics);

			System.out.println("Retrieving attribute 'NumberOfCommittedTransactions' from '"+objectName+"'");
			Object numberOfCommittedTransactions = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(new ObjectName(objectName), "NumberOfCommittedTransactions");
			System.out.println("Attribute 'NumberOfCommittedTransactions' = "+numberOfCommittedTransactions);

			System.out.println("Retrieving attribute 'NumberOfAbortedTransactions' from '"+objectName+"'");
			Object numberOfAbortedTransactions = TransactionServiceMX.getTransactionServiceMX().getAgentInterface().getAgent().getAttribute(new ObjectName(objectName), "NumberOfAbortedTransactions");
			System.out.println("Attribute 'NumberOfAbortedTransactions' = "+numberOfAbortedTransactions);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		boolean success = tsmx.unregisterMBeans();

		System.out.println("Unregistering mbeans: "+ (success ? "successful" : "failed") );
	}
}
