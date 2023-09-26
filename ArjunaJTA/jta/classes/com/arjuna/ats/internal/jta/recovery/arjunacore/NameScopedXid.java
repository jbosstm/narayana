/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.recovery.arjunacore;

import javax.transaction.xa.Xid;
import java.util.Objects;

class NameScopedXid implements NameScopedElement {

    private final Xid xid;
    private final String jndiName;

    NameScopedXid(Xid xid, String jndiName) {
        this.xid = xid;
        this.jndiName = jndiName;
    }

    Xid getXid() {
        return xid;
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
        NameScopedXid scopedXid = (NameScopedXid) o;
        return xid.equals(scopedXid.xid) &&
                Objects.equals(jndiName, scopedXid.jndiName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xid, jndiName);
    }

    @Override
    public String toString() {
        return "NameScopedXid{" +
                "xid=" + xid +
                ", jndiName='" + jndiName + '\'' +
                '}';
    }
}