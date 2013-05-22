/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.impl;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.FaultedException;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.ConfirmCompletedParticipant;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.CompensationTransactionRuntimeException;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public class CompensationParticipant implements BusinessAgreementWithParticipantCompletionParticipant, ConfirmCompletedParticipant {

    private CompensationHandler compensationHandler;

    private ConfirmationHandler confirmationHandler;

    private TransactionLoggedHandler transactionLoggedHandler;


    public CompensationParticipant(Class<? extends CompensationHandler> compensationHandlerClass, Class<? extends ConfirmationHandler> confirmationHandlerClass, Class<? extends TransactionLoggedHandler> transactionLoggedHandlerClass) {

        this.compensationHandler = instantiate(compensationHandlerClass);
        this.confirmationHandler = instantiate(confirmationHandlerClass);
        this.transactionLoggedHandler = instantiate(transactionLoggedHandlerClass);
    }

    private <T extends Object> T instantiate(Class<T> clazz) {

        if (clazz == null) {
            return null;
        }
        try {
            return (T) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new CompensationTransactionRuntimeException("Error instantiating handler of type: " + clazz.getName());
        } catch (IllegalAccessException e) {
            throw new CompensationTransactionRuntimeException("Error instantiating handler of type: " + clazz.getName());
        }
    }

    @Override
    public void confirmCompleted(boolean confirmed) {

        if (transactionLoggedHandler != null) {
            transactionLoggedHandler.transactionLogged(confirmed);
        }
    }

    @Override
    public void close() throws WrongStateException, SystemException {

        if (confirmationHandler != null) {
            confirmationHandler.confirm();
        }
    }

    @Override
    public void cancel() throws FaultedException, WrongStateException, SystemException {
        //TODO: Do nothing?
    }

    @Override
    public void compensate() throws FaultedException, WrongStateException, SystemException {

        if (compensationHandler != null) {
            compensationHandler.compensate();
        }
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
}
