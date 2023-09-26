/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 *  Means that the method is not transactional, so no context on
 *  the thread or locks acquired/released.
 *  
 * @author marklittle
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface TransactionFree
{
}