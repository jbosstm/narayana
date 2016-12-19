package org.jboss.narayana.compensations.internal.remote;

import com.arjuna.wst.SystemException;
import com.arjuna.wst.UnknownTransactionException;
import com.arjuna.wst.WrongStateException;
import com.arjuna.wst11.BAParticipantManager;
import org.jboss.narayana.compensations.internal.CurrentTransaction;
import org.jboss.narayana.compensations.internal.ParticipantManager;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;

import javax.xml.namespace.QName;

/**
 * @author paul.robinson@redhat.com
 * @author gytis@redhat.com
 */
public class RemoteParticipantManager implements ParticipantManager {

    private final BAParticipantManager baParticipantManager;

    private final String participantId;

    private final CurrentTransaction currentTransaction;

    private final CompensationContextStateManager compensationContextStateManager;

    public RemoteParticipantManager(BAParticipantManager baParticipantManager, String participantId,
            CurrentTransaction currentTransaction, CompensationContextStateManager compensationContextStateManager) {
        this.baParticipantManager = baParticipantManager;
        this.participantId = participantId;
        this.currentTransaction = currentTransaction;
        this.compensationContextStateManager = compensationContextStateManager;
    }

    /**
     * Detach participant from the compensation context and tell compensation context manager to update its recovery record.
     * Then notify remote transaction coordinator that participant has exited the transaction.
     * 
     * @throws WrongStateException
     * @throws UnknownTransactionException
     * @throws SystemException
     */
    @Override
    public void exit() throws WrongStateException, UnknownTransactionException, SystemException {
        compensationContextStateManager.getCurrent().detachParticipant(participantId);
        compensationContextStateManager.persist(currentTransaction.getId());
        baParticipantManager.exit();
    }

    /**
     * Tell compensation context manager to update its recovery record. And then notify remote transaction coordinator that
     * participant has completed.
     * 
     * @throws WrongStateException
     * @throws UnknownTransactionException
     * @throws SystemException
     */
    @Override
    public void completed() throws WrongStateException, UnknownTransactionException, SystemException {
        compensationContextStateManager.persist(currentTransaction.getId());
        baParticipantManager.completed();
    }

    @Override
    public void cannotComplete() throws WrongStateException, UnknownTransactionException, SystemException {
        baParticipantManager.cannotComplete();
    }

    @Override
    public void fail(QName exceptionIdentifier) throws SystemException {
        baParticipantManager.fail(exceptionIdentifier);
    }
}
