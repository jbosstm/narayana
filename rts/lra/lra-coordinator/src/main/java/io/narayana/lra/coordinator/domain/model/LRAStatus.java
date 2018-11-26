/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package io.narayana.lra.coordinator.domain.model;

import org.eclipse.microprofile.lra.annotation.CompensatorStatus;
import org.eclipse.microprofile.lra.client.LRAInfo;

import java.io.IOException;

//@Data
//@AllArgsConstructor
//@ApiModel( value = "LRA", description = "A Long Running Action" )
public class LRAStatus {
    //    @ApiModelProperty( value = "The unique id of the LRA", required = true )
    private String lraId;
    //    @ApiModelProperty( value = "The client id associated with this LRA", required = false )
    private String clientId;

    //    @ApiModelProperty( value = "Indicates whether or not this LRA is top level", required = false )
    private boolean isTopLevel;
    private int httpStatus;
    private String responseData;
    private long startTime;
    private long finishTime;
    private long timeNow;

    private CompensatorStatus status;
    private boolean isRecovering;

    public LRAStatus(Transaction lra) {
        LRAInfo info = lra.getLRAInfo();

        this.lraId = info.getLraId();
        this.clientId = info.getClientId();
        this.isTopLevel = info.isTopLevel();
        this.httpStatus = lra.getHttpStatus();
        this.responseData = lra.getResponseData();
        this.status = lra.getLRAStatus();
        this.isRecovering = lra.isRecovering();
        this.startTime = lra.getStartTime();
        this.finishTime = lra.getFinishTime();
        this.timeNow = lra.getTimeNow();
    }

    public String getLraId() {
        return lraId;
    }

    public String getClientId() {
        return clientId;
    }

    public CompensatorStatus getStatus() {
        return status;
    }

    private boolean isInState(CompensatorStatus state) {
        return status != null && status == state;
    }

    public boolean isCompensating() {
        return isInState(CompensatorStatus.Compensating);
    }

    public boolean isCompensated() {
        return isInState(CompensatorStatus.Compensated);
    }

    public boolean isCompleting() {
        return isInState(CompensatorStatus.Completing);
    }

    public boolean isCompleted() {
        return isInState(CompensatorStatus.Completed);
    }

    public boolean isFailedToComplete() {
        return isInState(CompensatorStatus.FailedToComplete);
    }

    public boolean isFailedToCompensate() {
        return isInState(CompensatorStatus.FailedToCompensate);
    }

    public boolean isRecovering() {
        return isRecovering;
    }

    public boolean isActive() {
        return status == null;
    }

    public boolean isTopLevel() {
        return isTopLevel;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getResponseData() {
        return responseData;
    }

    public String getEncodedResponseData() throws IOException {
        return responseData;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public long getTimeNow() {
        return timeNow;
    }
}
