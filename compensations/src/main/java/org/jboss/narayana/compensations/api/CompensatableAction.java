/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.api;

/**
 * Multi-work action which is executed atomically. This is an alternative API to use compensating transactions.
 * If all work was completed successfully compensatable transaction is confirmed, otherwise it is compensated.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public interface CompensatableAction {

    /**
     * Add a piece of work to the action with a compensation handler.
     *
     * @param compensatableWork Work that needs to be executed atomically.
     * @param compensationHandler Handler which needs to be invoked in case of failure to undo the work.
     * @return this instance of CompensatableAction.
     */
    CompensatableAction addWork(CompensatableWork compensatableWork, CompensationHandler compensationHandler);

    /**
     * Add a piece of work to the action with a confirmation handler.
     *
     * @param compensatableWork Work that needs to be executed atomically.
     * @param confirmationHandler Handler which needs to be invoked if action is completed successfully.
     * @return this instance of CompensatableAction
     */
    CompensatableAction addWork(CompensatableWork compensatableWork, ConfirmationHandler confirmationHandler);

    /**
     * Add a piece of work to the action with a compensation and a confirmation handlers.
     *
     * @param compensatableWork Work that needs to be executed atomically.
     * @param compensationHandler Handler which needs to be invoked in case of failure to undo the work.
     * @param confirmationHandler Handler which needs to be invoked if action is completed successfully.
     * @return this instance of CompensatableAction
     */
    CompensatableAction addWork(CompensatableWork compensatableWork, CompensationHandler compensationHandler,
            ConfirmationHandler confirmationHandler);

    /**
     * Execute all registered work pieces one by one.
     *
     * @throws EnlistException Is thrown if action fails to register any handler with the transaction.
     */
    void execute() throws EnlistException;

}