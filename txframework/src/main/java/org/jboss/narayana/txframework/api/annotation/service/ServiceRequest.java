package org.jboss.narayana.txframework.api.annotation.service;

import org.jboss.narayana.txframework.api.configuration.service.Default;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method level annotation used to enable lifecycle handling and configure parameters which control its operation
 */

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ServiceRequest
{
    /**
     * The class which is the target of lifecycle handler callbacks. With POJO execution mode this class is the
     * direct target for handler method invocations. With EJB execution mode it identifies the EJB interface
     * class. With WS execution mode it identifies the client interface for  a JaxWS client.
     */
    public Class lifecycleClass() default Default.class;
}
