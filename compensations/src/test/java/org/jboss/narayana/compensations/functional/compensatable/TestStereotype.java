/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.compensations.functional.compensatable;

import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationTransactionType;

import jakarta.enterprise.inject.Stereotype;
import jakarta.inject.Named;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Named
@Compensatable(CompensationTransactionType.REQUIRED)
@Stereotype
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestStereotype {

}