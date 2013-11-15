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


import com.arjuna.ats.jta.logging.jtaLogger;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * @author paul.robinson@redhat.com 02/05/2013
 */

public class TransactionalInterceptorBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    transient javax.enterprise.inject.spi.BeanManager beanManager;

    private boolean previousUserTransactionAvailability;

    private static TransactionManager transactionManager;

    private Transactional getTransactional(InvocationContext ic) {

        Transactional transactional = ic.getMethod().getAnnotation(Transactional.class);
        if (transactional != null) {
            return transactional;
        }

        Class<?> targetClass = ic.getTarget().getClass();
        transactional = targetClass.getAnnotation(Transactional.class);
        if (transactional != null) {
            return transactional;
        }

        // see if the target is a stereotype
        for (Annotation annotation : ic.getMethod().getDeclaringClass().getAnnotations()) {
            if (beanManager.isStereotype(annotation.annotationType())) {
                for (Annotation stereotyped : beanManager.getStereotypeDefinition(annotation.annotationType())) {
                    if (stereotyped.annotationType().equals(Transactional.class)) {
                        return (Transactional) stereotyped;
                    }
                }
            }
        }

        throw new RuntimeException(jtaLogger.i18NLogger.get_expected_transactional_annotation());
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

    protected void setUserTransactionAvailable(boolean available) {

        previousUserTransactionAvailability = ServerVMClientUserTransaction.isAvailable();
        ServerVMClientUserTransaction.setAvailability(available);
    }

    protected void resetUserTransactionAvailability() {

        ServerVMClientUserTransaction.setAvailability(previousUserTransactionAvailability);
    }

    protected TransactionManager getTransactionManager() {

        if (transactionManager == null) {
            try {
                InitialContext initialContext = new InitialContext();
                transactionManager = (TransactionManager) initialContext.lookup("java:jboss/TransactionManager");
            } catch (NamingException e) {
                throw new ContextNotActiveException(jtaLogger.i18NLogger.get_could_not_lookup_tm(), e);
            }
        }
        return transactionManager;
    }
}
