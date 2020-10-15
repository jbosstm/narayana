package io.narayana.lra;

import org.eclipse.microprofile.lra.annotation.LRAStatus;

/**
 * DTO object which serves to transfer data of particular LRA instance.
 * It's used by {@link io.narayana.lra.coordinator.api.Coordinator}
 * for JSON response creation when LRA info is asked for.
 */
public class LRAData {
    private String lraId;
    private String clientId;
    private LRAStatus status;
    private boolean isTopLevel;
    private boolean isRecovering;
    private long startTime;
    private long finishTime;
    private int httpStatus;

    public LRAData() {}

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

    public void setLraId(String lraId) {
        this.lraId = lraId;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public LRAStatus getStatus() {
        return this.status;
    }

    public void setStatus(LRAStatus status) {
        this.status = status;
    }

    public boolean isTopLevel() {
        return this.isTopLevel;
    }

    public void setTopLevel(boolean topLevel) {
        isTopLevel = topLevel;
    }

    public boolean isRecovering() {
        return this.isRecovering;
    }

    public void setRecovering(boolean recovering) {
        isRecovering = recovering;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public int getHttpStatus() {
        return this.httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
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
