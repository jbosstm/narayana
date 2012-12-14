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

package org.jboss.narayana.txframework.impl.handlers.restat.service;

import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Commit;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Prepare;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Rollback;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.util.Map;

public class RESTAT2PCParticipant extends org.jboss.narayana.txframework.impl.Participant {

    public RESTAT2PCParticipant(ServiceInvocationMeta serviceInvocationMeta, Map txDataMap) throws ParticipantRegistrationException {

        super(serviceInvocationMeta, txDataMap);

        registerEventsOfInterest(Rollback.class, Commit.class, Prepare.class);
    }

    public void commit() {

        invoke(Commit.class);
    }

    public void rollback() {

        invoke(Rollback.class);
    }

    public boolean prepare() {

        Boolean vote = (Boolean) invoke(Prepare.class);
        if (vote == null) {
            return true;
        } else {
            return vote;
        }
    }
}
