/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package io.narayana.lra.client.internal.proxy;

import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.participant.LRAParticipant;
import org.eclipse.microprofile.lra.participant.TerminationException;
import io.narayana.lra.proxy.logging.LRAProxyLogger;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class ParticipantProxy {
    private URI lraId;
    private String participantId;
    private LRAParticipant participant;
    private Future<Void> future;
    private boolean compensate;

    ParticipantProxy(URI lraId, String participantId, LRAParticipant participant) {
        this.lraId = lraId;
        this.participantId = participantId;
        this.participant = participant;
    }

    ParticipantProxy(URI lraId, String participantId) {
        this.lraId = lraId;
        this.participantId = participantId;
    }


    private URI getLraId() {
        return lraId;
    }

    String getParticipantId() {
        return participantId;
    }

    LRAParticipant getParticipant() {
        return participant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParticipantProxy)) {
            return false;
        }

        ParticipantProxy that = (ParticipantProxy) o;

        return getLraId().equals(that.getLraId()) && getParticipantId().equals(that.getParticipantId());
    }

    @Override
    public int hashCode() {
        int result = getLraId().hashCode();
        result = 31 * result + getParticipantId().hashCode();
        return result;
    }

     void setFuture(Future<Void> future, boolean compensate) {
        this.future = future;
        this.compensate = compensate;
    }

    private ParticipantStatus getExpectedStatus() {
        return compensate ? ParticipantStatus.Compensated : ParticipantStatus.Completed;
    }

    private ParticipantStatus getCurrentStatus() {
        return compensate ? ParticipantStatus.Compensating : ParticipantStatus.Completing;
    }

    private ParticipantStatus getFailedStatus() {
        return compensate ? ParticipantStatus.FailedToCompensate : ParticipantStatus.FailedToComplete;
    }

    Optional<ParticipantStatus> getStatus() throws InvalidLRAStateException {
        if (future == null) {
            return Optional.empty();
        }

        if (future.isDone()) {
            try {
                future.get();

                return Optional.of(getExpectedStatus());
            } catch (ExecutionException e) {
                if (!TerminationException.class.equals(e.getCause().getClass())) {
                    // the participant threw an unexpected exception
                    LRAProxyLogger.i18NLogger.error_participantExceptionOnCompletion(participant.getClass().getName(), e);
                }

                return Optional.of(getFailedStatus());
            } catch (InterruptedException e) {
                // the only recourse is to retry of mark as failed
                return Optional.of(getFailedStatus()); // interpret as failure
            }
        } else if (future.isCancelled()) {
            // the participant canceled it so assume it finished early
            return Optional.of(getExpectedStatus()); // success
        } else {
            return Optional.of(getCurrentStatus()); // still in progress
        }
    }
}
