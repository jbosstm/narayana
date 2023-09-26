/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.resources.jts.orbspecific;

import com.arjuna.ArjunaOTS.JTAInterposedSynchronizationOperations;

/**
 * Implementation of the marker interface used to distinguish Synchronizations that
 * should be interposed in the JTA 1.1 TransactionSynchronizationRegistry sense of the term.
 */
public class JTAInterposedSynchronizationImple extends SynchronizationImple implements JTAInterposedSynchronizationOperations {

    public JTAInterposedSynchronizationImple(jakarta.transaction.Synchronization ptr) {
        super(ptr);
    }

    protected org.omg.PortableServer.Servant getPOATie() {
        return new com.arjuna.ArjunaOTS.JTAInterposedSynchronizationPOATie(this);
    }
}