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
package io.narayana.lra.participant.model;

import org.eclipse.microprofile.lra.annotation.CompensatorStatus;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class Activity implements Serializable {
    private static final long serialVersionUID = -4141599248046299770L;
    public String id;
    public String rcvUrl;
    public String statusUrl;
    public CompensatorStatus status;
    public boolean registered;
    public String registrationStatus;
    private String userData;
    private String endData;
    private String how;
    private String arg;

    private final AtomicInteger acceptedCount = new AtomicInteger(0);

    public Activity(String txId) {
        this.id = txId;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getUserData() {
        return userData;
    }

    public void setEndData(String endData) {
        this.endData = endData;
    }

    public String getEndData() {
        return endData;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id='" + id + '\'' +
                ", rcvUrl='" + rcvUrl + '\'' +
                ", statusUrl='" + statusUrl + '\'' +
                ", status=" + status +
                ", registered=" + registered +
                ", registrationStatus='" + registrationStatus + '\'' +
                ", userData='" + userData + '\'' +
                ", endData='" + endData + '\'' +
                '}';
    }

    public int getAcceptedCount() {
        return acceptedCount.get();
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount.set(acceptedCount);
    }


    public int getAndDecrementAcceptCount() {
        return acceptedCount.getAndDecrement();
    }

    public String getHow() {
        return how;
    }

    public void setHow(String how) {
        this.how = how;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }
}
