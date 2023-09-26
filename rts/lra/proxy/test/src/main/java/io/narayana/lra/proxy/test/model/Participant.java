/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.proxy.test.model;

import io.narayana.lra.client.internal.proxy.LRAProxyParticipant;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import jakarta.ws.rs.NotFoundException;
import java.io.Serializable;
import java.net.URI;
import java.util.concurrent.Future;

public class Participant implements LRAProxyParticipant, Serializable {
    private Activity activity;

    public Participant(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Future<Void> completeWork(URI lraId) throws NotFoundException {
        activity.status = ParticipantStatus.Completed;

        return null;
    }

    @Override
    public Future<Void> compensateWork(URI lraId) throws NotFoundException {
        activity.status = ParticipantStatus.Compensated;

        return null;
    }
}