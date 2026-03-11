package com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan;

/**
 * A strategy for generating unique keys for slot entries {@see SlotStoreEnvironmentBean}
 */
public interface InfinispanSlotKeyGenerator {
    // generate a unique slot key for the given index (which is different from SlotStoreKey)
    byte[] generateUniqueKey(int index);
    // initialise the key generator with the InfinispanStoreEnvironment config
    void init(InfinispanStoreEnvironmentBean config);
}
