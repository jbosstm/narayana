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
package io.narayana.lra.client;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class NarayanaLRAInfo {
    private String lraId;
    private String clientId;
    private String status;
    private boolean isClosed;
    private boolean isCancelled;
    private boolean isRecovering;
    private boolean isActive;
    private boolean isTopLevel;
    private long startTime;
    private long finishTime;

    public NarayanaLRAInfo(String lraId, String clientId, String status,
                boolean isClosed, boolean isCancelled, boolean isRecovering,
                boolean isActive, boolean isTopLevel,
                long startTime, long finishTime) {
        this.lraId = lraId;
        this.clientId = clientId;
        this.status = status;
        this.isClosed = isClosed;
        this.isCancelled = isCancelled;
        this.isRecovering = isRecovering;
        this.isActive = isActive;
        this.isTopLevel = isTopLevel;
        this.startTime = startTime;
        this.finishTime = finishTime;
    }

    public String getLraId() {
        return this.lraId;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isClosed() {
        return this.isClosed;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public boolean isRecovering() {
        return this.isRecovering;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public boolean isTopLevel() {
        return this.isTopLevel;
    }


    public long getStartTime() {
        return startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public long getTimeNow() {
        return LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    public ZoneOffset getZoneOffset() {
        return ZoneOffset.UTC;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof NarayanaLRAInfo)) {
            return false;
        } else {
            NarayanaLRAInfo lraStatus = (NarayanaLRAInfo)o;
            return this.getLraId().equals(lraStatus.getLraId());
        }
    }

    public int hashCode() {
        return this.getLraId().hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "lraId='" + lraId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", status='" + status + '\'' +
                ", isClosed=" + isClosed +
                ", isCancelled=" + isCancelled +
                ", isRecovering=" + isRecovering +
                ", isActive=" + isActive +
                ", isTopLevel=" + isTopLevel +
                ", startTime=" + startTime +
                ", finishTime=" + finishTime +
                '}';
    }
}
