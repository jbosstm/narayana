/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class  ByteArrayKey implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final byte[] key;
    private final int hashcode;

    public ByteArrayKey(byte[] key) {
        if (key == null) {
            hashcode = 0;
            this.key = null;
        } else {
            hashcode = Arrays.hashCode(key);
            this.key = key.clone();
        }
    }

    public byte[] getKey() {
        return key != null ? key.clone() : null;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ByteArrayKey other)) return false;
        return Arrays.equals(key, other.key);
    }

    @Override
    public String toString() {
        return "ByteArrayKey" + Arrays.toString(key);
    }
}
