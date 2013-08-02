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

package org.jboss.narayana.compensations.impl;


import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.wst.SystemException;
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

    private static final Map<TxContext, Map<String, Object>> beanStorePerTransaction = new HashMap<TxContext, Map<String, Object>>();

    private static ThreadLocal<TxContext> txContextToExtend = new ThreadLocal<TxContext>();

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

            TxContext currentTX = txContextToExtend.get();
            if (currentTX == null) {
                BusinessActivityManager bam = BusinessActivityManagerFactory.businessActivityManager();
                currentTX = bam.currentTransaction();
            }

            if (beanStorePerTransaction.get(currentTX) == null) {
                beanStorePerTransaction.put(currentTX, new HashMap<String, Object>());
            }
            return beanStorePerTransaction.get(currentTX);

        } catch (SystemException e) {
            throw new CompensationTransactionRuntimeException("Error looking up Transaction", e);
        }
    }

    public boolean isActive() {

        if (txContextToExtend.get() != null) {
            return true;
        }

        try {
            BusinessActivityManager bam = BusinessActivityManagerFactory.businessActivityManager();
            TxContext currentTX = bam.currentTransaction();
            return currentTX != null;
        } catch (SystemException e) {
            throw new CompensationTransactionRuntimeException("Error looking up Transaction", e);
        }
    }

    public static void setTxContextToExtend(TxContext currentTX) {

        txContextToExtend.set(currentTX);
    }

    /**
     * Garbage collect the beans. Call when the context is closed and can't be used again.
     *
     * @param currentTX the Transaction Context associated with this context.
     */
    public static void close(TxContext currentTX) {
        beanStorePerTransaction.remove(currentTX);
    }
}

