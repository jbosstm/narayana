/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan;

import com.arjuna.ats.arjuna.logging.tsLogger;
import org.infinispan.conflict.EntryMergePolicy;
import org.infinispan.container.entries.CacheEntry;

import java.util.List;

/*
 * Merge policy for partition conflict resolution is to use the "preferredEntry" which is the CacheEntry associated
 * with the partition whose coordinator is conducting the merge (or if multiple entries exist in this partition,
 * it’s the primary replica).
 *
 * Log any conflicts to facilitate post-mortem analysis.
 */
public class InfinispanSlotsPreferredEntryMergePolicy implements EntryMergePolicy<String, String> {
    @Override
    public CacheEntry<String, String> merge(CacheEntry<String, String> preferredEntry, List<CacheEntry<String, String>> otherEntries) {
        tsLogger.logger.infof("InfinispanSlotsPreferredEntryMergePolicy: choosing %s over alternatives:%n",
                preferredEntry);

        for (CacheEntry<String, String> entry : otherEntries) {
            tsLogger.logger.infof("\t%s%n", entry);
        }

        return preferredEntry;
    }
}