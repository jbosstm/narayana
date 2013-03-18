package org.jboss.narayana.jta;
/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import static org.junit.Assert.assertTrue;

import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

public class DoubleEnlistXAResource {

	@Test
	public void test() throws NotSupportedException, SystemException,
			IllegalStateException, RollbackException {
		javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
				.transactionManager();

		tm.begin();

		FirstXAResource resource1 = new FirstXAResource();
		SecondXAResource resource2 = new SecondXAResource();
		javax.transaction.Transaction theTransaction = tm.getTransaction();

		theTransaction.enlistResource(resource1);
		theTransaction.enlistResource(resource2);

		assertTrue(resource1.isSameRMCalledAsExpected());
		assertTrue(resource2.startNotCalled());

		tm.rollback();

	}

	private class FirstXAResource implements XAResource {

		private boolean isSameRMCalledAsExpected;

		public boolean isSameRMCalledAsExpected() {
			return isSameRMCalledAsExpected;
		}

		@Override
		public boolean isSameRM(XAResource xares) throws XAException {
			isSameRMCalledAsExpected = xares instanceof SecondXAResource;
			return isSameRMCalledAsExpected;
		}

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public void end(Xid xid, int flags) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public void forget(Xid xid) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public int getTransactionTimeout() throws XAException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int prepare(Xid xid) throws XAException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Xid[] recover(int flag) throws XAException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void rollback(Xid xid) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean setTransactionTimeout(int seconds) throws XAException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void start(Xid xid, int flags) throws XAException {
			// TODO Auto-generated method stub

		}

	}

	private class SecondXAResource implements XAResource {

		private boolean startCalled;

		public boolean startNotCalled() {
			// TODO Auto-generated method stub
			return !startCalled;
		}

		@Override
		public void start(Xid xid, int flags) throws XAException {
			startCalled = true;
		}

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isSameRM(XAResource xares) throws XAException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void end(Xid xid, int flags) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public void forget(Xid xid) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public int getTransactionTimeout() throws XAException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int prepare(Xid xid) throws XAException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Xid[] recover(int flag) throws XAException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void rollback(Xid xid) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean setTransactionTimeout(int seconds) throws XAException {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
