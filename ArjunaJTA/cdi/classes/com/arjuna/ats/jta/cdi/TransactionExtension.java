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

package com.arjuna.ats.jta.cdi;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.transaction.TransactionScoped;

import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorMandatory;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorNever;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorNotSupported;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorRequired;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorRequiresNew;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorSupports;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 */
public class TransactionExtension implements Extension {

    public static final String TX_INTERCEPTOR = "-tx-interceptor";

    private Map<Bean<?>, AnnotatedType<?>> beanToAnnotatedTypeMapping = new HashMap<>();

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {

        event.addContext(new TransactionContext());
    }

    public void register(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {

        bbd.addScope(TransactionScoped.class, true, true);

        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorMandatory.class), TransactionalInterceptorMandatory.class.getName() + TX_INTERCEPTOR);
        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorNever.class), TransactionalInterceptorNever.class.getName() + TX_INTERCEPTOR);
        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorNotSupported.class), TransactionalInterceptorNotSupported.class.getName() + TX_INTERCEPTOR);
        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorRequired.class), TransactionalInterceptorRequired.class.getName() + TX_INTERCEPTOR);
        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorRequiresNew.class), TransactionalInterceptorRequiresNew.class.getName() + TX_INTERCEPTOR);
        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorSupports.class), TransactionalInterceptorSupports.class.getName() + TX_INTERCEPTOR);
    }

    /**
     * Gathering information about managed bean to obtain mapping bean to annotated type.
     * This is needed later when handling Stereotypes in TransactionalInterceptorBase.
     */
    public void processManagedBean(@Observes ProcessManagedBean<?> pmb) {
        beanToAnnotatedTypeMapping.put(pmb.getBean(), pmb.getAnnotatedBeanClass());
    }

    public Map<Bean<?>, AnnotatedType<?>> getBeanToAnnotatedTypeMapping() {
        return beanToAnnotatedTypeMapping;
    }
}
