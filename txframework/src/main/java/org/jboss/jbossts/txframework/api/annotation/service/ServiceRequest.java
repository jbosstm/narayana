package org.jboss.jbossts.txframework.api.annotation.service;

import org.jboss.jbossts.txframework.api.configuration.service.Default;
import org.jboss.jbossts.txframework.api.configuration.service.RequestType;
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
    /**
     * identifies whether the service request is always read only as far as transactional modifications are
     * concerned or, alternatively, that it may make changes to transactional data. in the latter case the
     * service request method can indicate a read-only outcome by invoking the readOnly method of an injected
     * control.
     */
    public RequestType requestType() default RequestType.MODIFY;
}