package org.jboss.narayana.txframework.impl;

import java.lang.reflect.Method;

/**
 * @author paul.robinson@redhat.com, 2012-11-07
 */
public class ServiceInvocationMeta {

    private Object proxyInstance;
    private Class serviceClass;
    private Method serviceMethod;

    public ServiceInvocationMeta(Object proxyInstance, Class serviceClass, Method serviceMethod) {
        this.proxyInstance = proxyInstance;
        this.serviceClass = serviceClass;
        this.serviceMethod = serviceMethod;
    }

    public Object getProxyInstance() {
        return proxyInstance;
    }

    public Class getServiceClass() {
        return serviceClass;
    }

    public Method getServiceMethod() {
        return serviceMethod;
    }
}
