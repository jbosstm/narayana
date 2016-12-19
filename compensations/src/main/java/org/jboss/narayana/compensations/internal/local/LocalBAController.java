package org.jboss.narayana.compensations.internal.local;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wscf.exceptions.ProtocolNotRegisteredException;
import com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorCancelledException;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
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

/**
 * @author paul.robinson@redhat.com
 * @author gytis@redhat.com
 */
public class LocalBAController implements BAController {

    private final CompensationContextStateManager compensationContextStateManager;

    public LocalBAController(CompensationContextStateManager compensationContextStateManager) {
        this.compensationContextStateManager = compensationContextStateManager;
    }

    @Override
    public void beginBusinessActivity() throws Exception {
        CoordinatorManagerFactory.coordinatorManager().begin("Sagas11HLS");
        CompensationManagerImpl.resume(new CompensationManagerState());
        compensationContextStateManager.activate(getCurrentTransaction().getId());
    }

    @Override
    public void closeBusinessActivity() throws Exception {
        CurrentTransaction currentTransaction = getCurrentTransaction();
        compensationContextStateManager.deactivate();
        CoordinatorManagerFactory.coordinatorManager().close();
        compensationContextStateManager.remove(currentTransaction.getId());
    }

    @Override
    public void cancelBusinessActivity() throws Exception {
        CurrentTransaction currentTransaction = getCurrentTransaction();
        compensationContextStateManager.deactivate();
        CoordinatorManagerFactory.coordinatorManager().cancel();
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
            } catch (CoordinatorCancelledException e) {
                throw new TransactionCompensatedException("Failed to close transaction", e);
            }
        }
    }

    public boolean isBARunning() {
        try {
            return CoordinatorManagerFactory.coordinatorManager().currentActivity() != null;
        } catch (SystemException e) {
            return false;
        } catch (ProtocolNotRegisteredException e) {
            return false;
        }
    }

    public CurrentTransaction suspend() throws Exception {
        compensationContextStateManager.deactivate();
        return new LocalCurrentTransaction(CoordinatorManagerFactory.coordinatorManager().suspend());
    }

    public void resume(CurrentTransaction currentTransaction) throws Exception {
        if (currentTransaction.getDelegateClass() != ActivityHierarchy.class) {
            throw new Exception("Invalid current transaction type: " + currentTransaction);
        }
        CoordinatorManagerFactory.coordinatorManager().resume((ActivityHierarchy) currentTransaction.getDelegate());
        compensationContextStateManager.activate(currentTransaction.getId());
    }


    @Override
    public CurrentTransaction getCurrentTransaction() throws Exception {
        ActivityHierarchy context = CoordinatorManagerFactory.coordinatorManager().currentActivity();
        if (context == null) {
            return null;
        }
        return new LocalCurrentTransaction(context);
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

        CurrentTransaction transaction = getCurrentTransaction();
        String participantId = new Uid().stringForm();
        String coordinatorId = CoordinatorManagerFactory.coordinatorManager().identifier().toString();
        LocalParticipant participant = new LocalParticipant(compensationHandler, confirmationHandler, transactionLoggedHandler,
                transaction, participantId, coordinatorId, compensationContextStateManager, new DeserializerHelper());

        CoordinatorManagerFactory.coordinatorManager().enlistParticipant(participant);

        return new LocalParticipantManager(participant, transaction, compensationContextStateManager);
    }

    private <T> T instantiate(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }

        return BeanManagerUtil.createBeanInstance(clazz);
    }

}
