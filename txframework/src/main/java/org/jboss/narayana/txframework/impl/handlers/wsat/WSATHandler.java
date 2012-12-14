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

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst11.TransactionManagerFactory;
import com.arjuna.mw.wst11.UserTransactionFactory;
import com.arjuna.wst.*;
import org.jboss.narayana.txframework.api.exception.TXFrameworkException;
import org.jboss.narayana.txframework.impl.Participant;
import org.jboss.narayana.txframework.impl.ServiceInvocationMeta;
import org.jboss.narayana.txframework.impl.handlers.ParticipantRegistrationException;
import org.jboss.narayana.txframework.impl.handlers.ProtocolHandler;

import javax.interceptor.InvocationContext;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WSATHandler implements ProtocolHandler {

    private static final WSATParticipantRegistry participantRegistry = new WSATParticipantRegistry();

    private static final Map<String, Participant> durableServiceParticipants = new HashMap<String, Participant>();

    public WSATHandler(ServiceInvocationMeta serviceInvocationMeta) throws TXFrameworkException {

        try {
            synchronized (participantRegistry) {
                String txid = UserTransactionFactory.userTransaction().toString();

                //Only create participant if there is not already a participant for this ServiceImpl and this transaction
                Participant participantToResume;
                if (!participantRegistry.isRegistered(txid, serviceInvocationMeta.getServiceClass())) {
                    Map txDataMap = new HashMap();
                    Volatile2PCParticipant volatileParticipant = new WSATVolatile2PCParticipant(serviceInvocationMeta, txDataMap, txid);
                    Durable2PCParticipant durableParticipant = new WSATDurable2PCParticipant(serviceInvocationMeta, txDataMap);

                    TransactionManager transactionManager = TransactionManagerFactory.transactionManager();
                    String idPrefix = serviceInvocationMeta.getServiceClass().getName();
                    transactionManager.enlistForVolatileTwoPhase(volatileParticipant, idPrefix + UUID.randomUUID());
                    transactionManager.enlistForDurableTwoPhase(durableParticipant, idPrefix + UUID.randomUUID());

                    participantRegistry.register(txid, serviceInvocationMeta.getServiceClass());

                    synchronized (durableServiceParticipants) {
                        participantToResume = (Participant) durableParticipant;
                        durableServiceParticipants.put(txid, participantToResume);
                    }
                } else {
                    participantToResume = durableServiceParticipants.get(txid);
                }
                participantToResume.resume();
            }
        } catch (WrongStateException e) {
            throw new ParticipantRegistrationException("Transaction was not in a state in which participants can be registered", e);
        } catch (UnknownTransactionException e) {
            throw new ParticipantRegistrationException("Can't register a participant as the transaction in unknown", e);
        } catch (SystemException e) {
            throw new ParticipantRegistrationException("A SystemException occurred when attempting to register a participant", e);
        }
    }

    @Override
    public Object proceed(InvocationContext ic) throws Exception {

        return ic.proceed();
    }

    @Override
    public void notifySuccess() {

        Participant.suspend();
    }

    @Override
    public void notifyFailure() {
        //Todo: ensure transaction rolled back
        Participant.suspend();
    }

}
