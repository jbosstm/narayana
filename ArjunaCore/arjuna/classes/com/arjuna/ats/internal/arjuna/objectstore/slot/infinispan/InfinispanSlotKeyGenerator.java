package com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan;

public interface InfinispanSlotKeyGenerator {
    // generate a unique slot key for the given index (which is different from SlotStoreKey)
    byte[] generateUniqueKey(int index);
    // initialise the key generator with the InfinispanStoreEnvironment config
    void init(InfinispanStoreEnvironmentBean config);
}
