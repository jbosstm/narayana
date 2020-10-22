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
package io.narayana.lra.proxy.test.model;

import io.narayana.lra.client.internal.proxy.LRAProxyParticipant;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;

import javax.ws.rs.NotFoundException;
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
