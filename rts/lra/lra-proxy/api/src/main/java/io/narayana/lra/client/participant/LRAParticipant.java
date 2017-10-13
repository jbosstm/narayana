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

import javax.ws.rs.NotFoundException;
import java.io.Serializable;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The API for notifying participants that a LRA is completing or cancelling.
 * A participant joins with an LRA via a call to
 * {@link LRAManagement#joinLRA(LRAParticipant, LRAParticipantDeserializer, URL, Long, TimeUnit)}
 */
public interface LRAParticipant extends Serializable {
    /**
     * Notifies the participant that the LRA is closing
     * @param lraId the LRA that is closing
     * @return null if the participant completed successfully. If the participant cannot
     *         complete immediately it should return a future that the caller can use
     *         to monitor progress. If the JVM crashes before the participant can finish
     *         it should expect this method to be called again. If the participant fails
     *         to complete it must cancel the future or throw a TerminationException.
     * @throws NotFoundException the participant does not know about this LRA
     * @throws TerminationException the participant was unable to complete and will never
     *         be able to do so
     */
    Future<Void> completeWork(URL lraId) throws NotFoundException, TerminationException;

    /**
     * Notifies the participant that the LRA is cancelling
     * @param lraId the LRA that is closing
     * @return null if the participant completed successfully. If the participant cannot
     *         complete immediately it should return a future that the caller can use
     *         to monitor progress. If the JVM crashes before the participant can finish
     *         it should expect this method to be called again. If the participant fails
     *         to complete it must cancel the future or throw a TerminationException.
     * @throws NotFoundException the participant does not know about this LRA
     * @throws TerminationException the participant was unable to complete and will never
     *         be able to do so
     */
    Future<Void> compensateWork(URL lraId) throws NotFoundException, TerminationException;
}

