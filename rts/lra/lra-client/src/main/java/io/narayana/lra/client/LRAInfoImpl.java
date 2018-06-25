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

import org.eclipse.microprofile.lra.client.LRAInfo;

import java.net.URL;

public class LRAInfoImpl implements LRAInfo {
    private Exception jsonParseError;
    private String lraId;
    private String clientId;
    private boolean isComplete;
    private boolean isCompensated;
    private boolean isRecovering;
    private boolean isActive;
    private boolean isTopLevel;

    public LRAInfoImpl(String lraId) {
        this.lraId = lraId;
    }

    public LRAInfoImpl(URL lraId) {
        this.lraId = lraId.toString();
    }

    public LRAInfoImpl(String lraId, String clientId, boolean isComplete,
                       boolean isCompensated, boolean isRecovering,
                       boolean isActive, boolean isTopLevel) {
        this.lraId = lraId;
        this.clientId = clientId;
        this.isComplete = isComplete;
        this.isCompensated = isCompensated;
        this.isRecovering = isRecovering;
        this.isActive = isActive;
        this.isTopLevel = isTopLevel;
        this.jsonParseError = null;
    }

    public LRAInfoImpl(Exception e) {
        this.jsonParseError = e;
        this.lraId = "JSON Parse Error: " + e.getMessage();
        this.clientId = e.getMessage();
        this.isComplete = false;
        this.isCompensated = false;
        this.isRecovering = false;
        this.isActive = false;
        this.isTopLevel = false;
    }

    public String getLraId() {
        return this.lraId;
    }

    public String getClientId() {
        return this.clientId;
    }

    public boolean isComplete() {
        return this.isComplete;
    }

    public boolean isCompensated() {
        return this.isCompensated;
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof LRAInfo)) {
            return false;
        } else {
            LRAInfo lraStatus = (LRAInfo)o;
            return this.getLraId().equals(lraStatus.getLraId());
        }
    }

    public int hashCode() {
        return this.getLraId().hashCode();
    }
}
