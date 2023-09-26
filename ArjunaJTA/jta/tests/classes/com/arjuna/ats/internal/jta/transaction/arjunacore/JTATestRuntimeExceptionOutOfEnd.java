/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.arjunacore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.transaction.Synchronization;
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