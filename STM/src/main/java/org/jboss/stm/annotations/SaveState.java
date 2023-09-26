/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.stm.annotations;

import java.lang.annotation.*;

/**
 * Used to define the specific save_state method for the class. This
 * is used in preference to any @State indications on the class
 * state. This is the case no matter where in the class hierarchy it
 * occurs. So if you have a base class that uses save/restore methods the
 * inherited classes must have them too if their state is to be durable.
 * In future we may save/restore specifically for each class in the
 * inheritance hierarchy.
 * 
 * TODO save/restore specifically for each class in the inheritance hierarchy.
 * 
 * @author marklittle
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface SaveState
{
}