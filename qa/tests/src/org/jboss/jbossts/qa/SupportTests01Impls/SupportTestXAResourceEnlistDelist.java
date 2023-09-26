/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.SupportTests01Impls;



/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */




import org.jboss.jbossts.qa.SupportTests01.*;

import jakarta.transaction.Transaction;
import javax.transaction.xa.XAResource;

public class SupportTestXAResourceEnlistDelist implements ServiceOperations
{
	public void test() throws InvocationException
	{
		try
		{
			// Get a reference to the transaction manager
			jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

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