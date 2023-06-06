/*
 * SPDX short identifier: Apache-2.0
 */

package org.jboss.jbossts.star.annotation;

import org.jboss.jbossts.star.client.SRAClient;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import jakarta.ws.rs.core.Response;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for controlling the lifecycle of Short Running Actions (SRAs).
 *
 * Newly created SRAs are uniquely identified and the id is referred to as the SRA context. The context
 * is passed around using a JAX-RS request/response header called SRAClient#SRA_HTTP_HEADER ("Short-Running-Action").
 * The implementation (of the SRA specification) is expected to manage this context and the application
 * developer is expected to declaratively control the creation, propagation and destruction of SRAs
 * using the @SRA annotation. When a JAX-RS bean method is invoked in the context of an SRA any JAX-RS
 * client requests that it performs will carry the same header so that the receiving resource knows that
 * it is inside an SRA context (typically achieved using JAX-RS client filters). Similarly if the
 * {@link SRA#enableJTABridge()} attribute is enabled then a JTA transaction context will be associated for the
 * duration of the method call so that any resources used by the method will be enlisted with the
 * SRA and will be committed or rolled back when the SRA finishes.
 *
 * Resource methods can access the context id, if required, by injecting it via the JAX-RS @HeaderParam
 * annotation or via the {@link SRAClient} API. This may be useful, for example, for associating
 * business work with an SRA.
 */
@Inherited
@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SRA {
    enum Type {
        /**
         *  If called outside a SRA context a JAX-RS filter will begin a new SRA for the duration of the
         *  method call and when the call completes another JAX-RS filter will complete the SRA.
         */
        REQUIRED,

        /**
         *  If called outside a SRA context a JAX-RS filter will begin a new SRA for the duration of the
         *  method call and when the call completes another JAX-RS filter will complete the SRA.
         *
         *  If called inside a SRA context a JAX-RS filter will suspend it and begin a new SRA for the
         *  duration of the method call and when the call completes another JAX-RS filter will complete the SRA
         *  and resume the one that was active on entry to the method.
         */
        REQUIRES_NEW,

        /**
         *  If called outside a transaction context, the method call will return with a 412 Precondition Failed
         *  HTTP status code
         *
         *  If called inside a transaction context the bean method execution will then continue under that
         *  context.
         */
        MANDATORY,

        /**
         *  If called outside a SRA context managed bean method execution
         *  must then continue outside a SRA context.
         *
         *  If called inside a SRA context, the managed bean method execution
         *  must then continue inside this SRA context.
         */
        SUPPORTS,

        /**
         *  The bean method is executed without a SRA context. If a context is present on entry then it is
         *  suspended and then resumed after the execution has completed.
         */
        NOT_SUPPORTED,

        /**
         *  If called outside a SRA context, managed bean method execution
         *  must then continue outside a SRA context.
         *
         *  If called inside a SRA context the method is not executed and a 412 Precondition Failed HTTP status
         *  code is returned to the caller.
         */
        NEVER
    }

    /**
     * The Type element of the SRA annotation indicates whether a bean method
     * is to be executed within the context of a SRA.
     */
    Type value() default Type.REQUIRED;

    /**
     * Create a local JTA transaction context such that existing transactional JavaEE code may be called
     * within the scope of the SRA. This enables JTA resources to participate in the SRA and will be committed
     * or rolled back when the SRA finishes.
     *
     * @return whether or not JTA bridging is enabled
     */
    boolean enableJTABridge() default false;

    /**
     * Normally if an SRA is present when a bean method is invoked it will not be ended when the method returns.
     * To override this behaviour use the end element to force its termination
     *
     * @return true if an SRA that was present before method execution will be closed when the bean method finishes.
     */
    boolean end() default true;


    /**
     * The cancelOnFamily element can be set to indicate which families of HTTP response codes will cause
     * the SRA to cancel. By default client errors (4xx codes) and server errors (5xx codes) will result in
     * cancellation of the SRA.
     *
     * @return the {@link Response.Status.Family} families that will cause cancellation of the SRA
     */
    @Nonbinding
    Response.Status.Family[] cancelOnFamily() default {
        Response.Status.Family.CLIENT_ERROR, Response.Status.Family.SERVER_ERROR
    };

    /**
     * The cancelOn element can be set to indicate which  HTTP response codes will cause the SRA to cancel
     *
     * @return the {@link Response.Status} HTTP status codes that will cause cancellation of the SRA
     */
    @Nonbinding
    Response.Status [] cancelOn() default {};
}