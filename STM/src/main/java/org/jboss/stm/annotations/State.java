/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * State that will be written to the log (or restored).
 * 
 * By default, all member variables (non-static, non-volatile) will be
 * saved.
 * 
 * @author marklittle
 */
        
/*
 * This annotation is not needed since only things annotated with the NotState annotation
 * are ignored and everything else will be saved. Maybe we need a class-level annotation
 * that basically says "all member state must have an annotation to indicate State or NotState"?
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Inherited
public @interface State
{
}