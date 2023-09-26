/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot;

/**
 * A JMX MBean interface containing configuration for the SlotStore transaction logging system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface SlotStoreEnvironmentBeanMBean {

    int getNumberOfSlots();

    void setNumberOfSlots(int numberOfSlots);

    int getBytesPerSlot();

    void setBytesPerSlot(int bytesPerSlot);

    String getStoreDir();

    void setStoreDir(String storeDir);

    boolean isSyncWrites();

    void setSyncWrites(boolean syncWrites);

    boolean isSyncDeletes();

    void setSyncDeletes(boolean syncDeletes);

    String getBackingSlotsClassName();

    void setBackingSlotsClassName(String backingSlotsClassName);

    BackingSlots getBackingSlots();

    void setBackingSlots(BackingSlots instance);
}