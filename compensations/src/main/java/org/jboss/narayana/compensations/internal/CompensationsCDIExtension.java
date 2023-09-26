/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.narayana.compensations.api.CompensatableAction;


/**
 * @author paul.robinson@redhat.com, 2012-02-13
 */
public class CompensationsCDIExtension implements Extension {

    /**
     * Register all admin CDI beans.
     *
     * @param bbd the bbd event
     * @param bm  the bean manager
     */
    public void register(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {

        //Current API
        addAnnotatedType(bbd, bm, CompensationManagerImpl.class);
        addAnnotatedType(bbd, bm, CompensationInterceptorMandatory.class);
        addAnnotatedType(bbd, bm, CompensationInterceptorNever.class);
        addAnnotatedType(bbd, bm, CompensationInterceptorNotSupported.class);
        addAnnotatedType(bbd, bm, CompensationInterceptorRequired.class);
        addAnnotatedType(bbd, bm, CompensationInterceptorRequiresNew.class);
        addAnnotatedType(bbd, bm, CompensationInterceptorSupports.class);
        addAnnotatedType(bbd, bm, TxCompensateInterceptor.class);
        addAnnotatedType(bbd, bm, TxConfirmInterceptor.class);
        addAnnotatedType(bbd, bm, TxLoggedInterceptor.class);
        addAnnotatedType(bbd, bm, CancelOnFailureInterceptor.class);
        addAnnotatedType(bbd, bm, CompensatableActionImpl.class);
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {

        event.addContext(new CompensationContext());
    }

    private static void addAnnotatedType(final BeforeBeanDiscovery bbd, final BeanManager bm, final Class<?> type) {
        bbd.addAnnotatedType(bm.createAnnotatedType(type), type.getName() + "-tx-compensations");
    }
}