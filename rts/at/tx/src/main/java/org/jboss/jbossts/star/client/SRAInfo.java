/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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