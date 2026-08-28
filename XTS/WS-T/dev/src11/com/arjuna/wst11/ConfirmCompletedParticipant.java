/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 */
package com.arjuna.wst11;

/**
 * This interface can be implemented by a BA participant in order to allow it to perform
 * its complete operation in two phases. This enables the participant to prepare
 * changes to persistent state and then commit them i) after the participant recovery
 * record has been written to disk but ii) before the coordinator is notified that the
 * participant has completed.
 *
 * A participant completion participant is expected to prepare its changes before calling
 * completed. The BA implementation will call confirmCompleted after writing a recovery
 * record, ensuring compensation is possible before the changes are persisted. It will
 * not notify the coordinator that the participant has completed until after the callback
 * returns, ensuring that any notification received by the coordinator is valid.
 *
 * A coordinator completion participant is expected to prepare its changes when its
 * completed method is called and then return. The BA implementation will call
 * confirmCompleted after writing a recovery record, ensuring compensation is possible
 * before the changes are persisted. It will not notify the coordinator that the
 * participant has completed until after the callback returns, ensuring that any
 * notification received by the coordinator is valid.
 *
 * The associated participant recovery module must be able to detect uncommitted changes.
 * The logged recovery record should include some sort of identifier tying the changes to the
 * participant. If the recovery module identifies uncommitted changes with no associated
 * recovery record then a crash happened between prepare and completed and this means the
 * changes should be rolled back (presumed abort). If the recovery module identifies
 * uncommitted changes with an associated recovery record then a crash happened after completed
 * but before the confirmCompleted callback. The changes can be rolled forward and the participant
 * recreated. Alternatively, the changes can be rolled back and the participant record rejected.
 * If a recovery record is found with no uncommitted changes then the participant can be safely
 * recreated, allowing the recovery manager to resend a committed message.
 *
 * It is possible that a completion operation may be initiated and then be cancelled part way through,
 * e.g. because a CANCEL message is received from the coordinator while writing the log record. in this
 * case the client will need to roll back any uncommitted changes. A boolean flag argument to the
 * confirmCompleted method is used to distinguish this case. If the flag is supplied as true then
 * changes should be committed. If it is supplied as false then changes should be rolled back.
 */

public interface ConfirmCompletedParticipant
{
    /**
     * a participant callback use to notify the participant either that a recovery record has been written to
     * the log and hence that uncommitted changes should be committed or that completion was cancelled and
     * hence that uncommitted changes should be rolled back.
     * @param confirmed true if the log record has been written and changes should be rolled forward and false
     * if it has not been written and changes should be rolled back
     */
    public void confirmCompleted(boolean confirmed);
}
