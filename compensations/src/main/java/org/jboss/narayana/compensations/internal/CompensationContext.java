/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;


import org.jboss.narayana.compensations.api.CompensationScoped;
import org.jboss.narayana.compensations.api.CompensationTransactionRuntimeException;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class CompensationContext implements Context {

    private static final Map<Object, Map<String, Object>> beanStorePerTransaction = new HashMap<Object, Map<String, Object>>();

    private static ThreadLocal<Object> txContextToExtend = new ThreadLocal<Object>();

    @Override
    public Class<? extends Annotation> getScope() {

        return CompensationScoped.class;
    }

    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {

        if (!isActive()) {
            throw new ContextNotActiveException();
        }
        if (contextual == null) {
            throw new RuntimeException("contextual is null");
        }

        PassivationCapable bean = (PassivationCapable) contextual;
        Map beans = getBeansForThisTransaction();
        Object resource = beans.get(bean.getId());

        if (resource != null) {
            return (T) resource;
        } else if (creationalContext != null) {
            T t = contextual.create(creationalContext);
            beans.put(bean.getId(), t);
            return t;
        } else {
            return null;
        }
    }

    public <T> T get(Contextual<T> contextual) {

        return get(contextual, null);
    }

    private Map getBeansForThisTransaction() {

        try {

            Object currentTX = txContextToExtend.get();
            if (currentTX == null) {
                currentTX = BAControllerFactory.getInstance().getCurrentTransaction();
            }

            if (beanStorePerTransaction.get(currentTX) == null) {
                beanStorePerTransaction.put(currentTX, new HashMap<String, Object>());
            }
            return beanStorePerTransaction.get(currentTX);

        } catch (Exception e) {
            throw new CompensationTransactionRuntimeException("Error looking up Transaction", e);
        }
    }

    public boolean isActive() {

        if (txContextToExtend.get() != null) {
            return true;
        }

        try {
            Object currentTX = BAControllerFactory.getInstance().getCurrentTransaction();
            return currentTX != null;
        } catch (Exception e) {
            throw new CompensationTransactionRuntimeException("Error looking up Transaction", e);
        }
    }

    public static void setTxContextToExtend(Object currentTX) {

        txContextToExtend.set(currentTX);
    }

    /**
     * Garbage collect the beans. Call when the context is closed and can't be used again.
     *
     * @param currentTX the Transaction Context associated with this context.
     */
    public static void close(Object currentTX) {

        txContextToExtend.set(null);
        beanStorePerTransaction.remove(currentTX);
    }
}