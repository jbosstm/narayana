package org.jboss.narayana.compensations.internal.remote;

import com.arjuna.ats.arjuna.common.Uid;
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
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;
import org.jboss.narayana.compensations.internal.recovery.DeserializerHelper;
import org.jboss.narayana.compensations.internal.utils.BeanManagerUtil;
import org.jboss.narayana.compensations.internal.CompensationManagerImpl;
import org.jboss.narayana.compensations.internal.CompensationManagerState;
import org.jboss.narayana.compensations.internal.CurrentTransaction;
import org.jboss.narayana.compensations.internal.ParticipantManager;

import java.util.UUID;

/**
 * @author paul.robinson@redhat.com
 * @author gytis@redhat.com
 */
public class RemoteBAController implements BAController {

    private final CompensationContextStateManager compensationContextStateManager;

    public RemoteBAController(CompensationContextStateManager compensationContextStateManager) {
        this.compensationContextStateManager = compensationContextStateManager;
    }

    @Override
    public void beginBusinessActivity() throws Exception {
        UserBusinessActivityFactory.userBusinessActivity().begin();
        CompensationManagerImpl.resume(new CompensationManagerState());
        compensationContextStateManager.activate(getCurrentTransaction().getId());
    }

    @Override
    public void closeBusinessActivity() throws Exception {
        CurrentTransaction currentTransaction = getCurrentTransaction();
        compensationContextStateManager.deactivate();
        UserBusinessActivityFactory.userBusinessActivity().close();
        CompensationManagerImpl.suspend();
        compensationContextStateManager.remove(currentTransaction.getId());
    }

    @Override
    public void cancelBusinessActivity() throws Exception {
        CurrentTransaction currentTransaction = getCurrentTransaction();
        compensationContextStateManager.deactivate();
        UserBusinessActivityFactory.userBusinessActivity().cancel();
        CompensationManagerImpl.suspend();
        compensationContextStateManager.remove(currentTransaction.getId());
    }

    @Override
    public void completeBusinessActivity(final boolean isException) throws Exception {
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

    public CurrentTransaction suspend() throws Exception {
        compensationContextStateManager.deactivate();
        return new RemoteCurrentTransaction(BusinessActivityManagerFactory.businessActivityManager().suspend());
    }

    public void resume(CurrentTransaction currentTransaction) throws Exception {
        if (currentTransaction.getDelegateClass() != TxContext.class) {
            throw new Exception("Invalid current transaction type: " + currentTransaction);
        }
        BusinessActivityManagerFactory.businessActivityManager().resume((TxContext) currentTransaction.getDelegate());
        compensationContextStateManager.activate(currentTransaction.getId());
    }


    @Override
    public CurrentTransaction getCurrentTransaction() throws Exception {
        TxContext context = BusinessActivityManagerFactory.businessActivityManager().currentTransaction();
        if (context == null) {
            return null;
        }
        return new RemoteCurrentTransaction(context);
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

        String participantId = new Uid().stringForm();
        CurrentTransaction currentTransaction = getCurrentTransaction();
        RemoteParticipant participant = new RemoteParticipant(compensationHandler, confirmationHandler,
                transactionLoggedHandler, currentTransaction, participantId, compensationContextStateManager,
                new DeserializerHelper());
        BAParticipantManager baParticipantManager = BusinessActivityManagerFactory.businessActivityManager()
                .enlistForBusinessAgreementWithParticipantCompletion(participant, String.valueOf(UUID.randomUUID()));

        return new RemoteParticipantManager(baParticipantManager, participantId, currentTransaction,
                compensationContextStateManager);
    }

    private <T> T instantiate(Class<T> clazz) {

        if (clazz == null) {
            return null;
        }

        return BeanManagerUtil.createBeanInstance(clazz);
    }

}
