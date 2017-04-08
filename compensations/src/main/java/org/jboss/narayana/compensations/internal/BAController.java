package org.jboss.narayana.compensations.internal;

import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;

/**
 * Controller of the underlying business activity implementation.
 *
 * @author paul.robinson@redhat.com 19/04/2014
 */
public interface BAController {

    /**
     * Start a new compensating transaction.
     *
     * @throws Exception if a start of a new compensating transaction has failed.
     */
    void beginBusinessActivity() throws Exception;

    /**
     * Close current compensating transaction.
     *
     * @throws Exception if closing of a compensating transaction has failed.
     */
    void closeBusinessActivity() throws Exception;

    /**
     * Cancel current compensating transaction.
     *
     * @throws Exception if cancellation of a compensating transaction has failed.
     */
    void cancelBusinessActivity() throws Exception;

    /**
     * Complete current compensating transaction.
     *
     * @param isException is compensating transaction being closed as a result of an exception.
     * @throws Exception if completion of a compensating transaction has failed.
     */
    void completeBusinessActivity(boolean isException) throws Exception;

    /**
     * Check if there is an active compensating transaction associated with the thread.
     *
     * @return {@code true} if there is a compensating transaction currently active and {@code false} otherwise.
     */
    boolean isBARunning();

    /**
     * Suspend current compensating transaction.
     *
     * @return {@link CurrentTransaction} containing information of the suspended transaction.
     * @throws Exception if suspension of a compensating transaction has failed.
     */
    CurrentTransaction suspend() throws Exception;

    /**
     * Resume previously suspended compensating transaction.
     *
     * @param currentTransaction {@link CurrentTransaction} containing information of the transaction to be resumed.
     * @throws Exception if resumption of a compensating transaction has failed.
     */
    void resume(CurrentTransaction currentTransaction) throws Exception;

    /**
     * Get info of a currently running compensating transaction.
     * 
     * @return {@link CurrentTransaction} containing information of the transaction or {@code null} if there is no active
     *         transaction.
     * @throws Exception if failure occurred when getting transaction information.
     */
    CurrentTransaction getCurrentTransaction() throws Exception;

    /**
     * Enlist compensation handlers as a single business activity participant. Handlers are allowed to be null.
     * 
     * Handler implementations must have a no-parameter constructor, because coordinator will instantiate them before
     * enlistment.
     * 
     * @param compensationHandlerClass class name of a handler to be invoked if the transaction was compensated.
     * @param confirmationHandlerClass class name of a handler to be invoked if the transaction was closed.
     * @param transactionLoggedHandlerClass class name of a handler to be invoked after the participant has completed.
     * @return manager to update coordinator once the participant has completed its work and is ready to complete the
     *         transaction.
     * @throws Exception if enlistment of a participant has failed.
     */
    ParticipantManager enlist(Class<? extends CompensationHandler> compensationHandlerClass,
            Class<? extends ConfirmationHandler> confirmationHandlerClass,
            Class<? extends TransactionLoggedHandler> transactionLoggedHandlerClass) throws Exception;

    /**
     * Enlist compensation handlers as a single business activity participant. Handlers are allowed to be null.
     * 
     * @param compensationHandler instance of a handler to be invoked if the transaction was compensated.
     * @param confirmationHandler instance of a handler to be invoked if the transaction was closed.
     * @param transactionLoggedHandler instance of a handler to be invoked after the participant has completed.
     * @return manager to update coordinator once the participant has completed its work and is ready to complete the
     *         transaction.
     * @throws Exception if enlistment of a participant has failed.
     */
    ParticipantManager enlist(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
            TransactionLoggedHandler transactionLoggedHandler) throws Exception;
}
