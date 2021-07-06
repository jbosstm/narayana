/*
 * Copyright Red Hat
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.arquillian.deployment;

import org.jboss.shrinkwrap.api.Archive;

/**
 * This interface represents a sort of guide to indicate that extension
 * classes used to generate deployment scenarios MUST implement
 * this interface
 */
public interface Deployment<T extends Archive<T>> {

    // Constructors with parameters SHOULD NOT be declared in classes
    // implementing this interface. Only the default parameterless
    // constructor should be used
    Archive<T> create(String deploymentName);
}