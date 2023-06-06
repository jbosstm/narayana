/*
 * SPDX short identifier: Apache-2.0
 */

package com.arjuna.ats;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.distributed.spi.TxListener;
import org.jboss.tm.listener.*;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate;

import java.util.EnumSet;
import java.util.Iterator;

public class TransactionListenerRegistryTest {
	private enum CompletionType {
		CMT,
		BMTCOMMIT,
		BMTROLLBACK,
		CMTSUSPEND
	};

	private static boolean wasListenersEnabled;

	@BeforeClass
	public static void setUp() {
		// transaction to thread listener is deprecated and deactivated by default with JBTM-3166
		wasListenersEnabled = jtaPropertyManager.getJTAEnvironmentBean().isTransactionToThreadListenersEnabled();
		jtaPropertyManager.getJTAEnvironmentBean().setTransactionToThreadListenersEnabled(true);
	}

	@AfterClass
	public static void tearDown() {
		jtaPropertyManager.getJTAEnvironmentBean().setTransactionToThreadListenersEnabled(wasListenersEnabled);
	}
	
  
  @Test
  public void testResume() throws SystemException, InvalidTransactionException {
    TransactionManager tm = new TransactionManagerDelegate();
		tm.resume(null); // JBTM-2385 used to cause an NPE 
  }

	private EnumSet<EventType> runTxn(TransactionManager tm) throws SystemException, TransactionTypeNotSupported, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
		ServerVMClientUserTransaction userTransaction = new ServerVMClientUserTransaction(tm);
		TransactionListenerRegistry listenerRegistration = (TransactionListenerRegistry) tm;
		final EnumSet<EventType> log = EnumSet.noneOf(EventType.class);

		TransactionListener listener = new TransactionListener() {
			@Override
			public void onEvent(TransactionEvent transactionEvent) {
				Iterator<EventType> events = transactionEvent.getTypes().iterator();

				while (events.hasNext()) {
					EventType e = events.next();

					log.add(e);
					System.out.printf("TransactionEvent: %s%n", e);
				}
			}
		};

		tm.suspend(); // clean the thread

		userTransaction.begin();
		listenerRegistration.addListener(tm.getTransaction(), listener, EnumSet.allOf(EventType.class));
		userTransaction.commit();

		return log;
	}

	@Test
	public void testIllegalCommit() throws SystemException, TransactionTypeNotSupported, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
		TransactionManager tm = new TransactionManagerDelegate();

		runTxn(tm);

		try {
			tm.commit();
			fail("Commit finished transaction should have failed");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testIllegalRollback() throws SystemException, TransactionTypeNotSupported, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
		TransactionManager tm = new TransactionManagerDelegate();

		runTxn(tm);

		try {
			tm.rollback();
			fail("Rollback finished transaction should have failed");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testLifecycle() throws SystemException, TransactionTypeNotSupported, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
		TransactionManager tm = new TransactionManagerDelegate();
		EnumSet<EventType> log = runTxn(tm);

		assertTrue(log.containsAll(EnumSet.of(EventType.ASSOCIATED, EventType.DISASSOCIATING)));
	}

	@Test
	public void test() throws SystemException, NotSupportedException, RollbackException, TransactionTypeNotSupported, InterruptedException, InvalidTransactionException, HeuristicRollbackException, HeuristicMixedException {

		TransactionManager tm = new TransactionManagerDelegate();
		ServerVMClientUserTransaction userTransaction = new ServerVMClientUserTransaction(tm);

		userTransaction.setTransactionTimeout(1);

		for (CompletionType completionType : CompletionType.values()) {
			TransactionListenerRegistry listenerRegistration = (TransactionListenerRegistry) tm;

			userTransaction.begin();

			// The TSR for interposed synchronizations
			final TransactionSynchronizationRegistry tsr = new TransactionSynchronizationRegistryImple();
			final TxListener listener = new TxListener(listenerRegistration);

			if (completionType != CompletionType.CMTSUSPEND) {
				tsr.registerInterposedSynchronization(listener);
			} else {
				tm.getTransaction().registerSynchronization(listener);
			}

			listenerRegistration.addListener(tm.getTransaction(), listener, EnumSet.allOf(EventType.class));

			if (completionType == CompletionType.CMTSUSPEND) {
				Transaction suspended = tm.suspend();
				Thread.sleep(2000);
				assertTrue(listener.shouldDisassoc());
				tm.resume(suspended);
			} else {
				Thread.sleep(2000);
				assertFalse(listener.shouldDisassoc());
			}

			assertTrue(listener.singleCallAC());

			assertTrue(Status.STATUS_ROLLEDBACK == userTransaction.getStatus());

			// Apps need to call rollback anyway to clear the association from
			// the
			// thread
			// https://community.jboss.org/thread/92489
			if (completionType == CompletionType.BMTCOMMIT) {
				try {
					userTransaction.commit();
					fail("Should not have been able to commit");
				} catch (RollbackException e) {
				}
			} else if (completionType == CompletionType.BMTROLLBACK) {
				userTransaction.rollback();
			} else if (completionType == CompletionType.CMT) {
				// This is possible in CMT mode
				// If they did check the status, it is still expected that a CMT
				// calls suspend at least when a tx is marked as completed to
				// clear
				// it from the thread
				tm.suspend();
			}

			assertTrue(listener.singleCallAC());
			if (completionType == CompletionType.CMTSUSPEND) {
				assertFalse(listener.shouldDisassoc());
			} else {
				assertTrue(listener.shouldDisassoc());
			}
			assertTrue(listener.isClosed());
		}
	}
}