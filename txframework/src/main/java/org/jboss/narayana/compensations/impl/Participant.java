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
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.helpers.TCCLSingletonProvider;

import javax.enterprise.inject.spi.BeanManager;
import java.util.Map;

/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
public class Participant implements BusinessAgreementWithParticipantCompletionParticipant, ConfirmCompletedParticipant {

    private Class<? extends CompensationHandler> compensationHandler;

    private Class<? extends ConfirmationHandler> confirmationHandler;

    private Class<? extends TransactionLoggedHandler> transactionLoggedHandler;

    private Map<String, Object> rememberedBeans;

    private SingletonProvider singletonProvider;

    private BeanManager beanManager;

    public Participant(Class<? extends CompensationHandler> compensationHandlerClass, Class<? extends ConfirmationHandler> confirmationHandlerClass, Class<? extends TransactionLoggedHandler> transactionLoggedHandlerClass) {

        this.compensationHandler = compensationHandlerClass;
        this.confirmationHandler = confirmationHandlerClass;
        this.transactionLoggedHandler = transactionLoggedHandlerClass;

        rememberedBeans = CompensationContext.getBeansForThisTransaction();
        beanManager = BeanManagerLookup.getBeanManager();
        singletonProvider = TCCLSingletonProvider.instance();
    }

    private <T extends Object> T instantiate(Class<T> clazz) {

        if (clazz == null) {
            return null;
        }
        return (T) ProgrammaticBeanLookup.lookup(clazz, beanManager);
    }

    @Override
    public void confirmCompleted(boolean confirmed) {

        if (transactionLoggedHandler != null) {
            TransactionLoggedHandler handler = instantiate(transactionLoggedHandler);
            handler.transactionLogged(confirmed);
        }
    }

    @Override
    public void close() throws WrongStateException, SystemException {

        CompensationContext.rememberForAfterTransaction(rememberedBeans);
        if (confirmationHandler != null) {
            ConfirmationHandler handler = instantiate(confirmationHandler);
            handler.confirm();
        }
        CompensationContext.forgetAfterTransactionBeans();
    }

    @Override
    public void cancel() throws FaultedException, WrongStateException, SystemException {
        //TODO: Do nothing?
    }

    @Override
    public void compensate() throws FaultedException, WrongStateException, SystemException {

        try {
            CompensationContext.rememberForAfterTransaction(rememberedBeans);
            TCCLSingletonProvider.reset();
            TCCLSingletonProvider.initialize(singletonProvider);
            if (compensationHandler != null) {
                CompensationHandler handler = instantiate(compensationHandler);
                handler.compensate();
            }
            CompensationContext.forgetAfterTransactionBeans();
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
