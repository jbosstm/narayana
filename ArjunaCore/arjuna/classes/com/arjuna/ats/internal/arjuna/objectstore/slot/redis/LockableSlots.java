/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.redis;

public interface LockableSlots {
    // lock just this store - the lease time is defined by the config (can be unbounded)
    default boolean lock(long secondsToExpire) {
        throw new UnsupportedOperationException();
    }

    default boolean unlock(String resourceKey, String randomValue) {
        throw new UnsupportedOperationException();
    }

    default LockableSlots[] lock(long secondsToExpire, boolean allOrNone, String ... nodeIds) {
        throw new UnsupportedOperationException();
    }

    default boolean renewLock(String resourceKey, String randomValue, int secondsToExpire) {
        throw new UnsupportedOperationException();
    }
}