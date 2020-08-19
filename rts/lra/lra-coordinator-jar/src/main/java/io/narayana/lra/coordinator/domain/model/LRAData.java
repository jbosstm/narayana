package io.narayana.lra.coordinator.domain.model;

import org.eclipse.microprofile.lra.annotation.LRAStatus;

/**
 * DTO object which serves to transfer data of particular LRA instance.
 * It's used by {@link io.narayana.lra.coordinator.api.Coordinator}
 * for JSON response creation when LRA info is asked for.
 */
public class LRAData {
    private final String lraId;
    private final String clientId;
    private final LRAStatus status;
    private final boolean isTopLevel;
    private final boolean isRecovering;
    private final long startTime;
    private final long finishTime;
    private final int httpStatus;

    public LRAData(String lraId, String clientId, LRAStatus status, boolean isTopLevel, boolean isRecovering,
                    long startTime, long finishTime, int httpStatus) {
        this.lraId = lraId;
        this.clientId = clientId;
        this.status = status;
        this.isTopLevel = isTopLevel;
        this.isRecovering = isRecovering;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.httpStatus = httpStatus;
    }

    public String getLraId() {
        return this.lraId;
    }

    public String getClientId() {
        return this.clientId;
    }

    public LRAStatus getStatus() {
        return this.status;
    }

    public boolean isTopLevel() {
        return this.isTopLevel;
    }

    public boolean isRecovering() {
        return this.isRecovering;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public int getHttpStatus() {
        return this.httpStatus;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof LRAData)) {
            return false;
        } else {
            LRAData lraData = (LRAData) o;
            return this.getLraId().equals(lraData.getLraId());
        }
    }

    public int hashCode() {
        return this.getLraId().hashCode();
    }

    @Override
    public String toString() {
        return String.format(
            "%s {lraId='%s', clientId='%s', status='%s', isTopLevel=%b, isRecovering=%b, startTime=%d, finishTime=%d, httpStatus=%d}",
                this.getClass().getSimpleName(), lraId, clientId, status,
                isTopLevel, isRecovering, startTime, finishTime, httpStatus);
    }
}
