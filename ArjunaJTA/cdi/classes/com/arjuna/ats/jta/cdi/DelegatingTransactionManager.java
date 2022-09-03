/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013-2019, Red Hat, Inc., and individual contributors
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

package com.arjuna.ats.jta.cdi;

import java.io.Serializable;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status; // for javadoc only
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

/**
 * An {@code abstract} {@link TransactionManager} implementation that
 * delegates all method invocations to another {@link
 * TransactionManager}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see TransactionManager
 */
public abstract class DelegatingTransactionManager implements Serializable, TransactionManager {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization} purposes.
   */
  private static final long serialVersionUID = 596L; // 596 ~= 5.9.9.Final-SNAPSHOT

  /**
   * The {@link TransactionManager} to which all operations will be
   * delegated.
   *
   * <p>This field may be {@code null}.</p>
   *
   * @see #DelegatingTransactionManager(TransactionManager)
   */
  private final transient TransactionManager delegate;

  /**
   * Creates a new {@link DelegatingTransactionManager}.
   *
   * @param delegate the {@link TransactionManager} to which all
   * method invocations will be delegated; may be {@code null}, but
   * all methods in this class will then throw a {@link
   * SystemException}
   */
  protected DelegatingTransactionManager(final TransactionManager delegate) {
    super();
    this.delegate = delegate;
  }

  /**
   * Creates a new transaction and associates it with the current thread.
   *
   * @exception NotSupportedException if the thread is already
   * associated with a transaction and this {@link TransactionManager}
   * implementation does not support nested transactions
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   */
  @Override
  public void begin() throws NotSupportedException, SystemException {
    if (this.delegate == null) {
      throw new SystemException("delegate == null");
    }
    this.delegate.begin();
  }

  /**
   * Completes the transaction associated with the current thread.
   *
   * <p>When this method completes, the thread is no longer associated
   * with a transaction.</p>
   *
   * @exception RollbackException if the transaction has been rolled
   * back rather than committed
   *
   * @exception HeuristicMixedException if a heuristic decision was
   * made and that some relevant updates have been committed while
   * others have been rolled back
   *
   * @exception HeuristicRollbackException if a heuristic decision was
   * made and all relevant updates have been rolled back
   *
   * @exception SecurityException if the thread is not allowed to
   * commit the transaction
   *
   * @exception IllegalStateException if the current thread is not
   * associated with a transaction
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   */
  @Override
  public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
    if (this.delegate == null) {
      throw new SystemException("delegate == null");
    }
    this.delegate.commit();
  }

  /**
   * Returns the status of the transaction associated with the current
   * thread.
   *
   * @return the transaction status expressed as the value of one of
   * the {@code int} constants in the {@link Status} class; if no
   * transaction is associated with the current thread, this method
   * returns {@link Status#STATUS_NO_TRANSACTION}
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   *
   * @see Status
   */
  @Override
  public int getStatus() throws SystemException {
    if (this.delegate == null) {
      throw new SystemException("delegate == null");
    }
    return this.delegate.getStatus();
  }

  /**
   * Returns the {@link Transaction} object that represents the
   * transaction context of the calling thread.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link Transaction} object representing the
   * transaction associated with the calling thread; never {@code
   * null}
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   */
  @Override
  public Transaction getTransaction() throws SystemException {
    if (this.delegate == null) {
      throw new SystemException("delegate == null");
    }
    return this.delegate.getTransaction();
  }

  /**
   * Resumes the transaction context association of the calling thread
   * with the transaction represented by the supplied {@link
   * Transaction} object.
   *
   * <p>When this method returns, the calling thread is associated
   * with the transaction context specified.</p>
   *
   * @param transaction the {@link Transaction} representing the
   * transaction to be resumed; must not be {@code null}
   *
   * @exception InvalidTransactionException if {@code transaction} is
   * invalid
   *
   * @exception IllegalStateException if the thread is already
   * associated with another transaction
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   */
  @Override
  public void resume(final Transaction transaction) throws InvalidTransactionException, SystemException {
    if (this.delegate == null) {
      throw new SystemException("delegate == null");
    }
    this.delegate.resume(transaction);
  }

  /**
   * Rolls back the transaction associated with the current thread.
   *
   * <p>When this method completes, the thread is no longer associated
   * with a transaction.</p>
   *
   * @exception SecurityException if the thread is not allowed to roll
   * back the transaction
   *
   * @exception IllegalStateException if the current thread is not
   * associated with a transaction
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   */
  @Override
  public void rollback() throws SystemException {
    if (this.delegate == null) {
      throw new SystemException("delegate == null");
    }
    this.delegate.rollback();
  }

  /**
   * Irrevocably modifies the transaction associated with the current
   * thread such that the only possible outcome is for it to
   * {@linkplain #rollback() roll back}.
   *
   * @exception IllegalStateException if the current thread is not
   * associated with a transaction
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   */
  @Override
  public void setRollbackOnly() throws SystemException {
    if (this.delegate == null) {
      throw new SystemException("delegate == null");
    }
    this.delegate.setRollbackOnly();
  }

  /**
   * Sets the timeout value that is associated with transactions
   * started by the current thread with the {@link #begin()} method.
   *
   * <p>If an application has not called this method, the transaction
   * service uses some default value for the transaction timeout.</p>
   *
   * @param seconds the timeout in seconds; if the value is zero, the
   * transaction service restores the default value; if the value is
   * negative a {@link SystemException} is thrown
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition or if {@code seconds} is
   * less than zero
   */
  @Override
  public void setTransactionTimeout(final int seconds) throws SystemException {
    if (this.delegate == null) {
      throw new SystemException("delegate == null");
    }
    this.delegate.setTransactionTimeout(seconds);
  }

  /**
   * Suspends the transaction currently associated with the calling
   * thread and returns a {@link Transaction} that represents the
   * transaction context being suspended, or {@code null} if the
   * calling thread is not associated with a transaction.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>When this method returns, the calling thread is no longer
   * associated with a transaction.</p>
   *
   * @return a {@link Transaction} representing the suspended
   * transaction, or {@code null}
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   */
  @Override
  public Transaction suspend() throws SystemException {
    if (this.delegate == null) {
      throw new SystemException("delegate == null");
    }
    return this.delegate.suspend();
  }

}
