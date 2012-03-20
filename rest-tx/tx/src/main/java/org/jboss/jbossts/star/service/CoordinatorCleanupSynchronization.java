/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2012
 * @author JBoss Inc.
 */
package org.jboss.jbossts.star.service;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.jbossts.star.resource.Transaction;

import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.arjuna.coordinator.SynchronizationRecord;

/**
 * This synchronization initiates cleanup of the {@link #transaction} and it's participants from the {@link Coordinator}'s
 * internal store. The {@link #beforeCompletion()} is not called when a transaction times out in which case the cleanup
 * is initiated with an empty list of participants and the {@link Coordinator} locates the participants.
 */
public class CoordinatorCleanupSynchronization implements SynchronizationRecord {

    private Coordinator coordinator;
    private Uid uid = new Uid();
    private Transaction transaction;
    private Collection<String> enlistmentIds;

    public CoordinatorCleanupSynchronization(Coordinator coordinator, Transaction transaction) {
        this.coordinator = coordinator;
        this.transaction = transaction;
    }

    @Override
    public int compareTo(Object o) {
        SynchronizationRecord sr = (SynchronizationRecord) o;
        if (uid.equals(sr.get_uid())) {
            return 0;
        }
        else {
            return uid.lessThan(sr.get_uid()) ? -1 : 1;
        }
    }

    @Override
    public Uid get_uid() {
        return uid;
    }

    @Override
    public boolean beforeCompletion() {
        // Get a list of the participants before they check-out of the transaction
        enlistmentIds = new ArrayList<String>();
        transaction.getParticipants(enlistmentIds);
        return true;
    }

    @Override
    public boolean afterCompletion(int status) {
        coordinator.removeTxState(transaction, enlistmentIds);
        return true;
    }

}
