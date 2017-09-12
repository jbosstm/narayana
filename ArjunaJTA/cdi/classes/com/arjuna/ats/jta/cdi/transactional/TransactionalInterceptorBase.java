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

package com.arjuna.ats.jta.cdi.transactional;


import static java.security.AccessController.doPrivileged;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;

import org.jboss.tm.usertx.UserTransactionOperationsProvider;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.security.PrivilegedAction;
import com.arjuna.ats.jta.cdi.TransactionExtension;

/**
 * @author paul.robinson@redhat.com 02/05/2013
 */

public abstract class TransactionalInterceptorBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    transient javax.enterprise.inject.spi.BeanManager beanManager;

    @Inject
    private TransactionExtension extension;

    @Inject
    @Intercepted
    private Bean<?> interceptedBean;

    private static TransactionManager transactionManager;

    private final boolean userTransactionAvailable;

    protected TransactionalInterceptorBase(boolean userTransactionAvailable) {
        this.userTransactionAvailable = userTransactionAvailable;
    }

    public Object intercept(InvocationContext ic) throws Exception {

        final TransactionManager tm = getTransactionManager();
        final Transaction tx = tm.getTransaction();

        boolean previousUserTransactionAvailability = setUserTransactionAvailable(userTransactionAvailable);
        try {
            return doIntercept(tm, tx, ic);
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
        if(interceptedBean != null) { // not-null for CDI
            // getting annotated type and method corresponding of the intercepted bean and method
            AnnotatedType<?> currentAnnotatedType = extension.getBeanToAnnotatedTypeMapping().get(interceptedBean);
            AnnotatedMethod<?> currentAnnotatedMethod = null;
            for(AnnotatedMethod<?> methodInSearch: currentAnnotatedType.getMethods()) {
                if(methodInSearch.getJavaMember().equals(ic.getMethod())) {
                    currentAnnotatedMethod = methodInSearch;
                    break;
                }
            }
    
            // check existence of the stereotype on method
            Transactional transactionalMethod = getTransactionalAnnotationRecursive(currentAnnotatedMethod.getAnnotations());
            if(transactionalMethod != null) return transactionalMethod;
            // stereotype recursive search, covering ones added by an extension too
            Transactional transactionalExtension = getTransactionalAnnotationRecursive(currentAnnotatedType.getAnnotations());
            if(transactionalExtension != null) return transactionalExtension;
            // stereotypes already merged to one chunk by BeanAttributes.getStereotypes()
            for(Class<? extends Annotation> stereotype: interceptedBean.getStereotypes()) {
                Transactional transactionalAnn = stereotype.getAnnotation(Transactional.class);
                if(transactionalAnn != null) return transactionalAnn;
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
        if(annotationsOnMember == null) return null;
        Set<Class<? extends Annotation>> stereotypeAnnotations = new HashSet<>();

        for(Annotation annotation: annotationsOnMember) {
            if(annotation.annotationType().equals(Transactional.class)) {
                return (Transactional) annotation;
            }
            if (beanManager.isStereotype(annotation.annotationType())) {
                stereotypeAnnotations.add(annotation.annotationType());
            }
        }
        for(Class<? extends Annotation> stereotypeAnnotation: stereotypeAnnotations) {
            return getTransactionalAnnotationRecursive(beanManager.getStereotypeDefinition(stereotypeAnnotation));
        }
        return null;
    }

    private Transactional getTransactionalAnnotationRecursive(Set<Annotation> annotationsOnMember) {
        return getTransactionalAnnotationRecursive(
            annotationsOnMember.toArray(new Annotation[annotationsOnMember.size()]));
    }

    protected Object invokeInOurTx(InvocationContext ic, TransactionManager tm) throws Exception {

        tm.begin();
        Transaction tx = tm.getTransaction();

        try {
            return ic.proceed();
        } catch (Exception e) {
            handleException(ic, e, tx);
        } finally {
            endTransaction(tm, tx);
        }
        throw new RuntimeException("UNREACHABLE");
    }

    protected Object invokeInCallerTx(InvocationContext ic, Transaction tx) throws Exception {

        try {
            return ic.proceed();
        } catch (Exception e) {
            handleException(ic, e, tx);
        }
        throw new RuntimeException("UNREACHABLE");
    }

    protected Object invokeInNoTx(InvocationContext ic) throws Exception {

        return ic.proceed();
    }

    protected void handleException(InvocationContext ic, Exception e, Transaction tx) throws Exception {

        Transactional transactional = getTransactional(ic);

        for (Class dontRollbackOnClass : transactional.dontRollbackOn()) {
            if (dontRollbackOnClass.isAssignableFrom(e.getClass())) {
                throw e;
            }
        }

        for (Class rollbackOnClass : transactional.rollbackOn()) {
            if (rollbackOnClass.isAssignableFrom(e.getClass())) {
                tx.setRollbackOnly();
                throw e;
            }
        }

        if (e instanceof RuntimeException) {
            tx.setRollbackOnly();
            throw e;
        }

        throw e;
    }

    protected void endTransaction(TransactionManager tm, Transaction tx) throws Exception {

        if (tx != tm.getTransaction()) {
            throw new RuntimeException(jtaLogger.i18NLogger.get_wrong_tx_on_thread());
        }

        if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
            tm.rollback();
        } else {
            tm.commit();
        }
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

    protected TransactionManager getTransactionManager() {

        if (transactionManager == null) {
            try {
                InitialContext initialContext = new InitialContext();
                transactionManager = (TransactionManager) initialContext.lookup(jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerJNDIContext());
            } catch (NamingException e) {
                throw new ContextNotActiveException(jtaLogger.i18NLogger.get_could_not_lookup_tm(), e);
            }
        }
        return transactionManager;
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
