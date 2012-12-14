/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.txframework.api.configuration.trigger;

/**
 */
public enum BALifecycleEvent {
    /**
     * lifecycle event which happens if all service requests methods executed in the activity have read only
     * outcomes or if a service request method indicates, via a control, that the activity should exit.
     */
    EXIT,
    /**
     * lifecycle event which happens if a service request indicates that the activity cannot completed
     */
    NOT_COMPLETE,
    /**
     * lifecycle event which happens when the activity is completed. for coordinator completion activities this
     * occurs when a completed notification is received from the coordinator. for participant completion activities
     * this happens when a service request method indicates, via a control, that the activity has completed or a
     * service request annotated with an @Completes annotation finishes executing.
     */
    COMPLETE,
    /**
     * lifecycle event which happens when the middleware has made durable the completed.
     */
    CONFIRM_COMPLETE,
    /**
     * lifecycle event which happens when the activity is cancelled.
     */
    CANCEL,
    /**
     * lifecycle event which happens when the activity is closed.
     */
    CLOSE,
    /**
     * lifecycle event which happens when the activity is compensated.
     */
    COMPENSATE,
    /**
     * lifecycle event which happens when the activity fails.
     */
    FAIL,
    /*
     * lifecycle event which happens when an error in the protocol occurs.
     */
    Error,

    //todo: document when this occurs, why deprecated and what to use instead
    @Deprecated
    UNKNOWN,

    //todo: do we need this lifecycle event? 
    STATUS
}