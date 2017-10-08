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
package io.narayana.lra.client.internal.proxy;

import io.narayana.lra.client.participant.LRAParticipant;
import io.narayana.lra.client.participant.LRAParticipantDeserializer;

import java.net.URL;

class ParticipantProxy {
    private URL lraId;
    private String participantId;
    private LRAParticipant LRAParticipant;
    private LRAParticipantDeserializer deserializer;

    ParticipantProxy(URL lraId, String participantId, LRAParticipant LRAParticipant, LRAParticipantDeserializer deserializer) {
        this.lraId = lraId;
        this.participantId = participantId;
        this.LRAParticipant = LRAParticipant;
        this.deserializer = deserializer;
    }

    public ParticipantProxy(URL lraId, String participantId) {
        this.lraId = lraId;
        this.participantId = participantId;
    }


    public URL getLraId() {
        return lraId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public LRAParticipant getLRAParticipant() {
        return LRAParticipant;
    }

    public LRAParticipantDeserializer getDeserializer() {
        return deserializer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantProxy)) return false;

        ParticipantProxy that = (ParticipantProxy) o;

        if (!getLraId().equals(that.getLraId())) return false;
        return getParticipantId().equals(that.getParticipantId());
    }

    @Override
    public int hashCode() {
        int result = getLraId().hashCode();
        result = 31 * result + getParticipantId().hashCode();
        return result;
    }
}
