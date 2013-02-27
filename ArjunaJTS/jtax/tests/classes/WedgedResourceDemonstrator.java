/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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

import static org.junit.Assert.fail;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.ORBPackage.InvalidName;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class WedgedResourceDemonstrator {

	@Test
	public void testWedge() throws InvalidName, SystemException,
			NotSupportedException, javax.transaction.SystemException,
			IllegalStateException, RollbackException, SecurityException,
			HeuristicMixedException, HeuristicRollbackException,
			InterruptedException {

		String mode = "jts";
		if (mode.equals("jts")) {
			ORB myORB = ORB.getInstance("test");
			RootOA myOA = OA.getRootOA(myORB);

			myORB.initORB(new String[0], null);
			myOA.initOA();

			com.arjuna.ats.internal.jts.ORBManager.setORB(myORB);
			com.arjuna.ats.internal.jts.ORBManager.setPOA(myOA);

			RecoveryManager.manager().initialize();
		}

		TransactionManager transactionManager = mode.equals("jts") ? new com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple()
				: new com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple();
		transactionManager.setTransactionTimeout(2);
		transactionManager.begin();
		transactionManager.getTransaction().enlistResource(
				new TimeoutOnFirstRollbackResource());

		// Business logic
		Thread.currentThread().sleep(5000);

		try {
			transactionManager.commit();
			fail("Should not have been able to commit");
		} catch (RollbackException e) {
			// This is fine
		} finally {
			if (mode.equals("jts")) {
				RecoveryManager.manager().terminate();

				ORB myORB = ORB.getInstance("test");
				RootOA myOA = OA.getRootOA(myORB);
				myOA.destroy();
				myORB.shutdown();
			}
		}
	}

	private static class TimeoutOnFirstRollbackResource implements XAResource {

		public void rollback(Xid arg0) throws XAException {
			synchronized (this) {
				long initialTime = System.currentTimeMillis();
				try {
					// This would wait forever in theory, I have reduced it
					// just so the app will be able to clean up
					this.wait(7000);
				} catch (InterruptedException e) {
					throw new NullPointerException(
							"Interrupted, simulating jacorb");
				}
			}
		}

		public void commit(Xid arg0, boolean arg1) throws XAException {
		}

		public void end(Xid arg0, int arg1) throws XAException {
		}

		public void forget(Xid arg0) throws XAException {
		}

		public int getTransactionTimeout() throws XAException {
			return 0;
		}

		public boolean isSameRM(XAResource arg0) throws XAException {
			return false;
		}

		public int prepare(Xid arg0) throws XAException {
			return 0;
		}

		public Xid[] recover(int arg0) throws XAException {
			return null;
		}

		public boolean setTransactionTimeout(int arg0) throws XAException {
			return false;
		}

		public void start(Xid arg0, int arg1) throws XAException {
		}
	}
}
