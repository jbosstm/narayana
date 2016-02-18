/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
