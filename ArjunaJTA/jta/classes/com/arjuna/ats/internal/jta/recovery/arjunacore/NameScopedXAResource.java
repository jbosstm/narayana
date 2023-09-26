/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.recovery.arjunacore;

import javax.transaction.xa.XAResource;
import java.util.Objects;

public class NameScopedXAResource implements NameScopedElement {

    private final XAResource xaResource;
    private final String jndiName;

    public NameScopedXAResource(XAResource xaResource, String jndiName) {
        this.xaResource = xaResource;
        this.jndiName = jndiName;
    }

    public XAResource getXaResource() {
        return xaResource;
    }

    @Override
    public String getJndiName() {
        return jndiName;
    }

    @Override
    public boolean isSameName(NameScopedElement other) {
        return jndiName != null && jndiName.equals(other.getJndiName());
    }

    @Override
    public boolean isAnonymous() {
        return jndiName == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NameScopedXAResource that = (NameScopedXAResource) o;
        return xaResource.equals(that.xaResource) &&
                Objects.equals(jndiName, that.jndiName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xaResource, jndiName);
    }

    @Override
    public String toString() {
        return "NameScopedXAResource{" +
                "xaResource=" + xaResource +
                ", jndiName='" + jndiName + '\'' +
                '}';
    }
}