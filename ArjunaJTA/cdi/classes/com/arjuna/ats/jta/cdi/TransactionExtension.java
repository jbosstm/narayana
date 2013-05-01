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

import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorMandatory;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorNever;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorNotSupported;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorRequired;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorRequiresNew;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorSupports;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.transaction.TransactionScoped;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 */
public class TransactionExtension implements Extension {

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {

        event.addContext(new TransactionContext());
    }

    public void register(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {

        bbd.addScope(TransactionScoped.class, true, true);

        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorMandatory.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorNever.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorNotSupported.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorRequired.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorRequiresNew.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(TransactionalInterceptorSupports.class));
    }
}