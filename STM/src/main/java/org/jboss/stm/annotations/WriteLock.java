/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * Grab a write lock for this method.
 * 
 * If no other annotation appears to override this, then
 * all transactional object methods will be assumed to modify
 * the state, i.e., WriteLock is the default value.
 * 
 * @author marklittle
 */

// TODO lock priority rules

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Inherited
public @interface WriteLock
{
}