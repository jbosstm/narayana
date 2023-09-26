/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.resources.jts.orbspecific;

import com.arjuna.ArjunaOTS.ManagedSynchronizationOperations;

public class ManagedSynchronizationImple extends SynchronizationImple implements ManagedSynchronizationOperations
{
    private jakarta.transaction.Synchronization ptr;

    public ManagedSynchronizationImple(jakarta.transaction.Synchronization ptr) {
        super(ptr);
        this.ptr = ptr;
    }

    protected org.omg.PortableServer.Servant getPOATie() {
        return new com.arjuna.ArjunaOTS.ManagedSynchronizationPOATie(this);
    }

    public String implementationType()
    {
        return ptr.getClass().getName();
    }

    public String instanceName()
    {
        return ptr.toString();
    }
}