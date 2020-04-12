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

import java.io.IOException;
import java.util.Objects;

import org.eclipse.microprofile.lra.annotation.LRAStatus;

//@Data
//@AllArgsConstructor
//@ApiModel( value = "LRA", description = "A Long Running Action" )
public class LRAStatusHolder {
    //    @ApiModelProperty( value = "The unique id of the LRA", required = true )
    private String lraId;
    //    @ApiModelProperty( value = "The client id associated with this LRA", required = false )
    private String clientId;
    //    @ApiModelProperty( value = "Indicates whether or not this LRA is recovering", required = false )
    private boolean isRecovering;
    //    @ApiModelProperty( value = "Indicates whether or not this LRA has been asked to complete or compensate yet", required = false )
    private boolean isActive;
    //    @ApiModelProperty( value = "Indicates whether or not this LRA is top level", required = false )
    private boolean isTopLevel;
    private int httpStatus;
    private String responseData;
    long startTime;
    long finishTime;
    long timeNow;
    String[] failedParticipants;

    private LRAStatus lraStatus;

    public LRAStatusHolder(Transaction lra) {

        LRAData data = lra.getLRAData();

        this.lraId = data.getLraId();
        this.clientId = data.getClientId();
        this.isRecovering = data.isRecovering();
        this.isActive = data.isActive();
        this.isTopLevel = data.isTopLevel();
        this.httpStatus = lra.getHttpStatus();
        this.responseData = lra.getResponseData();
        this.lraStatus = lra.getLRAStatus();
        this.failedParticipants = lra.getFailedParticipants().values().toArray(new String[0]);
    }

    public String getLraId() {
        return lraId;
    }

    public String getClientId() {
        return clientId;
    }

    public LRAStatus getStatus() {
        return lraStatus;
    }

    private boolean isInState(LRAStatus state) {
        return lraStatus != null && lraStatus == state;
    }

    public boolean isCancelling() {
        return isInState(LRAStatus.Cancelling);
    }

    public boolean isCancelled() {
        return isInState(LRAStatus.Cancelled);
    }

    public boolean isClosing() {
        return isInState(LRAStatus.Closing);
    }

    public boolean isClosed() {
        return isInState(LRAStatus.Closed);
    }

    public boolean isFailedToClose() {
        return isInState(LRAStatus.FailedToClose);
    }

    public boolean isFailedToCancel() {
        return isInState(LRAStatus.FailedToCancel);
    }

    public boolean isRecovering() {
        return isRecovering;
    }

    public boolean isActive() {
        return isActive;
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

    public String[] getFailedParticipants() {
        return failedParticipants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LRAStatusHolder lraStatus = (LRAStatusHolder) o;
        return Objects.equals(lraId, lraStatus.lraId) &&
                this.lraStatus == lraStatus.lraStatus;
    }

    @Override
    public int hashCode() {

        return Objects.hash(lraId);
    }
}
