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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTATest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.transaction.Synchronization;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

public class JTATestRuntimeExceptionOutOfEnd {
	@Test
	public void test() throws Exception {

		TransactionImple theTransaction = new TransactionImple(3);

		assertTrue(theTransaction.enlistResource(new XAResource() {

			@Override
			public void start(Xid arg0, int arg1) throws XAException {

			}

			@Override
			public boolean setTransactionTimeout(int arg0) throws XAException {
				return false;
			}

			@Override
			public void rollback(Xid arg0) throws XAException {

			}

			@Override
			public Xid[] recover(int arg0) throws XAException {
				return null;
			}

			@Override
			public int prepare(Xid arg0) throws XAException {
				return 0;
			}

			@Override
			public boolean isSameRM(XAResource arg0) throws XAException {
				return false;
			}

			@Override
			public int getTransactionTimeout() throws XAException {
				return 0;
			}

			@Override
			public void forget(Xid arg0) throws XAException {

			}

			@Override
			public void end(Xid arg0, int arg1) throws XAException {

			}

			@Override
			public void commit(Xid arg0, boolean arg1) throws XAException {

			}
		}));
		assertTrue(theTransaction.enlistResource(new XAResource() {

			@Override
			public void start(Xid xid, int flags) throws XAException {

			}

			@Override
			public boolean setTransactionTimeout(int seconds) throws XAException {
				return false;
			}

			@Override
			public void rollback(Xid xid) throws XAException {

			}

			@Override
			public Xid[] recover(int flag) throws XAException {
				return null;
			}

			@Override
			public int prepare(Xid xid) throws XAException {
				return 0;
			}

			@Override
			public boolean isSameRM(XAResource xares) throws XAException {
				return false;
			}

			@Override
			public int getTransactionTimeout() throws XAException {
				return 0;
			}

			@Override
			public void forget(Xid xid) throws XAException {

			}

			@Override
			public void end(Xid xid, int flags) throws XAException {
				throw new RuntimeException();

			}

			@Override
			public void commit(Xid xid, boolean onePhase) throws XAException {

			}
		}));

		SynchronizationImpl s = new SynchronizationImpl();
		theTransaction.registerSynchronization(s);
		try {
			theTransaction.commit();
		} catch (Exception e) {
			// E.g. this could be the reaper
			theTransaction.getAtomicAction().cancel();
		}

		assertFalse(s.wasCalled());

	}

	private class SynchronizationImpl implements Synchronization {

		private boolean wasCalled;

		@Override
		public void afterCompletion(int arg0) {
			wasCalled = true;

		}

		public boolean wasCalled() {
			return wasCalled;
		}

		@Override
		public void beforeCompletion() {

		}

	}
}
