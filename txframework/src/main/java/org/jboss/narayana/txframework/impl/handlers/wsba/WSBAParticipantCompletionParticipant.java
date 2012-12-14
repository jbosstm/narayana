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

package org.jboss.narayana.txframework.impl.handlers.wsba;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.*;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error;
import org.jboss.narayana.txframework.impl.Participant;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;

import java.io.Serializable;
import java.util.Map;

public class WSBAParticipantCompletionParticipant extends Participant implements BusinessAgreementWithParticipantCompletionParticipant,
        ConfirmCompletedParticipant, Serializable {

    protected final WSBAParticipantRegistry participantRegistry = new WSBAParticipantRegistry();
    private String txid;

    public WSBAParticipantCompletionParticipant(ServiceInvocationMeta serviceInvocationMeta, Map txDataMap, String txid) throws ParticipantRegistrationException {

        super(serviceInvocationMeta, txDataMap);
        this.txid = txid;

        registerEventsOfInterest(Cancel.class, Close.class, Compensate.class, ConfirmCompleted.class, Error.class, Status.class, Unknown.class);
    }

    public void error() throws SystemException {

        invoke(org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Error.class);
    }

    public void close() throws WrongStateException, SystemException {

        invoke(Close.class);
        participantRegistry.forget(txid);
    }

    public void cancel() throws FaultedException, WrongStateException, SystemException {

        invoke(Cancel.class);
        participantRegistry.forget(txid);
    }

    public void compensate() throws FaultedException, WrongStateException, SystemException {

        invoke(Compensate.class);
        participantRegistry.forget(txid);
    }

    public String status() throws SystemException {
        //todo: return a default status
        //todo: check impl returns a String
        return (String) invoke(Status.class);
    }

    @Deprecated
    public void unknown() throws SystemException {

        invoke(Unknown.class);
    }

    public void confirmCompleted(boolean completed) {

        invoke(ConfirmCompleted.class, completed);
    }
}
