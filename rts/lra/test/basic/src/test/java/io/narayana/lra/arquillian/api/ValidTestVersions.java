/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.arquillian.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotations used within JUnit rule {@link ValidTestVersionsRule}.
 * <p>
 * The annotation is used to limit the test method execution for particular API version values.
 * <p>
 * The {@link ValidTestVersionsRule} verifies what is content of the {@code version} field in test class
 * and then checks if {@code version} matches to values in the annotation.
 * When it matches the test method is executed, if it does not match then the test method is skipped.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ValidTestVersions {
    String[] value();
}