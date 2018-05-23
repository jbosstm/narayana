/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat Middleware LLC, and individual contributors
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

package com.arjuna.ats.internal.jta.xa;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;

/**
 * Describing the state that the {@link TxInfo} bound to a {@link XAResource}
 * can obtain in regards of {@link AbstractRecord#beforeTopLevelPrepare()} handling.
 */
public enum PrePrepareState {
    /**
     * there was called end association for the resource
     */
    END_ASSOCIATION_CALLED,
    /**
     * there was called end association for the resource and finished with read only
     */
    END_ASSOCIATION_READ_ONLY_RESULT,
    /**
     * end association not called for the resource, state is unknown,
     * possibly there is not implemented the pre-prepare handling
     */
    NO_STATE
}
