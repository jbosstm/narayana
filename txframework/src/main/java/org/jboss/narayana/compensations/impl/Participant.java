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
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;
import org.jboss.narayana.txframework.impl.TXDataMapImpl;

import javax.enterprise.inject.spi.BeanManager;
import java.util.Map;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public class Participant implements BusinessAgreementWithParticipantCompletionParticipant, ConfirmCompletedParticipant {

    private Class<? extends CompensationHandler> compensationHandler;

    private Class<? extends ConfirmationHandler> confirmationHandler;

    private Class<? extends TransactionLoggedHandler> transactionLoggedHandler;

    private BeanManager beanManager;

    private Map txDataMapState;

    public Participant(Class<? extends CompensationHandler> compensationHandlerClass, Class<? extends ConfirmationHandler> confirmationHandlerClass, Class<? extends TransactionLoggedHandler> transactionLoggedHandlerClass) {

        this.compensationHandler = compensationHandlerClass;
        this.confirmationHandler = confirmationHandlerClass;
        this.transactionLoggedHandler = transactionLoggedHandlerClass;

        beanManager = BeanManagerUtil.getBeanManager();
        txDataMapState = TXDataMapImpl.getState();
    }

    private <T extends Object> T instantiate(Class<T> clazz) {

        if (clazz == null) {
            return null;
        }
        return BeanManagerUtil.createBeanInstance(clazz, beanManager);
    }

    @Override
    public void confirmCompleted(boolean confirmed) {

        if (transactionLoggedHandler != null) {
            TXDataMapImpl.resume(txDataMapState);
            TransactionLoggedHandler handler = instantiate(transactionLoggedHandler);
            handler.transactionLogged(confirmed);
            TXDataMapImpl.suspend();
        }
    }

    @Override
    public void close() throws WrongStateException, SystemException {

        if (confirmationHandler != null) {
            TXDataMapImpl.resume(txDataMapState);
            ConfirmationHandler handler = instantiate(confirmationHandler);
            handler.confirm();
            TXDataMapImpl.suspend();
        }
    }

    @Override
    public void cancel() throws FaultedException, WrongStateException, SystemException {
        //TODO: Do nothing?
    }

    @Override
    public void compensate() throws FaultedException, WrongStateException, SystemException {

        try {
            if (compensationHandler != null) {
                TXDataMapImpl.resume(txDataMapState);
                CompensationHandler handler = instantiate(compensationHandler);
                handler.compensate();
                TXDataMapImpl.suspend();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
