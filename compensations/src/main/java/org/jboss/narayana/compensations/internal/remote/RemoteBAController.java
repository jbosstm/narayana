/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.narayana.compensations.internal.remote;

import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.BusinessActivityManagerFactory;
import com.arjuna.mw.wst11.UserBusinessActivityFactory;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;
import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;
import org.jboss.narayana.compensations.internal.BAController;
import org.jboss.narayana.compensations.internal.BeanManagerUtil;
import org.jboss.narayana.compensations.internal.CompensationManagerImpl;
import org.jboss.narayana.compensations.internal.CompensationManagerState;
import org.jboss.narayana.compensations.internal.ParticipantManager;

import java.util.UUID;

/**
 * @author paul.robinson@redhat.com 19/04/2014
 */
public class RemoteBAController implements BAController {

    @Override
    public void beginBusinessActivity() throws WrongStateException, SystemException {

        UserBusinessActivityFactory.userBusinessActivity().begin();
        CompensationManagerImpl.resume(new CompensationManagerState());
    }

    @Override
    public void closeBusinessActivity() throws WrongStateException, UnknownTransactionException, TransactionRolledBackException, SystemException {

        UserBusinessActivityFactory.userBusinessActivity().close();
        CompensationManagerImpl.suspend();
    }

    @Override
    public void cancelBusinessActivity() throws WrongStateException, UnknownTransactionException, SystemException {

        UserBusinessActivityFactory.userBusinessActivity().cancel();
        CompensationManagerImpl.suspend();
    }

    @Override
    public void completeBusinessActivity(final boolean isException) throws WrongStateException, UnknownTransactionException, SystemException {

        if (CompensationManagerImpl.isCompensateOnly() && !isException) {
            cancelBusinessActivity();
            throw new TransactionCompensatedException("Transaction was marked as 'compensate only'");
        } else if (CompensationManagerImpl.isCompensateOnly()) {
            cancelBusinessActivity();
        } else {
            try {
                closeBusinessActivity();
            } catch (TransactionRolledBackException e) {
                throw new TransactionCompensatedException("Failed to close transaction", e);
            }
        }
    }

    public boolean isBARunning() {

        try {

            BusinessActivityManager businessActivityManager = BusinessActivityManagerFactory.businessActivityManager();
            if (businessActivityManager == null) {
                return false;
            }
            return BusinessActivityManagerFactory.businessActivityManager().currentTransaction() != null;
        } catch (SystemException e) {
            return false;
        }
    }

    public Object suspend() throws Exception {

        return BusinessActivityManagerFactory.businessActivityManager().suspend();
    }

    public void resume(Object context) throws Exception {

        BusinessActivityManagerFactory.businessActivityManager().resume((TxContext) context);
    }


    @Override
    public Object getCurrentTransaction() throws Exception {

        return BusinessActivityManagerFactory.businessActivityManager().currentTransaction();
    }

    @Override
    public ParticipantManager enlist(Class<? extends CompensationHandler> compensationHandlerClass,
                                     Class<? extends ConfirmationHandler> confirmationHandlerClass,
                                     Class<? extends TransactionLoggedHandler> transactionLoggedHandlerClass) throws Exception {

        CompensationHandler compensationHandler = instantiate(compensationHandlerClass);
        ConfirmationHandler confirmationHandler = instantiate(confirmationHandlerClass);
        TransactionLoggedHandler transactionLoggedHandler = instantiate(transactionLoggedHandlerClass);

        return enlist(compensationHandler, confirmationHandler, transactionLoggedHandler);
    }

    @Override
    public ParticipantManager enlist(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
            TransactionLoggedHandler transactionLoggedHandler) throws Exception {

        RemoteParticipant participant = new RemoteParticipant(compensationHandler, confirmationHandler,
                transactionLoggedHandler, getCurrentTransaction());
        BAParticipantManager baParticipantManager = BusinessActivityManagerFactory.businessActivityManager()
                .enlistForBusinessAgreementWithParticipantCompletion(participant, String.valueOf(UUID.randomUUID()));

        return new RemoteParticipantManager(baParticipantManager);
    }

    private <T> T instantiate(Class<T> clazz) {

        if (clazz == null) {
            return null;
        }

        return BeanManagerUtil.createBeanInstance(clazz);
    }

}
