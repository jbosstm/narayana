/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * Defines that the container will create a new transaction
 * for each method invocation, regardless of whether there is
 * already a transaction associated with the caller. These transactions
 * will always be top-level transactions even if there is a transaction
 * already associated with the invoking thread.
 * 
 * @author marklittle
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface NestedTopLevel
{
}