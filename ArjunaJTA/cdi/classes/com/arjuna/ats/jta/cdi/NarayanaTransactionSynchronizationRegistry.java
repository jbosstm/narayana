/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.cdi;

import java.io.ObjectStreamException;
import java.io.Serializable;

import java.util.function.Supplier; // for javadoc only

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.enterprise.inject.CreationException;
import jakarta.enterprise.inject.Instance;

import jakarta.enterprise.inject.spi.CDI;

import jakarta.inject.Inject;

import javax.naming.NamingException;

import jakarta.transaction.TransactionSynchronizationRegistry;

import com.arjuna.ats.jta.common.JTAEnvironmentBean;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * A {@link DelegatingTransactionSynchronizationRegistry} in
 * {@linkplain ApplicationScoped application scope} that uses the
 * return value that results from invoking the {@link
 * JTAEnvironmentBean#getTransactionSynchronizationRegistry()} method
 * as its backing implementation.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see JTAEnvironmentBean#getTransactionSynchronizationRegistry()
 */
@ApplicationScoped
class NarayanaTransactionSynchronizationRegistry extends DelegatingTransactionSynchronizationRegistry {

  /**
   * The version of this class for {@linkplain Serializable
   * serialization} purposes.
   */
  private static final long serialVersionUID = 596L; // 596 ~= 5.9.9.Final-SNAPSHOT

  /**
   * Creates a new, <strong>nonfunctional</strong> {@link
   * NarayanaTransactionSynchronizationRegistry}.
   *
   * <p>This constructor exists only to conform with section 3.15 of
   * the CDI specification.</p>
   *
   * @deprecated This constructor exists only to conform with
   * section 3.15 of the CDI specification; please use the {@link
   * #NarayanaTransactionSynchronizationRegistry(Instance)}
   * constructor instead.
   *
   * @see
   * #NarayanaTransactionSynchronizationRegistry(Instance)
   *
   * @see <a
   * href="http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#unproxyable">Section
   * 3.15 of the CDI 2.0 specification</a>
   */
  @Deprecated
  NarayanaTransactionSynchronizationRegistry() {
    this((Supplier<? extends JTAEnvironmentBean>)null);
  }

  /**
   * Creates a new {@link
   * NarayanaTransactionSynchronizationRegistry}.
   *
   * @param jtaEnvironmentBeanInstance an {@link Instance} providing
   * access to a {@link JTAEnvironmentBean} describing the environment
   * in which transaction processing will take place; may be {@code
   * null} in which case all invocations of all methods will throw
   * {@link IllegalStateException}s; may be {@linkplain
   * Instance#isUnsatisfied() unsatisfied} in which case the return
   * value of an invocation of {@link
   * BeanPopulator#getDefaultInstance(Class)} will be supplied instead
   */
  @Inject
  NarayanaTransactionSynchronizationRegistry(final Instance<JTAEnvironmentBean> jtaEnvironmentBeanInstance) {
    this(jtaEnvironmentBeanInstance == null ? (Supplier<? extends JTAEnvironmentBean>)null : jtaEnvironmentBeanInstance.isUnsatisfied() ? () -> BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class) : jtaEnvironmentBeanInstance::get);
  }

  /**
   * Creates a new {@link NarayanaTransactionSynchronizationRegistry}.
   *
   * @param jtaEnvironmentBeanSupplier a {@link Supplier} providing
   * access to a {@link JTAEnvironmentBean} describing the environment
   * in which transaction processing will take place; may be {@code
   * null} in which case all invocations of all methods will throw
   * {@link IllegalStateException}s
   */
  private NarayanaTransactionSynchronizationRegistry(final Supplier<? extends JTAEnvironmentBean> jtaEnvironmentBeanSupplier) {
    super(getDelegate(jtaEnvironmentBeanSupplier));
  }

  /**
   * Returns an appropriately initialized {@link
   * NarayanaTransactionSynchronizationRegistry} when invoked as part
   * of the Java serialization mechanism.
   *
   * @return a non-{@code null} {@link
   * NarayanaTransactionSynchronizationRegistry}
   *
   * @exception ObjectStreamException if a serialization error occurs
   *
   * @see #NarayanaTransactionSynchronizationRegistry(Instance)
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
    return new NarayanaTransactionSynchronizationRegistry(supplier);
  }

  /**
   * Returns a {@link TransactionSynchronizationRegistry} to use as a
   * delegate {@link TransactionSynchronizationRegistry} for a {@link
   * NarayanaTransactionSynchronizationRegistry} instance.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param jtaEnvironmentBeanSupplier a {@link Supplier} capable of
   * supplying a {@link JTAEnvironmentBean}; may be {@code null}
   *
   * @return a {@link TransactionSynchronizationRegistry}, or {@code
   * null}
   *
   * @exception CreationException if an error occurs
   *
   * @see JTASupplier#get(String, Supplier)
   */
  private static final TransactionSynchronizationRegistry getDelegate(final Supplier<? extends JTAEnvironmentBean> jtaEnvironmentBeanSupplier) {
    final TransactionSynchronizationRegistry returnValue;
    if (jtaEnvironmentBeanSupplier == null) {
      returnValue = null;
    } else {
      final JTAEnvironmentBean jtaEnvironmentBean = jtaEnvironmentBeanSupplier.get();
      if (jtaEnvironmentBean == null) {
        returnValue = null;
      } else {
        TransactionSynchronizationRegistry temp = null;
        try {
          temp = JTASupplier.get(jtaEnvironmentBean.getTransactionSynchronizationRegistryJNDIContext(),
                                 jtaEnvironmentBean::getTransactionSynchronizationRegistry);
        } catch (final NamingException namingException) {
          throw new CreationException(namingException.getMessage(), namingException);
        } finally {
          returnValue = temp;
        }
      }
    }
    return returnValue;
  }

}