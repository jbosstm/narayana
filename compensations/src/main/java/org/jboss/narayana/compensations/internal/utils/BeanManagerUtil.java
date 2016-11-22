package org.jboss.narayana.compensations.internal.utils;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.BeanManagerLookupService;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

/**
 * An utility class to work with {@link BeanManager}.
 * 
 * This class requires Weld container as a result of https://issues.jboss.org/browse/JBTM-2704.
 */
public class BeanManagerUtil {

    /**
     * Get bean manager of the current CDI container.
     *
     * @return bean manager of the current CDI container.
     */
    public static BeanManager getBeanManager() {
        return CDI.current().getBeanManager();
    }

    /**
     * Create bean instance using bean manager of the current CDI container.
     *
     * @param clazz bean type.
     * @return bean instance of the specified type.
     * @throws IllegalStateException if bean wasn't found.
     */
    public static <T> T createBeanInstance(Class<T> clazz) {
        return createBeanInstance(clazz, getBeanManager());
    }

    /**
     * Create bean instance using the provided bean manager.
     *
     * @param clazz bean type.
     * @param beanManager bean manager to use for bean creation.
     * @return bean instance of the specified type.
     * @throws IllegalStateException if bean wasn't found.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createBeanInstance(Class<T> clazz, BeanManager beanManager) {
        BeanManager classBeanManager = getClassBeanManager(clazz, beanManager);
        Bean<T> bean = (Bean<T>) classBeanManager.resolve(classBeanManager.getBeans(clazz));
        if (bean == null) {
            throw new IllegalStateException("CDI BeanManager cannot find an instance of requested type " + clazz.getName());
        }
        CreationalContext<T> context = classBeanManager.createCreationalContext(bean);
        return (T) classBeanManager.getReference(bean, clazz, context);
    }

    /**
     * Get bean manager for the specified class.
     *
     * This method was introduced as a workaround for the issue which occurs when using EAR deployments (see
     * https://issues.jboss.org/browse/JBTM-2704).
     * 
     * @param clazz bean type.
     * @param beanManager the main bean manager to use during the lookup.
     * @return bean manager of the specified class.
     */
    private static <T> BeanManager getClassBeanManager(Class<T> clazz, BeanManager beanManager) {
        BeanManagerImpl unwrappedBeanManager = BeanManagerProxy.unwrap(beanManager);
        return BeanManagerLookupService.lookupBeanManager(clazz, unwrappedBeanManager);
    }

}
