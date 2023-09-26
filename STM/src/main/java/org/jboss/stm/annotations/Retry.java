/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * If pessimistic concurrency control is being used then a conflict will
 * immediately cause the operation to fail and the application can do something
 * else. If instead the developer wants the system to retry getting the lock
 * before returning, then this annotation defines the number of retry attempts.
 * 
 * @author marklittle
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Retry
{
    int count ();
}