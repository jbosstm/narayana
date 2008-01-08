/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2007,
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.internal.jta.resources.jts.orbspecific;

import com.arjuna.ArjunaOTS.JTAInterposedSynchronizationOperations;

/**
 * Implementation of the marker interface used to distinguish Synchronizations that
 * should be interposed in the JTA 1.1 TransactionSynchronizationRegistry sense of the term.
 */
public class JTAInterposedSynchronizationImple extends SynchronizationImple implements JTAInterposedSynchronizationOperations {

    public JTAInterposedSynchronizationImple(javax.transaction.Synchronization ptr) {
        super(ptr);
    }

    protected org.omg.PortableServer.Servant getPOATie() {
        return new com.arjuna.ArjunaOTS.JTAInterposedSynchronizationPOATie(this);
    }
}
