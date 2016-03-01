package org.jboss.narayana.compensations.impl;


import org.jboss.narayana.compensations.api.CompensationHandler;
import org.jboss.narayana.compensations.api.ConfirmationHandler;
import org.jboss.narayana.compensations.api.TransactionLoggedHandler;

/**
 * @author paul.robinson@redhat.com 19/04/2014
 */
public interface BAControler {

    void beginBusinessActivity() throws Exception;

    void closeBusinessActivity() throws Exception;

    void cancelBusinessActivity() throws Exception;

    void completeBusinessActivity(boolean isException) throws Exception;

    public boolean isBARunning();

    public Object suspend() throws Exception;

    public void resume(Object context) throws Exception;

    public Object getCurrentTransaction() throws Exception;

    public ParticipantManager enlist(Class<? extends CompensationHandler> compensationHandlerClass,
                                     Class<? extends ConfirmationHandler> confirmationHandlerClass,
                                     Class<? extends TransactionLoggedHandler> transactionLoggedHandlerClass) throws Exception;

    public ParticipantManager enlist(CompensationHandler compensationHandler, ConfirmationHandler confirmationHandler,
                                     TransactionLoggedHandler transactionLoggedHandler) throws Exception;
}
