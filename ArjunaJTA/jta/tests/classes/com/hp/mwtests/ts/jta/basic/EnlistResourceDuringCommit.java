/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.jta.basic;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;

public class EnlistResourceDuringCommit {

	@Test
	public void testEnlistResourceDuringBeforeCompletion()
			throws IllegalStateException, RollbackException, SystemException,
			SecurityException, HeuristicMixedException,
			HeuristicRollbackException {
		final TransactionImple tx = new TransactionImple(0);
		tx.registerSynchronization(new Synchronization() {

			@Override
			public void beforeCompletion() {
				System.out.println(new Throwable().getStackTrace()[0]
						.getMethodName());
				try {
					tx.enlistResource(new XAResource() {

						@Override
						public void start(Xid arg0, int arg1)
								throws XAException {
							System.out.println(new Throwable().getStackTrace()[0]
									.getMethodName());
						}

						@Override
						public boolean setTransactionTimeout(int arg0)
								throws XAException {
							System.out.println(new Throwable().getStackTrace()[0]
									.getMethodName());
							return false;
						}

						@Override
						public void rollback(Xid arg0) throws XAException {
							System.out.println(new Throwable().getStackTrace()[0]
									.getMethodName());
						}

						@Override
						public Xid[] recover(int arg0) throws XAException {
							System.out.println(new Throwable().getStackTrace()[0]
									.getMethodName());
							return null;
						}

						@Override
						public int prepare(Xid arg0) throws XAException {
							System.out.println(new Throwable().getStackTrace()[0]
									.getMethodName());
							return 0;
						}

						@Override
						public boolean isSameRM(XAResource arg0)
								throws XAException {
							System.out.println(new Throwable().getStackTrace()[0]
									.getMethodName());
							return false;
						}

						@Override
						public int getTransactionTimeout() throws XAException {
							System.out.println(new Throwable().getStackTrace()[0]
									.getMethodName());
							return 0;
						}

						@Override
						public void forget(Xid arg0) throws XAException {
							System.out.println(new Throwable().getStackTrace()[0]
									.getMethodName());
						}

						@Override
						public void end(Xid arg0, int arg1) throws XAException {
							System.out.println(new Throwable().getStackTrace()[0]
									.getMethodName());
						}

						@Override
						public void commit(Xid arg0, boolean arg1)
								throws XAException {
							System.out.println(new Throwable().getStackTrace()[0]
									.getMethodName());
						}
					});
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RollbackException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SystemException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void afterCompletion(int status) {
				System.out.println(new Throwable().getStackTrace()[0]
						.getMethodName());

			}
		});
		tx.commit();
	}

}
