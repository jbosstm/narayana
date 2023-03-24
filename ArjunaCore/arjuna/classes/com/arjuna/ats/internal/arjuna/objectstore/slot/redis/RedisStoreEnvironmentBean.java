/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.arjuna.ats.internal.arjuna.objectstore.slot.redis;

import com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots;
import com.arjuna.ats.internal.arjuna.objectstore.slot.SlotStoreEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class RedisStoreEnvironmentBean {
    private final SlotStoreEnvironmentBean slotStoreEnvironmentBean;
    private String failoverId = "0";
    private String redisURI = "redis://127.0.0.1:6379";
    private String redisHost = "127.0.0.1";
    private int redisPort = 30001;

    private boolean clustered = true;

    public RedisStoreEnvironmentBean() {
        slotStoreEnvironmentBean = BeanPopulator.getDefaultInstance(SlotStoreEnvironmentBean.class);
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

    public boolean isClustered() {
        return clustered;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    /**
     * @return the connection string to the Redis server holding the logs
     */
    public String getRedisURI() {
        return redisURI;
    }

    /**
     * set the connection details for the target Redis server
     *
     * @param redisURI connection string
     */
    public void setRedisURI(String redisURI) {
        this.redisURI = redisURI;
    }

    /**
     * {@link SlotStoreEnvironmentBean#getNumberOfSlots()}
     */
    public int getNumberOfSlots() {
        return slotStoreEnvironmentBean.getNumberOfSlots();
    }

    /**
     * {@link SlotStoreEnvironmentBean#setNumberOfSlots(int)}
     */
    public void setNumberOfSlots(int numberOfSlots) {
        slotStoreEnvironmentBean.setNumberOfSlots(numberOfSlots);
    }

    /**
     * {@link SlotStoreEnvironmentBean#getBytesPerSlot()}
     */
    public int getBytesPerSlot() {
        return slotStoreEnvironmentBean.getBytesPerSlot();
    }

    /**
     * {@link SlotStoreEnvironmentBean#setBytesPerSlot(int)} }
     */
    public void setBytesPerSlot(int bytesPerSlot) {
        slotStoreEnvironmentBean.setBytesPerSlot(bytesPerSlot);
    }

    /**
     * {@link SlotStoreEnvironmentBean#setBackingSlots(BackingSlots)} }
     */
    public void setBackingSlots(RedisSlots redisSlots) {
        slotStoreEnvironmentBean.setBackingSlots(redisSlots);
    }

    public String getFailoverId() {
        return failoverId;
    }

    public void setFailoverId(String failoverId) {
        this.failoverId = failoverId;
    }
}
