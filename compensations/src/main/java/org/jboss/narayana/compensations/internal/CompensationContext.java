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

package org.jboss.narayana.compensations.internal;


import org.jboss.narayana.compensations.api.CompensationScoped;
import org.jboss.narayana.compensations.api.CompensationTransactionRuntimeException;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.PassivationCapable;
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

