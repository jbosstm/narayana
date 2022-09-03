/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
