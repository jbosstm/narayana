/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package io.narayana.lra.annotation;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import javax.ws.rs.core.Response;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for controlling the lifecycle of Long Running Actions (LRAs).
 *
 * Newly created LRAs are uniquely identified and the id is referred to as the LRA context. The context is passed around
 * using a JAX-RS request/response header called LRAClient#LRA_HTTP_HEADER ("X-lra"). The implementation (of the LRA
 * specification) is expected to manage this context and the application developer is expected to declaratively control
 * the creation, propagation and destruction of LRAs using the @LRA annotation. When a JAX-RS bean method is invoked in the
 * context of an LRA any JAX-RS client requests that it performs will carry the same header so that the receiving
 * resource knows that it is inside an LRA context (typically achieved using JAX-RS client filters).
 *
 * Resource methods can access the context id, if required, by injecting it via the JAX-RS @HeaderParam annotation.
 * This may be useful, for example, for associating business work with an LRA.
 */
@Inherited
@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface LRA {

    /**
     * The Type element of the LRA annotation indicates whether a bean method
     * is to be executed within a compensatable LRA context.
     */
    Type value() default Type.REQUIRED;

    /**
     * <p>The Type element of the annotation indicates whether a bean method is to be executed within a
     * compensatable transaction (aka LRA) context where the values provide the following behaviors.</p>
     */
    enum Type {
        /**
         *  <p>If called outside a LRA context a JAX-RS filter will begin a new LRA for the duration of the
         *  method call and when the call completes another JAX-RS filter will complete the LRA.</p>
         */
        REQUIRED,

        /**
         *  <p>If called outside a LRA context a JAX-RS filter will begin a new LRA for the duration of the
         *  method call and when the call completes another JAX-RS filter will complete the LRA.</p>
         *
         *  <p>If called inside a LRA context a JAX-RS filter will suspend it and begin a new LRA for the
         *  duration of the method call and when the call completes another JAX-RS filter will complete the LRA and resume
         *  the one that was active on entry to the method.</p>
         */
        REQUIRES_NEW,

        /**
         *  <p>If called outside a transaction context, the method call will return
         *  with a 412 Precondition Failed HTTP status code</p>
         *  <p>If called inside a transaction context the bean method execution will then continue under that context.</p>
         */
        MANDATORY,

        /**
         *  <p>If called outside a LRA context managed bean method execution
         *  must then continue outside a LRA context.</p>
         *  <p>If called inside a LRA context, the managed bean method execution
         *  must then continue inside this LRA context.</p>
         */
        SUPPORTS,

        /**
         *  The bean method is executed without a LRA context. If a context is present on entry then it is
         *  suspended and then resumed after the execution has completed.</p>
         */
        NOT_SUPPORTED,

        /**
         *  <p>If called outside a LRA context, managed bean method execution
         *  must then continue outside a LRA context.</p>
         *  <p>If called inside a LRA context the method is not executed and a <code>412 Precondition Failed</code> HTTP status code
         *  is returned to the caller.</p>
         */
        NEVER
    }

    /**
     * Some annotations (such as REQUIRES_NEW) will start an LRA on entry to a method and
     * end it on exit. For some business activities it is desirable for the action to survive
     * method execution and be completed elsewhere.
     *
     * @return whether or not newly created LRAs will survive after the method has executed.
     */
    boolean delayClose() default false;

    /**
     * Normally if an LRA is present when a bean method is invoked it will not be ended when the method returns.
     * To override this behaviour use the terminal element to force its' termination
     *
     * @return true if an LRA that was present before method execution will be terminated when the bean method finishes.
     */
    boolean terminal() default false;

    /**
     * If true then the annotated class will be checked for compensator annotations and when present the class
     * will be enlisted with any LRA that is associated with the invocation
     * @return whether or not to automatically enlist a compensator
     */
    boolean join() default true;

    /**
     * The cancelOnFamily element can be set to indicate which families of HTTP response codes will cause
     * the LRA to cancel. By default client errors (4xx codes) and server errors (5xx codes) will result in
     * cancellation of the LRA.
     *
     * @return the {@link Response.Status.Family} families that will cause cancellation of the LRA
     */
    @Nonbinding
    Response.Status.Family[] cancelOnFamily() default {
//        Response.Status.Family.CLIENT_ERROR, Response.Status.Family.SERVER_ERROR
    };

    /**
     * The cancelOn element can be set to indicate which  HTTP response codes will cause the LRA to cancel
     *
     * @return the {@link Response.Status} HTTP status codes that will cause cancellation of the LRA
     */
    @Nonbinding
    Response.Status [] cancelOn() default {};
}
