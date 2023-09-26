/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * Used to define the specific restore_state method for the class. This
 * is used in preference to any @State indications on the class
 * state.
 * 
 * @author marklittle
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface RestoreState
{
}