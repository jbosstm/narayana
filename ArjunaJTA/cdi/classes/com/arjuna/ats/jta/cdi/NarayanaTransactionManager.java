/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.cdi;

import java.io.ObjectStreamException;
import java.io.Serializable;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;

import jakarta.enterprise.event.Event;

import jakarta.enterprise.inject.CreationException;
import jakarta.enterprise.inject.Instance;

import jakarta.enterprise.inject.spi.CDI;

import jakarta.inject.Inject;

import javax.naming.NamingException;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionScoped;

import com.arjuna.ats.jta.common.JTAEnvironmentBean;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * A {@link DelegatingTransactionManager} in {@linkplain ApplicationScoped application scope}
 * that uses the return value that results from invoking the
 * {@link com.arjuna.ats.jta.TransactionManager#transactionManager()} method
 * as its backing implementation.
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 *
 * @see com.arjuna.ats.jta.TransactionManager#transactionManager()
 */
@ApplicationScoped
class NarayanaTransactionManager extends DelegatingTransactionManager {

  /**
   * The version of this class for {@linkplain Serializable serialization} purposes.
   */
  private static final long serialVersionUID = 596L; // 596 ~= 5.9.9.Final-SNAPSHOT

  /**
   * An {@link Event} that can {@linkplain Event#fire(Object) fire}
   * {@link Transaction}s when the {@linkplain TransactionScoped transaction scope} is initialized.
   *
   * @see #NarayanaTransactionManager(Instance, Event, Event, Event)
   */
  private final Event<Transaction> transactionScopeInitializedBroadcaster;

  /**
   * An {@link Event} that can {@linkplain Event#fire(Object) fire}
   * {@link Object}s when the {@linkplain TransactionScoped transaction scope} is destroyed.
   *
   * @see #NarayanaTransactionManager(Instance, Event, Event, Event)
   */
  private final Event<Object> transactionScopeDestroyedBroadcaster;
  /**
   * An {@link Event} that can {@linkplain Event#fire(Object) fire}
   * {@link Object}s when the {@linkplain TransactionScoped transaction scope} is before to be destroyed.
   *
   * @see #NarayanaTransactionManager(Instance, Event, Event, Event)
   */
  private final Event<Object> transactionScopeBeforeDestroyedBroadcaster;

  /**
   * Creates a new, possibly <strong>non-functional</strong> {@link
   * NarayanaTransactionManager}.
   *
   * <p>This constructor exists only to conform with <a
   * href="http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#unproxyable">section
   * 3.15 of the CDI specification</a>.</p>
   *
   * @deprecated This constructor exists only to conform with <a
   * href="http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#unproxyable">section
   * 3.15 of the CDI specification</a>.  Please use the {@link
   * #NarayanaTransactionManager(Instance, Event, Event, Event)} constructor
   * instead.
   *
   * @see #NarayanaTransactionManager(Instance, Event, Event, Event)
   */
  @Deprecated
  NarayanaTransactionManager() {
    this((Supplier<JTAEnvironmentBean>)null, null, null, null);
  }

  /**
   * Creates a new {@link NarayanaTransactionManager}.
   *
   * @param jtaEnvironmentBeanInstance an {@link Instance} providing
   * access to a {@link JTAEnvironmentBean} used to help with delegate
   * computation; may be {@code null} in which case all method
   * invocations will throw {@link IllegalStateException}s
   *
   * @param transactionScopeInitializedBroadcaster an {@link Event}
   * for broadcasting the {@linkplain Initialized initialization} of
   * the {@linkplain TransactionScoped transaction scope}; may be {@code null}
   *
   * @param transactionScopeDestroyedBroadcaster an {@link Event} for
   * broadcasting the {@linkplain Destroyed destruction} of the
   * {@link TransactionScoped transaction scope}; may be {@code null}
   *
   * @param transactionScopeBeforeDestroyedBroadcaster an {@link Event} for
   * broadcasting the {@linkplain BeforeDestroyed before destruction} of the
   * {@link TransactionScoped transaction scope}; may be {@code null}
   *
   * @see JTAEnvironmentBean#getTransactionManager()
   *
   * @see #begin()
   *
   * @see #commit()
   *
   * @see #rollback()
   *
   * @see TransactionScoped
   */
  @Inject
  NarayanaTransactionManager(final Instance<JTAEnvironmentBean> jtaEnvironmentBeanInstance,
                             @Initialized(TransactionScoped.class)
                             final Event<Transaction> transactionScopeInitializedBroadcaster,
                             @Destroyed(TransactionScoped.class)
                             final Event<Object> transactionScopeDestroyedBroadcaster,
                             @BeforeDestroyed(TransactionScoped.class)
                             final Event<Object> transactionScopeBeforeDestroyedBroadcaster) {
    this(jtaEnvironmentBeanInstance == null ? (Supplier<? extends JTAEnvironmentBean>)null : jtaEnvironmentBeanInstance.isUnsatisfied() ? () -> BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class) : jtaEnvironmentBeanInstance::get,
         transactionScopeInitializedBroadcaster,
         transactionScopeDestroyedBroadcaster,
         transactionScopeBeforeDestroyedBroadcaster);
  }

  /**
   * Creates a new {@link NarayanaTransactionManager}.
   *
   * @param jtaEnvironmentBeanSupplier a {@link Supplier} providing
   * access to a {@link JTAEnvironmentBean} used to help with delegate
   * computation; may be {@code null} in which case all method
   * invocations will throw {@link IllegalStateException}s; may be
   * {@linkplain Instance#isUnsatisfied() unsatisfied} in which case
   * the return value of an invocation of {@link
   * BeanPopulator#getDefaultInstance(Class)} will be supplied instead
   *
   * @param transactionScopeInitializedBroadcaster an {@link Event}
   * for broadcasting the {@linkplain Initialized initialization} of
   * the {@linkplain TransactionScoped transaction scope}; may be
   * {@code null}
   *
   * @param transactionScopeDestroyedBroadcaster an {@link Event} for
   * broadcasting the {@linkplain Destroyed destruction} of the {@link
   * TransactionScoped transaction scope}; may be {@code null}
   */
  private NarayanaTransactionManager(final Supplier<? extends JTAEnvironmentBean> jtaEnvironmentBeanSupplier,
                                     final Event<Transaction> transactionScopeInitializedBroadcaster,
                                     final Event<Object> transactionScopeDestroyedBroadcaster,
                                     final Event<Object> transactionScopeBeforeDestroyedBroadcaster) {
    super(getDelegate(jtaEnvironmentBeanSupplier));
    this.transactionScopeInitializedBroadcaster = transactionScopeInitializedBroadcaster;
    this.transactionScopeDestroyedBroadcaster = transactionScopeDestroyedBroadcaster;
    this.transactionScopeBeforeDestroyedBroadcaster = transactionScopeBeforeDestroyedBroadcaster;
  }

  /**
   * Returns an appropriately initialized {@link
   * NarayanaTransactionManager} when invoked as part of the Java
   * serialization mechanism.
   *
   * @return a non-{@code null} {@link NarayanaTransactionManager}
   *
   * @exception ObjectStreamException if a serialization error occurs
   *
   * @see #NarayanaTransactionManager(Instance, Event, Event, Event)
   *
   * @see <a
   * href="https://docs.oracle.com/javase/8/docs/platform/serialization/spec/input.html#a5903">Section
   * 3.7 of the Java Serialization Specification</a>
   */
  private Object readResolve() throws ObjectStreamException {
    // Note that this readResolve() method must NOT be declared final,
    // though normally this would be permitted, or certain
    // interceptor-based tests fail in Wildfly.
    Supplier<? extends JTAEnvironmentBean> supplier = null;
    try {
      final Instance<JTAEnvironmentBean> instance = CDI.current().select(JTAEnvironmentBean.class);
      if (instance != null && !instance.isUnsatisfied()) {
        supplier = instance::get;
      }
    } catch (final IllegalStateException cdiCurrentFailed) {
    }
    if (supplier == null) {
      supplier = () -> BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class);
    }
    return new NarayanaTransactionManager(supplier,
                                          this.transactionScopeInitializedBroadcaster,
                                          this.transactionScopeDestroyedBroadcaster,
                                          this.transactionScopeBeforeDestroyedBroadcaster);
  }

  /**
   * Returns a {@link TransactionManager} to use as a delegate {@link TransactionManager}
   * for a {@link NarayanaTransactionManager} instance.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param jtaEnvironmentBeanSupplier a {@link Supplier} capable of
   * supplying a {@link JTAEnvironmentBean}; may be {@code null}
   *
   * @return a {@link TransactionManager}, or {@code null}
   *
   * @exception CreationException if an error occurs
   *
   * @see JTASupplier#get(String, Supplier)
   */
  private static TransactionManager getDelegate(final Supplier<? extends JTAEnvironmentBean> jtaEnvironmentBeanSupplier) {
    final TransactionManager returnValue;
    if (jtaEnvironmentBeanSupplier == null) {
      returnValue = null;
    } else {
      final JTAEnvironmentBean jtaEnvironmentBean = jtaEnvironmentBeanSupplier.get();
      if (jtaEnvironmentBean == null) {
        returnValue = null;
      } else {
        TransactionManager temp = null;
        try {
          temp = JTASupplier.get(jtaEnvironmentBean.getTransactionManagerJNDIContext(),
                                 jtaEnvironmentBean::getTransactionManager);
        } catch (final NamingException namingException) {
          throw new CreationException(namingException.getMessage(), namingException);
        } finally {
          returnValue = temp;
        }
      }
    }
    return returnValue;
  }

  /**
   * Overrides {@link DelegatingTransactionManager#begin()} to
   * additionally {@linkplain Event#fire(Object) fire} an {@link Object} representing
   * the {@linkplain Initialized initialization} of the {@linkplain TransactionScoped transaction scope}.
   *
   * @exception NotSupportedException if the thread is already
   * associated with a transaction and this {@link TransactionManager}
   * implementation does not support nested transactions
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   *
   * @see DelegatingTransactionManager#begin()
   *
   * @see Event#fire(Object)
   *
   * @see Initialized
   *
   * @see TransactionScoped
   */
  @Override
  public void begin() throws NotSupportedException, SystemException {
    super.begin();
    if (this.transactionScopeInitializedBroadcaster != null) {
      this.transactionScopeInitializedBroadcaster.fire(this.getTransaction());
    }
  }

  /**
   * Overrides {@link DelegatingTransactionManager#commit()} to
   * additionally {@linkplain Event#fire(Object) fire} an {@link Object} representing
   * the {@linkplain Destroyed destruction} of the {@linkplain TransactionScoped transaction scope}.
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
   *
   * @see DelegatingTransactionManager#commit()
   *
   * @see Event#fire(Object)
   *
   * @see Destroyed
   *
   * @see TransactionScoped
   */
  @Override
  public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SystemException {
    if (this.transactionScopeBeforeDestroyedBroadcaster != null) {
      this.transactionScopeBeforeDestroyedBroadcaster.fire(this.getTransaction());
    }

    try {
      super.commit();
    } finally {
      if (this.transactionScopeDestroyedBroadcaster != null) {
        this.transactionScopeDestroyedBroadcaster.fire(this.toString());
      }
    }
  }

  /**
   * Overrides {@link DelegatingTransactionManager#rollback()} to
   * additionally {@linkplain Event#fire(Object) fire} an {@link Object} representing
   * the {@linkplain Destroyed destruction} of the {@linkplain TransactionScoped transaction scope}.
   *
   * @exception SecurityException if the thread is not allowed to roll
   * back the transaction
   *
   * @exception IllegalStateException if the current thread is not
   * associated with a transaction
   *
   * @exception SystemException if this {@link TransactionManager}
   * encounters an unexpected error condition
   *
   * @see DelegatingTransactionManager#rollback()
   *
   * @see Event#fire(Object)
   *
   * @see Destroyed
   *
   * @see TransactionScoped
   */
  @Override
  public void rollback() throws SystemException {
    if (this.transactionScopeBeforeDestroyedBroadcaster != null) {
      this.transactionScopeBeforeDestroyedBroadcaster.fire(this.getTransaction());
    }

    try {
      super.rollback();
    } finally {
      if (this.transactionScopeDestroyedBroadcaster != null) {
        this.transactionScopeDestroyedBroadcaster.fire(this.toString());
      }
    }
  }

}