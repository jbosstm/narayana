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

package org.jboss.narayana.txframework.impl.handlers.wsat;

import com.arjuna.wst.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error;
import org.jboss.narayana.txframework.api.annotation.lifecycle.at.*;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.util.Map;

public class WSATVolatile2PCParticipant extends org.jboss.narayana.txframework.impl.Participant implements Volatile2PCParticipant {

    public WSATVolatile2PCParticipant(ServiceInvocationMeta serviceInvocationMeta, Map txDataMap) throws ParticipantRegistrationException {

        super(serviceInvocationMeta, txDataMap);
        registerEventsOfInterest(PrePrepare.class, PostCommit.class, Rollback.class, Error.class, Unknown.class);
    }

    public void commit() throws WrongStateException, SystemException {

        invoke(PostCommit.class);
    }

    public void rollback() throws WrongStateException, SystemException {

        invoke(Rollback.class);
    }

    public void error() throws SystemException {

        invoke(org.jboss.narayana.txframework.api.annotation.lifecycle.at.Error.class);
    }

    public Vote prepare() throws WrongStateException, SystemException {
        //todo check type
        Boolean prepare = (Boolean) invoke(PrePrepare.class);
        if (prepare == null) {
            return new Prepared();
        } else if (prepare) {
            return new Prepared();
        } else {
            return new Aborted();
        }
    }

    public void unknown() throws SystemException {

        invoke(Unknown.class);
    }
}
