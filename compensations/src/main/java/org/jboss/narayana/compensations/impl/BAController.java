package org.jboss.narayana.compensations.impl;

import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;

/**
 * @author paul.robinson@redhat.com 19/04/2014
 */
public interface BAController {

    void beginBusinessActivity() throws Exception;

    void closeBusinessActivity() throws Exception;

    void cancelBusinessActivity() throws Exception;

    void completeBusinessActivity(boolean isException) throws Exception;

    boolean isBARunning();

    Object suspend() throws Exception;

    void resume(Object context) throws Exception;

    Object getCurrentTransaction() throws Exception;

    ParticipantManager enlist(Class<? extends CompensationHandler> compensationHandlerClass,
            Class<? extends ConfirmationHandler> confirmationHandlerClass,
            Class<? extends TransactionLoggedHandler> transactionLoggedHandlerClass) throws Exception;

    ParticipantManager enlist(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
            TransactionLoggedHandler transactionLoggedHandler) throws Exception;
}
