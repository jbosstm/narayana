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

import java.net.URL;

//@Data
//@AllArgsConstructor
//@ApiModel( value = "LRA", description = "A Long Running Action" )
public class LRAInfo {
    private Exception jsonParseError;
    //    @ApiModelProperty( value = "The unique id of the LRA", required = true )
    private String lraId;
//    @ApiModelProperty( value = "The client id associated with this LRA", required = false )
    private String clientId ;
//    @ApiModelProperty( value = "Indicates whether or not this LRA has completed", required = false )
    private boolean isComplete;
//    @ApiModelProperty( value = "Indicates whether or not this LRA has compensated", required = false )
    private boolean isCompensated;
//    @ApiModelProperty( value = "Indicates whether or not this LRA is recovering", required = false )
    private boolean isRecovering;
//    @ApiModelProperty( value = "Indicates whether or not this LRA has been asked to complete or compensate yet", required = false )
    private boolean isActive;
//    @ApiModelProperty( value = "Indicates whether or not this LRA is top level", required = false )
    private boolean isTopLevel;

    public LRAInfo(String lraId) {
        this.lraId = lraId;
    }
    public LRAInfo(URL lraId) {
        this.lraId = lraId.toString();
    }

    public LRAInfo(String lraId, String clientId, boolean isComplete, boolean isCompensated, boolean isRecovering, boolean isActive, boolean isTopLevel) {
        this.lraId = lraId;
        this.clientId = clientId;
        this.isComplete = isComplete;
        this.isCompensated = isCompensated;
        this.isRecovering = isRecovering;
        this.isActive = isActive;
        this.isTopLevel = isTopLevel;
        this.jsonParseError = null;
    }

    public LRAInfo(Exception e) {
        jsonParseError = e;
        this.lraId = "JSON Parse Error: " + e.getMessage();
        this.clientId = e.getMessage();
        this.isComplete = false;
        this.isCompensated = false;
        this.isRecovering = false;
        this.isActive = false;
        this.isTopLevel = false;
    }

    public String getLraId() {
        return lraId;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public boolean isCompensated() {
        return isCompensated;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LRAInfo)) return false;

        LRAInfo lraStatus = (LRAInfo) o;

        return getLraId().equals(lraStatus.getLraId());
    }

    @Override
    public int hashCode() {
        return getLraId().hashCode();
    }
}
