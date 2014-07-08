package org.jboss.narayana.compensations.cdi.impl;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Iterator;

public class BeanManagerUtil {


    private static BeanManager instance;

    public static BeanManager getBeanManager() {

        if (instance != null) {
            return instance;
        }

        try {
            return InitialContext.doLookup("java:comp/BeanManager");
        } catch (NamingException e) {
            //Do nothing, and move onto alternative
        }

        try {
            return InitialContext.doLookup("java:comp/env/BeanManager");
        } catch (NamingException e) {
            //Do nothing, and move onto alternative
        }
        try {
            return CDI.current().getBeanManager();
        } catch (Exception e) {
            //Do nothing, and move onto alternative
        }
        throw new RuntimeException("Cannot obtain BeanManager - out of alternatives to try");
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

    public static void setInstance(BeanManager beanManager) {
        instance = beanManager;
    }

}