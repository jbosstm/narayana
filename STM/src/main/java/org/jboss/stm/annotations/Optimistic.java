/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * Use optimistic concurrency control. This may mean
 * that a transaction is forced to abort at the end
 * due to conflicting updates made by other users.
 * 
 * @author marklittle
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface Optimistic
{
}