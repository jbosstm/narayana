/*
 * SPDX short identifier: Apache-2.0
 */

package io.narayana.lra.proxy.test.model;

import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import java.io.Serializable;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

public class Activity implements Serializable {
    public String id;
    public URI rcvUrl;
    public String statusUrl;
    public ParticipantStatus status;
    public boolean registered;
    public String registrationStatus;
    private String userData;
    private String endData;
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
        return this.getClass().getSimpleName() + "{" +
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
}