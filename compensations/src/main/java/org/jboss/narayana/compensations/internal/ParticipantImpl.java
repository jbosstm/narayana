/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.internal;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public class ParticipantImpl implements BusinessAgreementWithParticipantCompletionParticipant, ConfirmCompletedParticipant {

    private static final Map<Object, AtomicInteger> PARTICIPANT_COUNTERS = new HashMap<>();

    private CompensationHandler compensationHandler;

    private ConfirmationHandler confirmationHandler;

    private TransactionLoggedHandler transactionLoggedHandler;

    private ClassLoader applicationClassloader;

    private Object currentTX;

    public ParticipantImpl(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
            TransactionLoggedHandler transactionLoggedHandler, Object currentTX) {

        this.compensationHandler = compensationHandler;
        this.confirmationHandler = confirmationHandler;
        this.transactionLoggedHandler = transactionLoggedHandler;
        this.currentTX = currentTX;

        applicationClassloader = Thread.currentThread().getContextClassLoader();

        incrementParticipantsCounter();
    }

    @Override
    public void confirmCompleted(boolean confirmed) {

        if (transactionLoggedHandler != null) {

            ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(applicationClassloader);

            transactionLoggedHandler.transactionLogged(confirmed);

            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }

    @Override
    public void close() throws WrongStateException, SystemException {

        if (confirmationHandler != null) {

            ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(applicationClassloader);
            CompensationContext.setTxContextToExtend(currentTX);

            confirmationHandler.confirm();

            Thread.currentThread().setContextClassLoader(origClassLoader);
        }

        decrementParticipantsCounter();
    }

    @Override
    public void cancel() throws FaultedException, WrongStateException, SystemException {
        //TODO: Do nothing?
    }

    @Override
    public void compensate() throws FaultedException, WrongStateException, SystemException {

        try {
            if (compensationHandler != null) {

                ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(applicationClassloader);
                CompensationContext.setTxContextToExtend(currentTX);

                compensationHandler.compensate();

                Thread.currentThread().setContextClassLoader(origClassLoader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        decrementParticipantsCounter();
    }

    @Override
    public String status() throws SystemException {
        //TODO: what to do here?
        return null;
    }

    @Override
    public void unknown() throws SystemException {

    }

    @Override
    public void error() throws SystemException {

    }

    /**
     * Increments the counter of the Compensations participants in the transaction.
     */
    private void incrementParticipantsCounter() {

        synchronized (PARTICIPANT_COUNTERS) {
            final AtomicInteger counter = PARTICIPANT_COUNTERS.get(currentTX);

            if (counter == null) {
                PARTICIPANT_COUNTERS.put(currentTX, new AtomicInteger(1));
            } else {
                counter.incrementAndGet();
            }
        }
    }

    /**
     * Decrements the counter of the Compensations participants in the transaction.
     * CompensationContext of the current transaction is destroyed once the counter reaches 0.
     */
    private void decrementParticipantsCounter() {

        synchronized (PARTICIPANT_COUNTERS) {
            final AtomicInteger counter = PARTICIPANT_COUNTERS.get(currentTX);

            if (counter == null || counter.decrementAndGet() > 0) {
                return;
            }

            PARTICIPANT_COUNTERS.remove(currentTX);
        }

        CompensationContext.close(currentTX);
    }
}