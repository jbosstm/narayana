/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.narayana.compensations.internal.interceptors.transaction;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationManager;
import org.jboss.narayana.compensations.internal.BAController;
import org.jboss.narayana.compensations.internal.BAControllerFactory;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CompensationInterceptorBase {

    @Inject
    private CompensationManager compensationManager;

    @Inject
    private BeanManager beanManager;

    private CompensationContextStateManager compensationContextStateManager = CompensationContextStateManager.getInstance();

    /**
     * Request should be invoked in a newly created compensating transaction.
     * 
     * If {@code Compensatable.distributed} is true - remote transaction is created, otherwise local transaction is created.
     * 
     * @param ic
     * @return
     * @throws Exception
     */
    protected Object invokeInOurTx(InvocationContext ic) throws Exception {

        BAController baController;
        Compensatable compensatable = getCompensatable(ic);
        if (compensatable.distributed()) {
            baController = BAControllerFactory.getRemoteInstance();
        } else {
            baController = BAControllerFactory.getLocalInstance();
        }
        baController.beginBusinessActivity();

        Object result = null;
        boolean isException = false;

        try {
            result = ic.proceed();
        } catch (Exception e) {
            isException = true;
            handleException(ic, e, true);
        } finally {
            baController.completeBusinessActivity(isException);
        }

        return result;
    }

    /**
     * Request should be executed in already existing transaction.
     * 
     * If the transaction is local, compensation context will be active already and we will be able to reuse it. However, if the
     * incoming transaction is distributed, we need to active compensation context ourselves and to make sure to close it after
     * the request processing.
     * 
     * @param ic
     * @return
     * @throws Exception
     */
    protected Object invokeInCallerTx(InvocationContext ic) throws Exception {

        boolean activatedContext = false;
        if (!compensationContextStateManager.isActive()) {
            compensationContextStateManager.activate(BAControllerFactory.getInstance().getCurrentTransaction().getId());
            activatedContext = true;
        }

        Object result = null;

        try {
            result = ic.proceed();
        } catch (Exception e) {
            handleException(ic, e, false);
        } finally {
            if (activatedContext) {
                compensationContextStateManager.deactivate();
            }
        }

        return result;
    }

    /**
     * Just continue with the request without any extra operations.
     *
     * @param ic
     * @return
     * @throws Exception
     */
    protected Object invokeInNoTx(InvocationContext ic) throws Exception {

        return ic.proceed();
    }

    /**
     * Based on the settings of the annotation, certain exceptions might cause transaction to fail, while others are allowed.
     *
     * By default {@link RuntimeException} causes transaction to compensate.
     *
     * @param ic invocation context.
     * @param exception exception that was caught.
     * @param started if the transaction was stared by this interceptor.
     * @throws Exception always rethrows the same exception.
     */
    private void handleException(final InvocationContext ic, final Exception exception, final boolean started) throws Exception {

        final Compensatable compensatable = getCompensatable(ic);

        if (isDontCancelOn(compensatable, exception)) {
            throw exception;
        }

        if (isCancelOn(compensatable, exception) || exception instanceof RuntimeException) {
            compensationManager.setCompensateOnly();
        }

        throw exception;
    }

    private boolean isDontCancelOn(final Compensatable compensatable, final Exception exception) {

        for (Class dontCancelOnClass : compensatable.dontCancelOn()) {
            if (dontCancelOnClass.isAssignableFrom(exception.getClass())) {
                return true;
            }
        }

        return false;
    }

    private boolean isCancelOn(final Compensatable compensatable, final Exception exception) {

        for (Class cancelOnClass : compensatable.cancelOn()) {
            if (cancelOnClass.isAssignableFrom(exception.getClass())) {
                return true;
            }
        }

        return false;
    }

    private Compensatable getCompensatable(InvocationContext ic) {

        Compensatable compensatable = ic.getMethod().getAnnotation(Compensatable.class);
        if (compensatable != null) {
            return compensatable;
        }

        Class<?> targetClass = ic.getTarget().getClass();
        compensatable = targetClass.getAnnotation(Compensatable.class);
        if (compensatable != null) {
            return compensatable;
        }

        for (Annotation annotation : ic.getMethod().getDeclaringClass().getAnnotations()) {
            if (beanManager.isStereotype(annotation.annotationType())) {
                for (Annotation stereotyped : beanManager.getStereotypeDefinition(annotation.annotationType())) {
                    if (stereotyped.annotationType().equals(Compensatable.class)) {
                        return (Compensatable) stereotyped;
                    }
                }
            }
        }

        throw new RuntimeException("Expected an @Compensatable annotation at class and/or method level");
    }

}
