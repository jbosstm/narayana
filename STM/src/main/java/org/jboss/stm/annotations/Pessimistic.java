/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * Specifies that pessimistic concurrency control
 * should be used. This means that a read or write operation
 * may block or be rejected if another user is manipulating
 * the same object in a conflicting manner.
 * 
 * If no other annotation appears to override this, then
 * pessimistic is the default for a transactional object.
 * 
 * @author marklittle
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface Pessimistic
{
}