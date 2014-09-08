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

import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;
import org.jboss.narayana.txframework.api.annotation.lifecycle.ba.Completes;
import org.jboss.narayana.txframework.api.configuration.transaction.CompletionType;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.api.management.WSBATxControl;
import org.jboss.narayana.txframework.impl.Participant;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;
import org.jboss.narayana.txframework.impl.handlers.ProtocolHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

public class WSBAHandler implements ProtocolHandler {

    private ServiceInvocationMeta serviceInvocationMeta;
    private BAParticipantManager participantManager;

    private Participant participant;

    WSBATxControl wsbaTxControl = new WSBATxControlImpl();

    public WSBAHandler(ServiceInvocationMeta serviceInvocationMeta, CompletionType completionType) throws TXFrameworkException {

        this.serviceInvocationMeta = serviceInvocationMeta;

        try {
            BusinessActivityManager businessActivityManager = BusinessActivityManagerFactory.businessActivityManager();

            if (completionType == CompletionType.COORDINATOR) {
                WSBACoordinatorCompletionParticipant coordinatorCompletionParticipant = new WSBACoordinatorCompletionParticipant(serviceInvocationMeta, new HashMap());
                participantManager = businessActivityManager.enlistForBusinessAgreementWithCoordinatorCompletion(coordinatorCompletionParticipant, serviceInvocationMeta.getServiceClass().getName() + UUID.randomUUID());
                participant = coordinatorCompletionParticipant;
            } else {
                WSBAParticipantCompletionParticipant participantCompletionParticipant = new WSBAParticipantCompletionParticipant(serviceInvocationMeta, new HashMap());
                participantManager = businessActivityManager.enlistForBusinessAgreementWithParticipantCompletion(participantCompletionParticipant,
                        serviceInvocationMeta.getServiceClass().getName() + UUID.randomUUID());
                participant = participantCompletionParticipant;
            }

        } catch (WrongStateException e) {
            throw new ParticipantRegistrationException("Transaction was not in a state in which participants can be registered", e);
        } catch (UnknownTransactionException e) {
            throw new ParticipantRegistrationException("Can't register a participant as the transaction in unknown", e);
        } catch (SystemException e) {
            throw new ParticipantRegistrationException("A SystemException occurred when attempting to register a participant", e);
        }

        WSBATxControlImpl.resume(participantManager);
    }

    public void preInvocation() throws Exception {

        WSBATxControlImpl.resume(participantManager);
        participant.resume();
    }

    @Override
    public void notifySuccess() throws TXFrameworkException {

        //todo: find a better way of getting the current status of the TX
        if (shouldComplete(serviceInvocationMeta.getServiceMethod()) && !((WSBATxControlImpl) wsbaTxControl).isCannotComplete()) {
            try {
                participantManager.completed();
            } catch (WrongStateException e) {
                throw new TXFrameworkException("Error notifying completion on participant manager.", e);
            } catch (UnknownTransactionException e) {
                throw new TXFrameworkException("Error notifying completion on participant manager.", e);
            } catch (SystemException e) {
                throw new TXFrameworkException("Error notifying completion on participant manager.", e);
            }
        }
        Participant.suspend();
        WSBATxControlImpl.suspend();
    }

    @Override
    public void notifyFailure() throws TXFrameworkException {

        try {
            participantManager.cannotComplete();
        } catch (WrongStateException e) {
            throw new TXFrameworkException("Error notifying cannotComplete on participant manager.", e);
        } catch (UnknownTransactionException e) {
            throw new TXFrameworkException("Error notifying cannotComplete on participant manager.", e);
        } catch (SystemException e) {
            throw new TXFrameworkException("Error notifying cannotComplete on participant manager.", e);
        }
        Participant.suspend();
        WSBATxControlImpl.suspend();
    }

    private boolean shouldComplete(Method serviceMethod) {

        Completes completes = serviceMethod.getAnnotation(Completes.class);
        return completes != null;
    }

}
