/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

import jakarta.enterprise.context.NormalScope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate a bean is to be scoped to the current active
 * compensation-based transaction.
 * <p/>
 * Note: There is currently a bug that prevents this scope from working
 * in implementations of:
 * <p/>
 * org.jboss.narayana.compensations.api.CompensationHandler
 * org.jboss.narayana.compensations.api.ConfirmationHandler
 * org.jboss.narayana.compensations.api.TransactionLoggedHandler
 * <p/>
 * See here for more details: https://issues.jboss.org/browse/JBTM-1711
 */

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@NormalScope(passivating = true)
public @interface CompensationScoped {

}