/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2020,
 * @author JBoss, by Red Hat.
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