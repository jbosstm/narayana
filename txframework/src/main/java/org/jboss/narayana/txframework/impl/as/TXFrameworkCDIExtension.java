package org.jboss.narayana.txframework.impl.as;

import org.jboss.narayana.txframework.api.management.TXDataMap;
import org.jboss.narayana.txframework.api.management.WSBATxControl;
import org.jboss.narayana.txframework.impl.ServiceRequestInterceptor;
import org.jboss.narayana.txframework.impl.TXDataMapImpl;
import org.jboss.narayana.txframework.impl.handlers.restat.client.RestTXRequiredInterceptor;
import org.jboss.narayana.txframework.impl.handlers.wsba.WSBATxControlImpl;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;


/**
 * @author paul.robinson@redhat.com, 2012-02-13
 */
public class TXFrameworkCDIExtension implements Extension {

    /**
     * Register all admin CDI beans.
     *
     * @param bbd the bbd event
     * @param bm  the bean manager
     */
    public void register(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {
        final AnnotatedType<ServiceRequestInterceptor> serviceRequestInterceptor = bm.createAnnotatedType(ServiceRequestInterceptor.class);
        bbd.addAnnotatedType(serviceRequestInterceptor);
        
        final AnnotatedType<RestTXRequiredInterceptor> restTXRequiredInterceptor = bm.createAnnotatedType(RestTXRequiredInterceptor.class);
        bbd.addAnnotatedType(restTXRequiredInterceptor);

        final AnnotatedType<TXDataMap> txDataMap = bm.createAnnotatedType(TXDataMap.class);
        bbd.addAnnotatedType(txDataMap);

        final AnnotatedType<TXDataMapImpl> txDataMapImpl = bm.createAnnotatedType(TXDataMapImpl.class);
        bbd.addAnnotatedType(txDataMapImpl);

        final AnnotatedType<WSBATxControl> wsbatxcontrol = bm.createAnnotatedType(WSBATxControl.class);
        bbd.addAnnotatedType(wsbatxcontrol);

        final AnnotatedType<WSBATxControlImpl> wsbaTxControlImpl = bm.createAnnotatedType(WSBATxControlImpl.class);
        bbd.addAnnotatedType(wsbaTxControlImpl);
    }
}
