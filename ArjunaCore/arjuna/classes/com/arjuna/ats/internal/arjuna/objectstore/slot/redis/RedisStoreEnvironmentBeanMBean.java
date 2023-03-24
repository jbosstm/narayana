/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.arjuna.ats.internal.arjuna.objectstore.slot.redis;

import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBeanMBean;

public interface RedisStoreEnvironmentBeanMBean {

    /**
     * @return the connection string to the Redis server holding the logs
     */
    String getRedisURI();

    /**
     * set the connection details for the target Redis server
     *
     * @param redisURI connection string
     */
    public void setRedisURI(String redisURI);

    /**
     * {@link SlotStoreEnvironmentBeanMBean#getNumberOfSlots()}
     */
    public int getNumberOfSlots();

    /**
     * {@link SlotStoreEnvironmentBeanMBean#setNumberOfSlots(int)}
     */
    public void setNumberOfSlots(int numberOfSlots);

    /**
     * {@link SlotStoreEnvironmentBeanMBean#getBytesPerSlot()}
     */
    public int getBytesPerSlot();

    /**
     * {@link SlotStoreEnvironmentBeanMBean#setBytesPerSlot(int)} }
     */
    public void setBytesPerSlot(int bytesPerSlot);

    /**
     * {@link SlotStoreEnvironmentBeanMBean#setBackingSlots(BackingSlots)} }
     */
    public void setBackingSlots(RedisSlots redisSlots);
}
