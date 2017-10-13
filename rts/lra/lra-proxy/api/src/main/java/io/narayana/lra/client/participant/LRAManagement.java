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
package io.narayana.lra.client.participant;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public interface LRAManagement {
    /**
     * Join an existing LRA
     *
     * @param participant an instance of a {@link LRAParticipant} that will be notified when the target LRA ends
     * @param deserializer a mechanism for recreating participants during recovery.
     *                     If the parameter is null then standard Java object deserialization will be used
     * @param lraId the LRA that the join request pertains to
     * @param timeLimit the time for which the participant should remain valid. When this time limit is exceeded
     *                  the participant may longer be able to fulfil the protocol guarantees.
     * @param unit the unit that the timeLimit parameter is expressed in
     */
    String joinLRA(LRAParticipant participant, LRAParticipantDeserializer deserializer,
                   URL lraId, Long timeLimit, TimeUnit unit) throws JoinLRAException;

    /**
     * Join an existing LRA. In contrast to the other form of registration this method does not indicate a time limit
     * for the participant meaning that the participant registration will remain valid until it terminates successfully
     * or unsuccessfully (ie it will never be timed out externally).
     *
     * @param participant an instance of a {@link LRAParticipant} that will be notified when the target LRA ends
     * @param deserializer a mechanism for recreating participants during recovery.
     *                     If the parameter is null then standard Java object deserialization will be used
     * @param lraId the LRA that the join request pertains to
     */
    String joinLRA(LRAParticipant participant, LRAParticipantDeserializer deserializer, URL lraId) throws JoinLRAException;
}
