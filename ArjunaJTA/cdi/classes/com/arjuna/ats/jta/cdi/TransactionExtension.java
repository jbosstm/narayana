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

package com.arjuna.ats.jta.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.inject.Singleton;
import javax.naming.CompositeName;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;
import javax.transaction.TransactionSynchronizationRegistry;

import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorMandatory;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorNever;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorNotSupported;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorRequired;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorRequiresNew;
import com.arjuna.ats.jta.cdi.transactional.TransactionalInterceptorSupports;
import com.arjuna.ats.jta.common.jtaPropertyManager;

/**
 * @author paul.robinson@redhat.com 01/05/2013
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 */
public class TransactionExtension implements Extension {

    public static final String TX_INTERCEPTOR = "-tx-interceptor";

    private Map<Bean<?>, AnnotatedType<?>> beanToAnnotatedTypeMapping = new HashMap<>();

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {        

        boolean maybeAddInitialContextBean = false;
        
        Set<Bean<?>> beans = manager.getBeans(TransactionManager.class);
        if (beans.isEmpty()) {
            event.addBean(new JNDIBean<>(jtaPropertyManager.getJTAEnvironmentBean().getTransactionManagerJNDIContext(), TransactionManager.class));
            maybeAddInitialContextBean = true;
        }

        beans = manager.getBeans(TransactionSynchronizationRegistry.class);
        if (beans.isEmpty()) {
            event.addBean(new JNDIBean<>(jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistryJNDIContext(), TransactionSynchronizationRegistry.class));
            maybeAddInitialContextBean = true;
        }

        if (maybeAddInitialContextBean) {
            beans = manager.getBeans(InitialContext.class);
            if (beans.isEmpty()) {
                event.addBean(new AbstractBean<InitialContext>() {
                        @Override
                        public final Set<Type> getTypes() {
                            return Collections.singleton(InitialContext.class); // deliberately NOT Context.class
                        }

                        @Override
                        public final Class<? extends Annotation> getScope() {
                            return Singleton.class;
                        }

                    @Override
                    protected String getTypeName() {
                        return InitialContext.class.getName();
                    }

                    @Override
                        public final InitialContext create(final CreationalContext<InitialContext> cc) {
                            try {
                                return new InitialContext();
                            } catch (final NamingException namingException) {
                                throw new CreationException(namingException.getMessage(), namingException);
                            }
                        }

                        @Override
                        public final void destroy(final InitialContext context, final CreationalContext<InitialContext> cc) {
                            if (context != null) {
                                try {
                                    context.close();
                                } catch (final NamingException namingException) {
                                    
                                }
                            }
                        }
                    });
            }
        }
        
        event.addContext(new TransactionContext(() -> {
            final Bean<?> tmBean = manager.resolve(manager.getBeans(TransactionManager.class));
            return (TransactionManager)manager.getReference(tmBean, TransactionManager.class, manager.createCreationalContext(tmBean));
        },
        () -> {
            final Bean<?> tsrBean = manager.resolve(manager.getBeans(TransactionSynchronizationRegistry.class));
            return (TransactionSynchronizationRegistry)manager.getReference(tsrBean, TransactionSynchronizationRegistry.class, manager.createCreationalContext(tsrBean));
        }));
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
