/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.recovery.arjunacore;

/**
 * Some object used in recovery, such as Xids or XAResources, are more useful if they carry naming
 * information around with them. This is conceptually similar to XAResourceWrapper.
 */
interface NameScopedElement {

    /**
     * The name of source (usually a DataSource) of the associated element, or null for unknown/anonymous.
     */
    String getJndiName();

    /**
     * Is the jndiName both non-null and the same as the other's jndiName?
     * @param other
     * @return
     */
    boolean isSameName(NameScopedElement other);

    /**
     * Is the jndiName null?
     * @return
     */
    boolean isAnonymous();
}