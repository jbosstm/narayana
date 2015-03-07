/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package com.hp.mwtests.ts.jta.cdi.transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.annotation.Resource;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.Transactional.TxType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>
 * Implements various scenarios reproducing bug
 * <a href="https://issues.jboss.org/browse/JBTM-2350">JBTM-2350</a>.
 * </p>
 * <p>
 * Tests weather previousUserTransactionAvailability leaks between threads in certain race condition when
 * same method annotated transactional is invoked from two threads with different transaction management type
 * (user transaction availability).
 * </p>
 *
 * @author <a href="mailto:Tomasz%20Krakowiak%20%3ctomasz.krakowiak@efish.pl%3c">Tomasz Krakowiak
 *         &lt;tomasz.krakowiak@efish.pl&gt;</a>
 */
@RunWith(Arquillian.class)
public class ConcurrentTransactionalTest {
	@Inject
	TestTransactionalInvokerHelper helper;

	@Resource(name = "java:comp/DefaultManagedExecutorService")
	ExecutorService executorService;

	@Deployment
	public static WebArchive createTestArchive() {
		return ShrinkWrap.create(WebArchive.class, "test.war")
				.addPackage("com.hp.mwtests.ts.jta.cdi.transactional")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Test
	public void testTxRequiredFromCmt() {
		doTest(TransactionManagementType.CONTAINER, TxType.REQUIRED);
	}

	@Test
	public void testTxRequiredFromBmt() {
		doTest(TransactionManagementType.BEAN, TxType.REQUIRED);
	}

	@Test
	public void testTxRequiresNewFromCmt() {
		doTest(TransactionManagementType.CONTAINER, TxType.REQUIRES_NEW);
	}

	@Test
	public void testTxRequiresNewFromBmt() {
		doTest(TransactionManagementType.BEAN, TxType.REQUIRES_NEW);
	}

	@Test
	public void testTxMandatoryFromCmt() {
		doTest(TransactionManagementType.CONTAINER, TxType.MANDATORY);
	}

	@Test
	public void testTxMandatoryFromBmt() {
		doTest(TransactionManagementType.BEAN, TxType.MANDATORY);
	}

	@Test
	public void testTxSupportsFromCmt() {
		doTest(TransactionManagementType.CONTAINER, TxType.SUPPORTS);
	}

	@Test
	public void testTxSupportsFromBmt() {
		doTest(TransactionManagementType.BEAN, TxType.SUPPORTS);
	}

	@Test
	public void testTxNotSupportedFromCmt() {
		doTest(TransactionManagementType.CONTAINER, TxType.NOT_SUPPORTED);
	}

	@Test
	public void testTxNotSupportedFromBmt() {
		doTest(TransactionManagementType.BEAN, TxType.NOT_SUPPORTED);
	}

	@Test
	public void testTxNeverFromCmt() {
		doTest(TransactionManagementType.CONTAINER, TxType.NEVER);
	}

	@Test
	public void testTxNeverFromBmt() {
		doTest(TransactionManagementType.BEAN, TxType.NEVER);
	}

	/**
	 * <p>
	 * Following description referees to narayana implementation from version 5.0.0 to 5.0.4.
	 * </p>
	 * <p>
	 * We have a bean - {@link TestTransactionalInvokerBean testTransactionalInvokerBean} -
	 * with a method annotated transactional using tx type txType.
	 * </p>
	 * <p>
	 * This method is being called from two threads, where:
	 * </p>
	 * <ul>
	 * <li>
	 * Thread 1 is participating in transactionManagementType, therefore it's
	 * {@link ServerVMClientUserTransaction#isAvailables} value is true if it's value is
	 * {@link TransactionManagementType#BEAN BEAN}.
	 * </li>
	 * <li>
	 * Thread 2 is participating in transaction management type opposite to transactionManagementType,
	 * therefore it's {@link ServerVMClientUserTransaction#isAvailables} value is different than for Thread 1.
	 * </li>
	 * </ul>
	 * <p>
	 * Both threads call {@link TestTransactionalInvokerBean testTransactionalInvokerBean}'s method with
	 * appropriate tx type at the same time.
	 * </p>
	 * <p>
	 * What may happen:
	 * </p>
	 * <ol>
	 * <li>
	 * 1. Thread 1 - enters {@link TestTransactionalInvokerBean testTransactionalInvokerBean}'s method.<br/>
	 * Interceptor sets previousUserTransactionAvailability.
	 * </li>
	 * <li>
	 * 2. Thread 2 - enters {@link TestTransactionalInvokerBean testTransactionalInvokerBean}'s method.<br/>
	 * Interceptor sets previousUserTransactionAvailability to opposite value that it was set in Thread 1.
	 * </li>
	 * <li>
	 * 3. Thread 1 - exits method {@link TestTransactionalInvokerBean testTransactionalInvokerBean}'s and
	 * {@link ServerVMClientUserTransaction#isAvailables} thread local value is set to incorrect value.
	 * </li>
	 * </ol>
	 *
	 * @param transactionManagementType transaction management type
	 * @param txType tx type to which related interceptor test to run.
	 */
	private void doTest(TransactionManagementType transactionManagementType, TxType txType) {
		CountDownLatch thread1EnterLatch = new CountDownLatch(1);
		CountDownLatch thread2StartLatch = thread1EnterLatch;
		CountDownLatch thread2EnterLatch = new CountDownLatch(1);
		CountDownLatch thread1ExitLatch = thread2EnterLatch;
		CountDownLatch thread2ExitLatch = new CountDownLatch(0);

		boolean startTransaction = txType == TxType.MANDATORY;

		TransactionManagementType otherTransactionManagementType = otherTransactionManagementType(transactionManagementType);
		boolean expectedUserTransactionAvailable = transactionManagementType == TransactionManagementType.BEAN;

		Runnable thread1Runnable = helper.runWithTransactionManagement(transactionManagementType, startTransaction,
				helper.runAndCheckUserTransactionAvailability(
						helper.runInTxType(txType,
								new DeterminingRaceRunnable(thread1EnterLatch, thread1ExitLatch)
						),
						expectedUserTransactionAvailable
				)
		);
		Runnable thread2Runnable = new AwaitAndRun(thread2StartLatch,
				helper.runWithTransactionManagement(otherTransactionManagementType, startTransaction,
						helper.runInTxType(txType,
								new DeterminingRaceRunnable(thread2EnterLatch, thread2ExitLatch)
						)
				)
		);

		Future<?> thread2Future = executorService.submit(thread2Runnable);
		thread1Runnable.run();
		try {
			thread2Future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * <p>
	 * Returns opposite transaction management type to transactionManagementType:
	 * </p>
	 * <ul>
	 * <li>{@link TransactionManagementType#BEAN} for {@link TransactionManagementType#CONTAINER}</li>
	 * <li>{@link TransactionManagementType#CONTAINER} for {@link TransactionManagementType#BEAN}</li>
	 * </ul>
	 *
	 * @param transactionManagementType transactionManagementType
	 * @return Opposite transaction management type to transactionManagementType
	 */
	private TransactionManagementType otherTransactionManagementType(TransactionManagementType transactionManagementType) {
		switch (transactionManagementType) {
			case CONTAINER:
				return TransactionManagementType.BEAN;
			case BEAN:
				return TransactionManagementType.CONTAINER;
			default:
				throw new RuntimeException("Unexpected transaction management type " + transactionManagementType);
		}
	}

	/**
	 * Waits for startLatch to be released and then invokes runnable.
	 */
	private class AwaitAndRun implements Runnable {
		private final CountDownLatch startLatch;
		private final Runnable runnable;

		public AwaitAndRun(CountDownLatch startLatch, Runnable runnable) {
			this.startLatch = startLatch;
			this.runnable = runnable;
		}

		@Override
		public void run() {
			try {
				startLatch.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			runnable.run();
		}
	}

	/**
	 * Releases enterLatch and waits for exitLatch to be released.
	 */
	private class DeterminingRaceRunnable implements Runnable {
		CountDownLatch enterLatch;
		CountDownLatch exitLatch;

		public DeterminingRaceRunnable(CountDownLatch enterLatch, CountDownLatch exitLatch) {
			this.enterLatch = enterLatch;
			this.exitLatch = exitLatch;
		}

		@Override
		public void run() {
			enterLatch.countDown();
			try {
				exitLatch.await();
			} catch (InterruptedException e) {
				throw new RuntimeException("Test was interrupted.", e);
			}
		}
	}
}
