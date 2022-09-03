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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionManager;

import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.SynchronizationUnavailable;
import org.omg.CosTransactions.Unavailable;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class TransactionManagerGetStatusDifferenceDemonstrator {

	@Test
	public void test() throws InvalidName, SystemException,
			NotSupportedException, jakarta.transaction.SystemException,
			IllegalStateException, RollbackException, IOException,
			SecurityException, HeuristicMixedException,
			HeuristicRollbackException, Unavailable,
			SynchronizationUnavailable, Inactive {

		String mode = "jts";
		TransactionManager transactionManager;
		if (mode.equals("jts")) {
			ORB myORB = ORB.getInstance("test");
			RootOA myOA = OA.getRootOA(myORB);

			myORB.initORB(new String[0], null);
			myOA.initOA();

			com.arjuna.ats.internal.jts.ORBManager.setORB(myORB);
			com.arjuna.ats.internal.jts.ORBManager.setPOA(myOA);

			RecoveryManager.manager().initialize();
			transactionManager = new com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple();
		} else {
			transactionManager = new com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple();
		}

		transactionManager.begin();

		GetStatusSync getStatusSync = new GetStatusSync(transactionManager);
		transactionManager.getTransaction().registerSynchronization(
				getStatusSync);

		transactionManager.commit();

		try {
			if (mode.equals("jts")) {
				String orbClassName = System.getProperty("OrbPortabilityEnvironmentBean.orbImpleClassName");

				System.out.printf("%s: orbClassName=%s%n", this.getClass().getName(), orbClassName);

				assertTrue(
						"Status: " + getStatusSync.getTransactionManagerGetStatus(),
						getStatusSync.getTransactionManagerGetStatus() == Status.STATUS_COMMITTED);
			} else {
				assertTrue(
						"Status: "
								+ getStatusSync
										.getTransactionManagerGetStatus(),
						getStatusSync.getTransactionManagerGetStatus() == Status.STATUS_COMMITTED);
			}

			assertTrue(getStatusSync.getAfterCompletionStatus() == Status.STATUS_COMMITTED);
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

	private class GetStatusSync implements Synchronization {

		private int transactionManagerGetStatus;
		private int afterCompletionStatus;
		private TransactionManager transactionManager;

		public GetStatusSync(TransactionManager transactionManager) {
			this.transactionManager = transactionManager;
		}

		public void beforeCompletion() {
		}

		public void afterCompletion(int status) {

			afterCompletionStatus = status;

			try {
				this.transactionManagerGetStatus = transactionManager
						.getStatus();
			} catch (jakarta.transaction.SystemException e) {
				e.printStackTrace();
			}
		}

		public int getTransactionManagerGetStatus() {
			return transactionManagerGetStatus;
		}

		public int getAfterCompletionStatus() {
			return afterCompletionStatus;
		}
	}
}
