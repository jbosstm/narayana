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
package io.narayana.lra.annotation;

/**
 * The status of a compensator. The status is only valid after the coordinator has told the compensator to
 * complete or compensate. The name value of the enum should be returned by compensator methods marked with
 * the {@link Status} annotation.
 */
public enum CompensatorStatus {
    /**
     * the Compensator is currently compensating for the LRA
     */
    Compensating,
    /**
     * the Compensator has successfully compensated for the LRA
     */
    Compensated,
    /**
     * the Compensator was not able to compensate for the LRA (and must remember
     * it could not compensate until such time that it receives a forget message)
     */
    FailedToCompensate,
    /**
     * the Compensator is tidying up after being told to complete
     */
    Completing,
    /**
     * the Compensator has confirmed
     */
    Completed,
    /**
     * the Compensator was unable to tidy-up
     */
    FailedToComplete,
}
