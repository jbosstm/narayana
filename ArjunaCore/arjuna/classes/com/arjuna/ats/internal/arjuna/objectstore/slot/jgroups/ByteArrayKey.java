/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

public class  ByteArrayKey implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final byte[] key;

    public ByteArrayKey(byte[] key) {
        this.key = key != null ? key.clone() : null;
    }

    public byte[] getKey() {
        return key != null ? key.clone() : null;
    }

    @Override
    public int hashCode() {
        // Don't cache - recalculate each time to avoid issues with transient fields
        return key != null ? Arrays.hashCode(key) : 0;
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
