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

import io.narayana.lra.annotation.CompensatorStatus;
import io.narayana.lra.client.participant.LRAParticipant;
import io.narayana.lra.client.participant.TerminationException;

import javax.ws.rs.NotFoundException;
import java.io.Serializable;
import java.net.URL;
import java.util.concurrent.Future;

public class Participant implements LRAParticipant, Serializable {
    private Activity activity;

    public Participant(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Future<Void> completeWork(URL lraId) throws NotFoundException, TerminationException {
        activity.status = CompensatorStatus.Completed;

        return null;
    }

    @Override
    public Future<Void> compensateWork(URL lraId) throws NotFoundException, TerminationException {
        activity.status = CompensatorStatus.Compensated;

        return null;
    }
}
