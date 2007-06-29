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
package com.hp.mwtests.ts.jta.subordinate;

import java.io.File;
import java.io.PrintWriter;

import junit.framework.TestCase;

import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.TransactionImple;

public class SubordinateTestCase extends TestCase
{
	public void testCleanupCommit () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final TransactionImple tm = new TransactionImple(0); // implicit begin

			tm.doPrepare();
			tm.doCommit();
		}
		
		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}
	
	public void testCleanupRollback () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final TransactionImple tm = new TransactionImple(0); // implicit begin

			tm.doRollback();
		}
		
		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}
	
	public void testCleanupSecondPhaseRollback () throws Exception
	{	
		for (int i = 0; i < 1000; i++)
		{
			final TransactionImple tm = new TransactionImple(0); // implicit begin

			tm.doPrepare();
			tm.doRollback();
		}
		
		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}
	
	public void testCleanupOnePhaseCommit () throws Exception
	{
		for (int i = 0; i < 1000; i++)
		{
			final TransactionImple tm = new TransactionImple(0); // implicit begin

			tm.doOnePhaseCommit();
		}
		
		assertEquals(ActionManager.manager().inflightTransactions().size(), 0);
	}
}
