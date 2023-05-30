/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package io.narayana.lra.client.internal.proxy;

import io.narayana.lra.proxy.logging.LRAProxyLogger;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class ParticipantProxy {
    private URI lraId;
    private String participantId;
    private LRAProxyParticipant participant;
    private Future<Void> future;
    private boolean compensate;

    ParticipantProxy(URI lraId, String participantId, LRAProxyParticipant participant) {
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

    LRAProxyParticipant getParticipant() {
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
                LRAProxyLogger.i18NLogger.error_participantExceptionOnCompletion(participant.getClass().getName(), e);
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