package org.jboss.narayana.compensations.cdi.impl;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 * @author paul.robinson@redhat.com 21/06/2014
 */
public class CompensationsCDIExtension implements Extension {

    /**
     * Register all admin CDI beans.
     *
     * @param bbd the bbd event
     * @param bm  the bean manager
     */
    public void register(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {

        bbd.addAnnotatedType(bm.createAnnotatedType(CompensationManagerImpl.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(CompensationInterceptorMandatory.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(CompensationInterceptorNever.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(CompensationInterceptorNotSupported.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(CompensationInterceptorRequired.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(CompensationInterceptorRequiresNew.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(CompensationInterceptorSupports.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(TxCompensateInterceptor.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(TxConfirmInterceptor.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(TxLoggedInterceptor.class));
        bbd.addAnnotatedType(bm.createAnnotatedType(CancelOnFailureInterceptor.class));
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {

        event.addContext(new CompensationContext());
    }

}
