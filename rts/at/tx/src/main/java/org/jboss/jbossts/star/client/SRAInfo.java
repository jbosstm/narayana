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
package org.jboss.jbossts.star.client;

import java.net.URL;

public class SRAInfo {
    private String sraId;
    private String clientId ;
    private boolean isComplete;
    private boolean isCompensated;
    private boolean isRecovering;
    private boolean isActive;
    private boolean isTopLevel;

    public SRAInfo(String sraId) {
        this.sraId = sraId;
    }
    public SRAInfo(URL sraId) {
        this.sraId = sraId.toString();
    }

    public SRAInfo(String sraId, String clientId, boolean isComplete, boolean isCompensated, boolean isRecovering, boolean isActive, boolean isTopLevel) {
        this.sraId = sraId;
        this.clientId = clientId;
        this.isComplete = isComplete;
        this.isCompensated = isCompensated;
        this.isRecovering = isRecovering;
        this.isActive = isActive;
        this.isTopLevel = isTopLevel;
    }

    public String getSraId() {
        return sraId;
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
        if (!(o instanceof SRAInfo)) return false;

        SRAInfo sraStatus = (SRAInfo) o;

        return getSraId().equals(sraStatus.getSraId());
    }

    @Override
    public int hashCode() {
        return getSraId().hashCode();
    }
}
