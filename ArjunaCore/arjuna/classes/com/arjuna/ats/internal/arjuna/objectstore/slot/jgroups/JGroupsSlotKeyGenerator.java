/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.jgroups;

/**
 * A strategy for generating unique keys for slot entries {@see SlotStoreEnvironmentBean}
 */
public interface JGroupsSlotKeyGenerator {
    // generate a unique slot key for the given index (which is different from SlotStoreKey)
    ByteArrayKey generateUniqueKey(int index);
    // initialise the key generator with the JGroupsStoreEnvironment config
    void init(JGroupsStoreEnvironmentBean config);
}
