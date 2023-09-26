/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * Marks member variables that should not be saved/restored during a transaction.
 * Such variables will therefore retain any state they had regardless of how a
 * transaction terminates. Use with care!
 * 
 * By default, all member variables (non-static, non-volatile) will be
 * saved.
 * 
 * @author marklittle
 */
    
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Inherited
public @interface NotState
{
}