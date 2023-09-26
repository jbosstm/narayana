/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.cdi.transactional.stereotype;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Stereotype;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@Stereotype
@Inherited
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Transactional(value = TxType.REQUIRED)
public @interface TransactionalRequiredStereotype {

}