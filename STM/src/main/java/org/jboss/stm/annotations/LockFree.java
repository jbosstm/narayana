/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * No locks will be obtained on this method, though
 * any transaction context will still be on the thread.
 * 
 * @author marklittle
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Inherited
public @interface LockFree
{
}