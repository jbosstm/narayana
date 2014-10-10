package org.jboss.narayana.compensations.impl;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Iterator;

public class BeanManagerUtil {


    public static BeanManager getBeanManager() {

        try {
            return InitialContext.doLookup("java:comp/BeanManager");
        } catch (NamingException e) {
            //Do nothing, and move onto alternative
        }

        try {
            return InitialContext.doLookup("java:comp/env/BeanManager");
        } catch (NamingException e) {
            throw new RuntimeException("BeanManager not available in JNDI");
        }
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