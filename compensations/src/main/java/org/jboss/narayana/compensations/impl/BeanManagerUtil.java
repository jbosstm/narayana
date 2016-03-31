package org.jboss.narayana.compensations.impl;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.util.Iterator;

public class BeanManagerUtil {


    public static BeanManager getBeanManager() {

        return CDI.current().getBeanManager();
    }

    public static <T> T createBeanInstance(Class<T> clazz) {

        return BeanManagerUtil.createBeanInstance(clazz, BeanManagerUtil.getBeanManager());
    }

    public static <T> T createBeanInstance(Class<T> clazz, BeanManager bm) {

        Iterator<Bean<?>> iter = bm.getBeans(clazz).iterator();
        if (!iter.hasNext()) {
            throw new IllegalStateException("CDI BeanManager cannot find an instance of requested type " + clazz.getName());
        }
        Bean<T> bean = (Bean<T>) iter.next();
        CreationalContext<T> ctx = bm.createCreationalContext(bean);
        return (T) bm.getReference(bean, clazz, ctx);
    }

}
