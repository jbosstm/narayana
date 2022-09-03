/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013-2018 Red Hat, Inc., and individual contributors
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

package com.arjuna.ats.jta.cdi.transactional;


import com.arjuna.ats.jta.cdi.SneakyThrow;
import com.arjuna.ats.jta.cdi.TransactionExtension;
import com.arjuna.ats.jta.cdi.async.ContextPropagationAsyncHandler;
import com.arjuna.ats.jta.cdi.RunnableWithException;
import com.arjuna.ats.jta.cdi.TransactionHandler;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;
import org.jboss.tm.usertx.UserTransactionOperationsProvider;

import jakarta.enterprise.inject.Intercepted;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.security.AccessController.doPrivileged;

/**
 * @author paul.robinson@redhat.com 02/05/2013
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public abstract class TransactionalInterceptorBase implements Serializable {
    private static final long serialVersionUID = 1L;

    // to distinguish Weld implementation where WeldInvocationContext defines context data key
    private static final String WELD_INTERCEPTOR_BINDINGS_KEY = "org.jboss.weld.interceptor.bindings";

    @Inject
    transient jakarta.enterprise.inject.spi.BeanManager beanManager;

    @Inject
    private TransactionExtension extension;

    @Inject
    @Intercepted
    private Bean<?> interceptedBean;

    @Inject
    private TransactionManager transactionManager;

    private final boolean userTransactionAvailable;

    protected TransactionalInterceptorBase(boolean userTransactionAvailable) {
        this.userTransactionAvailable = userTransactionAvailable;
    }

    public Object intercept(InvocationContext ic) throws Exception {

        final Transaction tx = transactionManager.getTransaction();

        boolean previousUserTransactionAvailability = setUserTransactionAvailable(userTransactionAvailable);
        try {
            return doIntercept(transactionManager, tx, ic);
        } finally {
            resetUserTransactionAvailability(previousUserTransactionAvailability);
        }
    }

    protected abstract Object doIntercept(TransactionManager tm, Transaction tx, InvocationContext ic) throws Exception;

    /**
     * <p>
     * Looking for the {@link Transactional} annotation first on the method, second on the class.
     * <p>
     * Method handles CDI types to cover cases where extensions are used.
     * In case of EE container uses reflection.
     *
     * @param ic  invocation context of the interceptor
     * @return instance of {@link Transactional} annotation or null
     */
    private Transactional getTransactional(InvocationContext ic) {
        // when the CDI implementation is Weld then using the Weld API for accessing the annotated type
        // when Weld does not provide the annotation we switch to more complicated processing
        if (interceptedBean != null && ic.getContextData().get(WELD_INTERCEPTOR_BINDINGS_KEY) != null) {
            Set<Annotation> annotationBindings = (Set<Annotation>) ic.getContextData().get(WELD_INTERCEPTOR_BINDINGS_KEY);
            for (Annotation annotation : annotationBindings) {
                if (annotation.annotationType() == Transactional.class) {
                    return (Transactional) annotation;
                }
            }
        }
        if (interceptedBean != null) { // not-null for CDI
            // getting annotated type and method corresponding of the intercepted bean and method
            AnnotatedType<?> currentAnnotatedType = extension.getBeanToAnnotatedTypeMapping().get(interceptedBean);
            if (currentAnnotatedType == null) {
                throw new IllegalStateException(jtaLogger.i18NLogger.get_not_supported_non_weld_interception(interceptedBean.getName()));
            }
            AnnotatedMethod<?> currentAnnotatedMethod = null;
            for (AnnotatedMethod<?> methodInSearch: currentAnnotatedType.getMethods()) {
                if (methodInSearch.getJavaMember().equals(ic.getMethod())) {
                    currentAnnotatedMethod = methodInSearch;
                    break;
                }
            }

            // check existence of the stereotype on method
            assert currentAnnotatedMethod != null;
            Transactional transactionalMethod = getTransactionalAnnotationRecursive(currentAnnotatedMethod.getAnnotations());
            if (transactionalMethod != null) return transactionalMethod;
            // stereotype recursive search, covering ones added by an extension too
            Transactional transactionalExtension = getTransactionalAnnotationRecursive(currentAnnotatedType.getAnnotations());
            if (transactionalExtension != null) return transactionalExtension;
            // stereotypes already merged to one chunk by BeanAttributes.getStereotypes()
            for (Class<? extends Annotation> stereotype: interceptedBean.getStereotypes()) {
                Transactional transactionalAnn = stereotype.getAnnotation(Transactional.class);
                if (transactionalAnn != null) return transactionalAnn;
            }
        } else { // null for EE components
            Transactional transactional = ic.getMethod().getAnnotation(Transactional.class);
            if (transactional != null) {
                return transactional;
            }

            Class<?> targetClass = ic.getTarget().getClass();
            transactional = targetClass.getAnnotation(Transactional.class);
            if (transactional != null) {
                return transactional;
            }
        }

        throw new RuntimeException(jtaLogger.i18NLogger.get_expected_transactional_annotation());
    }

    private Transactional getTransactionalAnnotationRecursive(Annotation... annotationsOnMember) {
        if (annotationsOnMember == null) return null;
        Set<Class<? extends Annotation>> stereotypeAnnotations = new HashSet<>();

        for (Annotation annotation: annotationsOnMember) {
            if (annotation.annotationType().equals(Transactional.class)) {
                return (Transactional) annotation;
            }
            if (beanManager.isStereotype(annotation.annotationType())) {
                stereotypeAnnotations.add(annotation.annotationType());
            }
        }
        for (Class<? extends Annotation> stereotypeAnnotation: stereotypeAnnotations) {
            return getTransactionalAnnotationRecursive(beanManager.getStereotypeDefinition(stereotypeAnnotation));
        }
        return null;
    }

    private Transactional getTransactionalAnnotationRecursive(Set<Annotation> annotationsOnMember) {
        return getTransactionalAnnotationRecursive(
            annotationsOnMember.toArray(new Annotation[0]));
    }

    protected Object invokeInOurTx(InvocationContext ic, TransactionManager tm) throws Exception {
        return invokeInOurTx(ic, tm, () -> {});
    }

    protected Object invokeInOurTx(InvocationContext ic, TransactionManager tm, RunnableWithException afterEndTransaction) throws Exception {

        tm.begin();
        Transaction tx = tm.getTransaction();

        boolean throwing = false;
        Object ret = null;

        try {
            ret = ic.proceed();
        } catch (Throwable e) {
            throwing = true;
            handleException(ic, e, tx);
        } finally {
            AtomicReference<Object> retRef = new AtomicReference<>(ret);
            boolean asyncReturnType =
                    ContextPropagationAsyncHandler.tryHandleAsynchronously(
                            tm, tx, getTransactional(ic), retRef, ic.getMethod().getReturnType(), afterEndTransaction);
            if (throwing || ret == null || !asyncReturnType) {
                // is throwing (OR) is null (OR) is not asynchronous type (OR) no async handler classes on classpath : handle synchronously
                TransactionHandler.endTransaction(tm, tx, afterEndTransaction);
            }
            if (asyncReturnType) {
                ret = retRef.get();
            }
        }
        return ret;
    }

    protected Object invokeInCallerTx(InvocationContext ic, Transaction tx) throws Exception {

        try {
            return ic.proceed();
        } catch (Throwable t) {
            handleException(ic, t, tx);
        }
        throw new RuntimeException("UNREACHABLE");
    }

    protected Object invokeInNoTx(InvocationContext ic) throws Exception {

        return ic.proceed();
    }

    /**
     * The handleException considers the transaction to be marked for rollback only in case the thrown exception
     * comes with this effect (see {@link TransactionHandler#handleExceptionNoThrow(Transactional, Throwable, Transaction)}
     * and consider the {@link Transactional#dontRollbackOn()}.
     * If so then this method rethrows the {@link Throwable} passed as the parameter 't'.
     */
    protected void handleException(InvocationContext ic, Throwable t, Transaction tx) throws Exception {
        TransactionHandler.handleExceptionNoThrow(getTransactional(ic), t, tx);
        SneakyThrow.sneakyThrow(t);
    }

    protected boolean setUserTransactionAvailable(boolean available) {

        UserTransactionOperationsProvider userTransactionProvider =
            jtaPropertyManager.getJTAEnvironmentBean().getUserTransactionOperationsProvider();
        boolean previousUserTransactionAvailability = userTransactionProvider.getAvailability();

        setAvailability(userTransactionProvider, available);

        return previousUserTransactionAvailability;
    }

    protected void resetUserTransactionAvailability(boolean previousUserTransactionAvailability) {
        UserTransactionOperationsProvider userTransactionProvider =
            jtaPropertyManager.getJTAEnvironmentBean().getUserTransactionOperationsProvider();
        setAvailability(userTransactionProvider, previousUserTransactionAvailability);
    }

    private void setAvailability(UserTransactionOperationsProvider userTransactionProvider, boolean available) {
        if (System.getSecurityManager() == null) {
            userTransactionProvider.setAvailability(available);
        } else {
            doPrivileged((PrivilegedAction<Object>) () -> {
                userTransactionProvider.setAvailability(available);
                return null;
            });
        }
    }
}
