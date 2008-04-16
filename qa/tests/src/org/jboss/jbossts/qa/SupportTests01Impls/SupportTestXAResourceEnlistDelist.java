/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jbossts.qa.SupportTests01Impls;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SupportTestXAResourceEnlistDelist.java,v 1.2 2003/06/26 11:45:06 rbegg Exp $
 */

/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SupportTestXAResourceEnlistDelist.java,v 1.2 2003/06/26 11:45:06 rbegg Exp $
 */


import org.jboss.jbossts.qa.SupportTests01.*;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

public class SupportTestXAResourceEnlistDelist implements ServiceOperations
{
	public void test() throws InvocationException
	{
		try
		{
			// Get a reference to the transaction manager
			javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

			// Begin the transaction
			tm.begin();

			// Enlist the dummy resource within this transaction
			Transaction transaction = tm.getTransaction();
			DummyXAResource resource = new DummyXAResource();

			System.err.println("Enlisting XA Resource...");
			transaction.enlistResource(resource);
			_correct = (resource.getLastCalled() == DummyXAResource.StartLastCalled);

			System.err.println("Delisting XA Resource...");
			transaction.delistResource(resource, XAResource.TMSUCCESS);
			_correct &= (resource.getLastCalled() == DummyXAResource.EndLastCalled);

			// Clear the state
			resource.clearLastCalled();

			System.err.println("Performing commit...");
			tm.commit();

			_correct &= (resource.getLastCalled() == DummyXAResource.CommitLastCalled);
			System.err.println("Finish Trace: " + resource.getLastCalledString());
		}
		catch (Exception e)
		{
			throw new InvocationException();
		}
	}

	public boolean isCorrect() throws InvocationException
	{
		return (_correct);
	}

	private boolean _correct = false;
}
