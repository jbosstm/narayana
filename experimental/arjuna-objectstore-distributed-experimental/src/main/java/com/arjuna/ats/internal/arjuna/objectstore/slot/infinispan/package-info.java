/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

/**
 * EXPERIMENTAL: Infinispan-backed SlotStore ObjectStore Implementation
 *
 * <p>This package provides an experimental implementation of Narayana's SlotStore
 * backed by Infinispan distributed caches. It enables in-memory, distributed
 * transaction log storage across a cluster of nodes.
 *
 * <p><strong>WARNING:</strong> This is an experimental feature. It is not recommended
 * for production systems and may contain breaking changes or be removed entirely in
 * future releases.
 *
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanSlots} -
 *       Main implementation of {@link com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots}
 *       using Infinispan cache as the backing store</li>
 *   <li>{@link com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanStoreEnvironmentBean} -
 *       Configuration bean for Infinispan store settings</li>
 *   <li>{@link com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan.InfinispanSlotKeyGenerator} -
 *       Interface for generating unique cache keys for slot entries</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>To use this experimental feature, add the following Maven dependency:
 * <pre>{@code
 * <dependency>
 *     <groupId>org.jboss.narayana.experimental</groupId>
 *     <artifactId>arjuna-objectstore-distributed-experimental</artifactId>
 *     <version>${narayana.version}</version>
 * </dependency>
 * }</pre>
 *
 * <h2>Configuration</h2>
 * <p>Configure the ObjectStore to use Infinispan by setting the SlotStore backing
 * slots implementation:
 * <pre>{@code
 * SlotStoreEnvironmentBean slotStoreEnvBean = ...;
 * slotStoreEnvBean.setBackingSlots(InfinispanSlots.class.getName());
 * }</pre>
 *
 * <h2>Important Considerations</h2>
 * <p>When using this feature in a clustered environment with recovery management:
 * <ul>
 *   <li>Configure Infinispan for appropriate replication (REPL_SYNC or DIST_SYNC)</li>
 *   <li>Use JGroups-Raft or equivalent for strict consistency in CP systems</li>
 *   <li>Ensure only one recovery manager acts as the leader</li>
 *   <li>Configure automatic failover with split-brain avoidance</li>
 *   <li>Review the design document for architecture and consistency trade-offs</li>
 * </ul>
 *
 * <p><strong>Sponsor:</strong> Michael Musgrove (mmusgrov@redhat.com)
 *
 * <p><strong>Tracking:</strong>
 * <a href="https://issues.redhat.com/browse/JBTM-845">JBTM-845</a>
 *
 * <p><strong>Design Document:</strong> See module README.md for architectural details
 *
 * @since 7.0.0
 * @see com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStore
 * @see com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot.infinispan;
