/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2020,
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot;

import com.arjuna.common.internal.util.ClassloadingUtility;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

import java.io.File;

/**
 * A JavaBean containing configuration properties for the SlotStore based transaction logging system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2020-03
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.slotstore.")
public class SlotStoreEnvironmentBean implements SlotStoreEnvironmentBeanMBean {

    private volatile int numberOfSlots = 256;

    private volatile int bytesPerSlot = 4096;

    private volatile String storeDir = System.getProperty("user.dir") + File.separator + "SlotStore";

    private volatile boolean syncWrites = true;

    private volatile boolean syncDeletes = true;

    private volatile String backingSlotsClassName = "com.arjuna.ats.internal.arjuna.objectstore.slot.VolatileSlots";
    private volatile BackingSlots backingSlots = null;

    /**
     * Returns the desired number of slots for the store.
     *
     * @return The capacity, in entries, of the store.
     */
    public int getNumberOfSlots() {
        return numberOfSlots;
    }

    /**
     * Sets the desired number of slots for the store.
     * Should equal the maximum number of unresolved transactions expected at any given time,
     * including those in-flight and awaiting recovery.
     * <p>
     * Caution: reducing the number of slots in a non-empty store may result in data loss.
     * <p>
     * Default: 256
     *
     * @param numberOfSlots The capacity, in entries, of the store.
     */
    public void setNumberOfSlots(int numberOfSlots) {
        this.numberOfSlots = numberOfSlots;
    }

    /**
     * Returns the desired maximum record size for the store.
     *
     * @return The max size, in bytes, of a store entry.
     */
    public int getBytesPerSlot() {
        return bytesPerSlot;
    }

    /**
     * Sets the desired maximum size of entries in the store.
     * A typical tx record is under 1k. A 4k (disk block) size is probably reasonable.
     * <p>
     * Caution: modifying the size of slots in a non-empty store may result in data loss.
     * <p>
     * Default: 4k
     *
     * @param bytesPerSlot the max size, in bytes, of a store entry.
     */
    public void setBytesPerSlot(int bytesPerSlot) {
        this.bytesPerSlot = bytesPerSlot;
    }

    /**
     * Returns the store directory path
     *
     * @return the store directory name
     */
    public String getStoreDir() {
        return storeDir;
    }

    /**
     * Sets the store directory path.
     * <p>
     * Default: {user.dir}/SlotStore
     *
     * @param storeDir the path to the store directory.
     */
    public void setStoreDir(String storeDir) {
        this.storeDir = storeDir;
    }

    /**
     * Returns the sync setting for transaction store write operations.
     * To preserve ACID properties this value must be set to true, in which case
     * log write operations block until data is forced to the physical storage device.
     * Turn sync off only if you don't care about data integrity.
     *
     * @return true if log writes should be synchronous, false otherwise.
     */
    public boolean isSyncWrites() {
        return syncWrites;
    }

    /**
     * Sets if store write operations should be synchronous or not.
     * <p>
     * Default: true.
     *
     * @param syncWrites true for synchronous operation, false otherwise.
     */
    public void setSyncWrites(boolean syncWrites) {
        this.syncWrites = syncWrites;
    }

    /**
     * Returns the sync setting for transaction store delete operations.
     * For optimal crash recovery this value should be set to true.
     * Asynchronous deletes may give rise to unnecessary crash recovery complications.
     *
     * @return true if log deletes should be synchronous, false otherwise.
     */
    public boolean isSyncDeletes() {
        return syncDeletes;
    }

    /**
     * Sets if store delete operations should be synchronous or not.
     * <p>
     * Default: true.
     *
     * @param syncDeletes true for synchronous operation, false otherwise.
     */
    public void setSyncDeletes(boolean syncDeletes) {
        this.syncDeletes = syncDeletes;
    }

    /**
     * Returns the class name of the com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots implementation
     * <p>
     * Default: "com.arjuna.ats.internal.arjuna.objectstore.slot.VolatileSlots"
     *
     * @return the name of the class implementing BackingSlots.
     */
    public String getBackingSlotsClassName() {
        return backingSlotsClassName;
    }

    public void setBackingSlotsClassName(String backingSlotsClassName) {
        synchronized (this) {
            if (backingSlotsClassName == null) {
                this.backingSlots = null;
            } else if (!backingSlotsClassName.equals(this.backingSlotsClassName)) {
                this.backingSlots = null;
            }
            this.backingSlotsClassName = backingSlotsClassName;
        }
    }

    /**
     * Returns an instance of a class implementing com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots
     * <p>
     * If there is no pre-instantiated instance set and classloading or instantiation fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return a BackingSlots implementation instance, or null.
     */
    public BackingSlots getBackingSlots() {
        if (backingSlots == null && backingSlotsClassName != null) {
            synchronized (this) {
                if (backingSlots == null && backingSlotsClassName != null) {
                    backingSlots = ClassloadingUtility.loadAndInstantiateClass(BackingSlots.class, backingSlotsClassName, null);
                }
            }
        }
        return backingSlots;
    }

    /**
     * Sets the instance of BackingSlots
     *
     * @param instance an Object that implements com.arjuna.ats.internal.arjuna.objectstore.slot.BackingSlots
     */
    public void setBackingSlots(BackingSlots instance) {
        synchronized (this) {
            BackingSlots oldInstance = this.backingSlots;
            backingSlots = instance;

            if (instance == null) {
                this.backingSlotsClassName = null;
            } else if (instance != oldInstance) {
                String name = ClassloadingUtility.getNameForClass(instance);
                this.backingSlotsClassName = name;
            }
        }
    }
}
